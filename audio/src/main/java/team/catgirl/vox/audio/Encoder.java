package team.catgirl.vox.audio;

import com.sun.jna.ptr.PointerByReference;
import tomp2p.opuswrapper.Opus;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public final class Encoder implements Closeable {

    private final PointerByReference encoder;

    public Encoder() {
        IntBuffer error = IntBuffer.allocate(1);
        encoder = Opus.INSTANCE.opus_encoder_create(OpusSettings.OPUS_SAMPLE_RATE, OpusSettings.OPUS_CHANNEL_COUNT, Opus.OPUS_APPLICATION_AUDIO, error);
        AudioException.assertOpusError(error.get());
    }

    /**
     * Produces an opus audio packet
     * @param rawAudio audio
     * @return byte buffer containing opus audio packet
     */
    public AudioPacket encodePacket(byte[] rawAudio) {
        System.out.println(rawAudio.length);
        ByteBuffer nonEncodedBuffer = ByteBuffer.allocateDirect(rawAudio.length);
        nonEncodedBuffer.put(rawAudio);
        nonEncodedBuffer.rewind();
        ByteBuffer encoded = ByteBuffer.allocateDirect(4096);
        int result = Opus.INSTANCE.opus_encode(encoder, nonEncodedBuffer.asShortBuffer(), OpusSettings.OPUS_FRAME_SIZE, encoded, encoded.capacity());
        AudioException.assertOpusError(result);
        return new AudioPacket(encoded);
    }

    @Override
    public void close() {
        Opus.INSTANCE.opus_encoder_destroy(encoder);
    }
}
