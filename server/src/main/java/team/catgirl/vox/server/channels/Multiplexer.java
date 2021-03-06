package team.catgirl.vox.server.channels;

import team.catgirl.vox.api.Caller;
import team.catgirl.vox.api.Channel;
import team.catgirl.vox.api.http.ChannelService;
import team.catgirl.vox.audio.opus.OpusMixer;
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
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Multiplexer {

    private static final Logger LOGGER = Logger.getLogger(Multiplexer.class.getName());

    private final ConcurrentMap<Channel, ChannelState> channels = new ConcurrentHashMap<>();
    private final ConcurrentMap<Channel, Future<?>> channelProcessors = new ConcurrentHashMap<>();
    private final ExecutorService consumerExecutor = Executors.newCachedThreadPool();

    private final ChannelService channelService;
    private final BiConsumer<Channel, List<AudioStreamPacket>> packetConsumer;

    public Multiplexer(ChannelService channels, BiConsumer<Channel, List<AudioStreamPacket>> packetConsumer) {
        channelService = channels;
        this.packetConsumer = packetConsumer;
    }

    /**
     * Receive packets for processing
     * @param packet to process
     */
    public void receive(SourceAudioPacket packet) {
        if (!channelService.isPermitted(packet.channel, packet.owner)) {
            return;
        }
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
            channelProcessors.put(channelState.channel, future);
        };
    }

    public void stop(Channel channel) {
        Future<?> removed = channelProcessors.remove(channel);
        if (removed == null){
            return;
        }
        removed.cancel(true);
    }

    private static class ChannelProcessor implements Runnable, Closeable {
        private final ChannelState state;
        private final BiConsumer<Channel, List<AudioStreamPacket>> packetConsumer;
        private final Mixer mixer = new OpusMixer();

        public ChannelProcessor(ChannelState state, BiConsumer<Channel, List<AudioStreamPacket>> packetConsumer) {
            this.state = state;
            this.packetConsumer = packetConsumer;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    List<AudioStreamPacket> packetList = state.packetQueues.entrySet().stream()
                            .map(entry -> {
                                AudioPacket packet = entry.getValue().poll();
                                return new AudioStreamPacket(entry.getKey(), packet == null ? AudioPacket.SILENCE : packet);
                            })
                            .collect(Collectors.toList());
                    packetConsumer.accept(state.channel, packetList);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        packetConsumer.accept(state.channel, null);
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
        public final Channel channel;
        public final ConcurrentMap<Caller, LinkedBlockingDeque<AudioPacket>> packetQueues;

        public ChannelState(Channel channel) {
            this.channel = channel;
            this.packetQueues = new ConcurrentHashMap<>();
        }
    }
}
