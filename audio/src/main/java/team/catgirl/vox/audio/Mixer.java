package team.catgirl.vox.audio;

import club.minnced.opus.util.OpusLibrary;
import com.sun.jna.ptr.PointerByReference;
import team.catgirl.vox.protocol.AudioPacket;
import team.catgirl.vox.protocol.AudioStreamPacket;
import team.catgirl.vox.protocol.OutputAudioPacket;
import tomp2p.opuswrapper.Opus;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Mixer implements Closeable {

    public static final int BUFFER_SIZE = 1276;

    private final PointerByReference opusRepacketizerPrt;
    private final ByteBuffer buffer;

    public Mixer() {
        this.buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        this.opusRepacketizerPrt = Opus.INSTANCE.opus_repacketizer_create();
    }

    public AudioPacket mix(OutputAudioPacket packets) {
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
            int result = Opus.INSTANCE.opus_repacketizer_cat(opusRepacketizerPrt, packet.audio.audio, packet.audio.audio.length);
            AudioException.assertOpusError(result);
        }
        int size = Opus.INSTANCE.opus_repacketizer_out(this.opusRepacketizerPrt, buffer, buffer.capacity());
        if (size < 0) {
            throw new AudioException("could not mix audio");
        }
        buffer.flip();
        byte[] out = new byte[buffer.limit()];
        buffer.get(out);
        return AudioPacket.fromEncodedBytes(out);
    }

    @Override
    public void close() {
        Opus.INSTANCE.opus_repacketizer_destroy(opusRepacketizerPrt);
    }

    static {
        try {
            OpusLibrary.loadFromJar();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
