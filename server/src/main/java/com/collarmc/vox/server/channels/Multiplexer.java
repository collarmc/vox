package com.collarmc.vox.server.channels;

import com.collarmc.vox.api.Caller;
import com.collarmc.vox.api.Channel;
import com.collarmc.vox.api.http.ChannelService;
import com.collarmc.vox.audio.Mixer;
import com.collarmc.vox.audio.opus.OpusCodec;
import com.collarmc.vox.audio.opus.OpusMixer;
import com.collarmc.vox.protocol.AudioPacket;
import com.collarmc.vox.protocol.AudioStreamPacket;
import com.collarmc.vox.protocol.SourceAudioPacket;

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
        ChannelState channelState = channels.computeIfAbsent(packet.channel, channel -> {
            ChannelState state = new ChannelState(packet.channel);
            Future<?> future = consumerExecutor.submit(new ChannelProcessor(state, packetConsumer));
            channelProcessors.put(state.channel, future);
            return state;
        });
        LinkedBlockingDeque<AudioPacket> audioPackets = channelState.packetQueues.computeIfAbsent(packet.owner, caller -> new LinkedBlockingDeque<>(Short.MAX_VALUE));
        audioPackets.offer(packet.audio);
        System.out.println(audioPackets.size());
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
        private final OpusCodec codec = new OpusCodec();
        private final Mixer mixer = new OpusMixer(codec);

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
                                if (packet == null) {
                                    return null;
                                }
                                return new AudioStreamPacket(entry.getKey(), packet);
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    packetConsumer.accept(state.channel, packetList);
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
