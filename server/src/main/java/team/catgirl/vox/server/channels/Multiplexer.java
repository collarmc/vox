package team.catgirl.vox.server.channels;

import com.google.common.collect.EvictingQueue;
import team.catgirl.vox.protocol.AudioPacket;
import team.catgirl.vox.audio.Mixer;
import team.catgirl.vox.protocol.SourceAudioPacket;
import team.catgirl.vox.protocol.AudioStreamPacket;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Multiplexer {

    private static final Logger LOGGER = Logger.getLogger(Multiplexer.class.getName());

    private final ConcurrentMap<UUID, ChannelState> channels = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Future<?>> channelProcessors = new ConcurrentHashMap<>();
    private final ExecutorService consumerExecutor = Executors.newCachedThreadPool();

    private final BiConsumer<UUID, List<AudioStreamPacket>> packetConsumer;

    public Multiplexer(BiConsumer<UUID, List<AudioStreamPacket>> packetConsumer) {
        this.packetConsumer = packetConsumer;
    }

    public void receive(SourceAudioPacket packet) {
        AtomicReference<ChannelState> newChannel = new AtomicReference<>();
        channels.compute(packet.channel, (channelId, channelState) -> {
            if (channelState == null) {
                channelState = new ChannelState(channelId);
                newChannel.set(channelState);
            }
            channelState.packetQueues.compute(packet.owner, (identityId, audioPackets) -> {
                audioPackets = audioPackets == null ? new LinkedBlockingDeque<>(Short.MAX_VALUE) : audioPackets;
                audioPackets.offer(packet.audio);
                return audioPackets;
            });
            return channelState;
        });
        ChannelState channelState = newChannel.get();
        if (channelState != null) {
            Future<?> future = consumerExecutor.submit(new ChannelProcessor(channelState, packetConsumer));
            channelProcessors.put(channelState.id, future);
        };
    }

    private static class ChannelProcessor implements Runnable, Closeable {
        private final ChannelState channel;
        private final BiConsumer<UUID, List<AudioStreamPacket>> packetConsumer;
        private final Mixer mixer = new Mixer();

        public ChannelProcessor(ChannelState channel, BiConsumer<UUID, List<AudioStreamPacket>> packetConsumer) {
            this.channel = channel;
            this.packetConsumer = packetConsumer;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    List<AudioStreamPacket> packetList = channel.packetQueues.entrySet().stream()
                            .map(entry -> {
                                AudioPacket packet = entry.getValue().poll();
                                return new AudioStreamPacket(entry.getKey(), packet == null ? AudioPacket.SILENCE : packet);
                            })
                            .collect(Collectors.toList());
                    packetConsumer.accept(channel.id, packetList);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        packetConsumer.accept(channel.id, null);
                        throw new RuntimeException(e);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        @Override
        public void close() throws IOException {
            mixer.close();
        }
    }

    private static class ChannelState {
        public final UUID id;
        public final ConcurrentMap<UUID, LinkedBlockingDeque<AudioPacket>> packetQueues;

        public ChannelState(UUID id) {
            this.id = id;
            this.packetQueues = new ConcurrentHashMap<>();
        }
    }
}
