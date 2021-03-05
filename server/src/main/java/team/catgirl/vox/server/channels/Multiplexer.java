package team.catgirl.vox.server.channels;

import team.catgirl.vox.audio.AudioPacket;
import team.catgirl.vox.audio.Mixer;
import team.catgirl.vox.protocol.IncomingVoicePacket;

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

    private final BiConsumer<UUID, AudioPacket> packetConsumer;

    public Multiplexer(BiConsumer<UUID, AudioPacket> packetConsumer) {
        this.packetConsumer = packetConsumer;
    }

    public void receive(IncomingVoicePacket packet) {
        AudioPacket audioPacket = AudioPacket.deserialize(packet.audio);
        AtomicReference<ChannelState> newChannel = new AtomicReference<>();
        channels.compute(packet.channel, (channelId, channelState) -> {
            if (channelState == null) {
                channelState = new ChannelState(channelId);
                newChannel.set(channelState);
            }
            channelState.packetQueues.compute(packet.identity, (identityId, audioPackets) -> {
                audioPackets = audioPackets == null ? new LinkedBlockingDeque<>(500) : audioPackets;
                if (!audioPackets.offer(audioPacket)) {
                    LOGGER.log(Level.WARNING, "Dropped packet from " + packet.identity + " destined for channel " + packet.channel);
                } else {
                    LOGGER.log(Level.INFO, "Received packet from " + packet.identity + " destined for channel " + packet.channel);
                }
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
        private final BiConsumer<UUID, AudioPacket> packetConsumer;
        private final Mixer mixer = new Mixer();

        public ChannelProcessor(ChannelState channel, BiConsumer<UUID, AudioPacket> packetConsumer) {
            this.channel = channel;
            this.packetConsumer = packetConsumer;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    List<AudioPacket> packetList = channel.packetQueues.values().stream()
                            .map(audioPackets -> {
                                System.out.println("before " + audioPackets.size());
                                AudioPacket packet = audioPackets.poll();
                                System.out.println("after " + audioPackets.size());
                                return packet;
                            })
                            .filter(audioPacket -> !Objects.isNull(audioPacket))
                            .collect(Collectors.toList());
                    if (packetList.isEmpty()) {
//                        packetConsumer.accept(channel.id, AudioPacket.SILENCE);
                    } else {
                        AudioPacket mixedPacket = mixer.mix(packetList);
                        packetConsumer.accept(channel.id, mixedPacket);
                    }
                    try {
                        Thread.sleep(20);
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
