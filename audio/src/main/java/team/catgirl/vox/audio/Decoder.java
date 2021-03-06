package team.catgirl.vox.audio;

import com.sun.jna.ptr.PointerByReference;
import team.catgirl.vox.audio.opus.OpusSettings;
import team.catgirl.vox.protocol.AudioPacket;
import tomp2p.opuswrapper.Opus;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.function.Function;

/**
 * Audio decoder
 */
public final class Decoder implements Closeable {

    private final PointerByReference decoderPtr;

    public Decoder() {
        IntBuffer error = IntBuffer.allocate(1);
        decoderPtr = Opus.INSTANCE.opus_decoder_create(OpusSettings.OPUS_SAMPLE_RATE, OpusSettings.OPUS_CHANNEL_COUNT, error);
        AudioException.assertOpusError(error.get());
    }

    /**
     * Decodes Audio Packet into raw PCM data
     * @param packet to decode
     * @param transformer for byte payload
     * @return bytes of PCM data
     */
    public byte[] decode(AudioPacket packet, Function<byte[], byte[]> transformer) {
        ByteBuffer backingBuffer = ByteBuffer.allocateDirect(4096);
        ShortBuffer decoded = backingBuffer.asShortBuffer();
        int result = Opus.INSTANCE.opus_decode(decoderPtr, packet.bytes, packet.bytes.length, decoded, OpusSettings.OPUS_FRAME_SIZE, 0);
        AudioException.assertOpusError(result);
        byte[] bytes = new byte[backingBuffer.limit()];
        backingBuffer.get(bytes);
        return transformer.apply(bytes);
    }

    @Override
    public void close() {
        Opus.INSTANCE.opus_decoder_destroy(decoderPtr);
    }
}
