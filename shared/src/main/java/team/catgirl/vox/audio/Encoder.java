package team.catgirl.vox.audio;

import club.minnced.opus.util.OpusLibrary;
import com.sun.jna.ptr.PointerByReference;
import tomp2p.opuswrapper.Opus;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static tomp2p.opuswrapper.Opus.OPUS_APPLICATION_AUDIO;

public final class Encoder implements Closeable {

    static {
        try {
            OpusLibrary.loadFromJar();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final int DEFAULT_SAMPLE_RATE = 48000;
    public static final int FRAME_SIZE = 960;
    private static final int MAX_PACKET_SIZE = 3*1276;

    private final PointerByReference encoder;
    private final ByteBuffer BUFFER = ByteBuffer.allocateDirect(MAX_PACKET_SIZE);

    public Encoder() {
        IntBuffer error = IntBuffer.allocate(1);
        encoder = Opus.INSTANCE.opus_encoder_create(DEFAULT_SAMPLE_RATE, 2, OPUS_APPLICATION_AUDIO, error);
        if (error.get() < 0) {
            throw new AudioException("could not create encoder");
        }
    }

    /**
     * Produces an opus audio packet
     * @param buffer of PCM audio
     * @return byte buffer containing opus audio packet
     */
    public AudioPacket encodePacket(ShortBuffer buffer) {
        BUFFER.clear();
        Opus.INSTANCE.opus_encode(encoder, buffer, FRAME_SIZE, BUFFER, MAX_PACKET_SIZE);
        return new AudioPacket(BUFFER.duplicate());
    }

    @Override
    public void close() {
        Opus.INSTANCE.opus_encoder_destroy(encoder);
    }
}
