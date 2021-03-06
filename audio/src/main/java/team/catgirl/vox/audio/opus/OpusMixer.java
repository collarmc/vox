package team.catgirl.vox.audio.opus;

import com.sun.jna.ptr.PointerByReference;
import team.catgirl.vox.audio.AudioException;
import team.catgirl.vox.audio.Mixer;
import team.catgirl.vox.protocol.AudioPacket;
import team.catgirl.vox.protocol.AudioStreamPacket;
import team.catgirl.vox.protocol.OutputAudioPacket;
import tomp2p.opuswrapper.Opus;

import java.nio.ByteBuffer;
import java.util.function.Function;

public class OpusMixer implements Mixer {

    public static final int BUFFER_SIZE = 1276;

    private final PointerByReference opusRepacketizerPrt;
    private final ByteBuffer buffer;

    public OpusMixer() {
        this.buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        this.opusRepacketizerPrt = Opus.INSTANCE.opus_repacketizer_create();
    }

    @Override
    public AudioPacket mix(OutputAudioPacket packets, Function<AudioStreamPacket, byte[]> transformer) {
        // If there are no packets then return silence
        if (packets.streamPackets.isEmpty()) {
            return AudioPacket.SILENCE;
        }
        // If there is just one packet, no need to repacketize
        if (packets.streamPackets.size() == 1) {
            return packets.streamPackets.get(0).audio;
        }
        // Repacketize multiple streams
        for (AudioStreamPacket packet : packets.streamPackets) {
            byte[] bytes = transformer.apply(packet);
            int result = Opus.INSTANCE.opus_repacketizer_cat(opusRepacketizerPrt, bytes, bytes.length);
            AudioException.assertOpusError(result);
        }
        int read = Opus.INSTANCE.opus_repacketizer_out(this.opusRepacketizerPrt, buffer, buffer.capacity());
        AudioException.assertOpusError(read);
        buffer.flip();
        byte[] out = new byte[buffer.limit()];
        buffer.get(out);
        return AudioPacket.fromEncodedBytes(out);
    }

    @Override
    public void close() {
        Opus.INSTANCE.opus_repacketizer_destroy(opusRepacketizerPrt);
    }
}