package com.collarmc.vox.audio.opus;

import com.collarmc.vox.api.Caller;
import com.collarmc.vox.audio.Mixer;
import com.collarmc.vox.protocol.AudioPacket;
import com.collarmc.vox.protocol.AudioStreamPacket;
import com.collarmc.vox.protocol.OutputAudioPacket;
import com.sun.jna.ptr.PointerByReference;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class OpusMixer implements Mixer {

    public static final int BUFFER_SIZE = 1276;

    private final OpusCodec codec;
    private final PointerByReference opusRepacketizerPrt;
    private final ByteBuffer buffer;
    private final CopyOnWriteArraySet<Caller> mutedCallers = new CopyOnWriteArraySet<>();

    public OpusMixer(OpusCodec codec) {
        this.codec = codec;
        this.buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        this.opusRepacketizerPrt = codec.opus_repacketizer_create();
    }

    @Override
    public AudioPacket mix(OutputAudioPacket packets) {
        // If there are no packets then return silence
        List<AudioStreamPacket> streamPackets = packets.streamPackets.stream()
                .filter(packet -> !mutedCallers.contains(packet.owner))
                .filter(packet -> !packet.audio.isEmpty())
                .collect(Collectors.toList());

        if (streamPackets.isEmpty()) {
            System.out.print('s');
            return AudioPacket.SILENCE;
        }
        // If there is just one packet, no need to repacketize
        if (streamPackets.size() == 1) {
            System.out.print('1');
            byte[] bytes = packets.streamPackets.get(0).audio.bytes;
            return AudioPacket.fromEncodedBytes(bytes);
        }

        // Re-packetize multiple streams
        for (AudioStreamPacket packet : streamPackets) {
            System.out.print('r');
            byte[] bytes = packet.audio.bytes;
            int result = codec.opus_repacketizer_cat(opusRepacketizerPrt, bytes, bytes.length);
            OpusCodec.assertOpusError(result);
        }
        int read = codec.opus_repacketizer_out(this.opusRepacketizerPrt, buffer, buffer.capacity());
        OpusCodec.assertOpusError(read);
        buffer.flip();
        byte[] out = new byte[buffer.limit()];
        buffer.get(out);
        return AudioPacket.fromEncodedBytes(out);
    }

    @Override
    public void mute(Caller caller) {
        mutedCallers.add(caller);
    }

    @Override
    public void unMute(Caller caller) {
        mutedCallers.remove(caller);
    }

    @Override
    public boolean isMuted(Caller caller) {
        return mutedCallers.contains(caller);
    }

    @Override
    public void close() {
        codec.opus_repacketizer_destroy(opusRepacketizerPrt);
    }
}
