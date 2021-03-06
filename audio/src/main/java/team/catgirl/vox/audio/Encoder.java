package team.catgirl.vox.audio;

import com.sun.jna.ptr.PointerByReference;
import team.catgirl.vox.audio.opus.OpusSettings;
import team.catgirl.vox.protocol.AudioPacket;
import tomp2p.opuswrapper.Opus;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Function;

import static tomp2p.opuswrapper.Opus.*;

/**
 * Audio encoder
 */
public final class Encoder implements Closeable {

    private final PointerByReference encoder;

    public Encoder() {
        IntBuffer error = IntBuffer.allocate(1);
        encoder = Opus.INSTANCE.opus_encoder_create(OpusSettings.OPUS_SAMPLE_RATE, OpusSettings.OPUS_CHANNEL_COUNT, OPUS_APPLICATION_AUDIO, error);
        AudioException.assertOpusError(error.get());
    }

    /**
     * Produces an opus audio packet
     * @param rawAudio audio
     * @param transformer for byte payload
     * @return byte buffer containing opus audio packet
     */
    public AudioPacket encodePacket(byte[] rawAudio, Function<byte[], byte[]> transformer) {
        ByteBuffer nonEncodedBuffer = ByteBuffer.allocateDirect(rawAudio.length);
        nonEncodedBuffer.put(rawAudio);
        nonEncodedBuffer.flip();
        ByteBuffer encoded = ByteBuffer.allocateDirect(4096);
        int result = Opus.INSTANCE.opus_encode(encoder, nonEncodedBuffer.asShortBuffer(), OpusSettings.OPUS_FRAME_SIZE, encoded, encoded.capacity());
        AudioException.assertOpusError(result);
        byte[] encodedByte = new byte[result];
        encoded.get(encodedByte);
        return AudioPacket.fromEncodedBytes(transformer.apply(encodedByte));
    }

    @Override
    public void close() {
        Opus.INSTANCE.opus_encoder_destroy(encoder);
    }
}
