package team.catgirl.vox.audio.opus;

import com.sun.jna.ptr.PointerByReference;
import team.catgirl.vox.audio.Decoder;
import team.catgirl.vox.protocol.AudioPacket;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.function.Function;

public final class OpusDecoder implements Decoder {

    private final OpusCodec codec;
    private final PointerByReference decoderPtr;

    public OpusDecoder(OpusCodec codec) {
        this.codec = codec;
        IntBuffer error = IntBuffer.allocate(1);
        decoderPtr = codec.opus_decoder_create(OpusSettings.OPUS_SAMPLE_RATE, OpusSettings.OPUS_CHANNEL_COUNT, error);
        OpusCodec.assertOpusError(error.get());
    }

    @Override
    public byte[] decode(AudioPacket packet, Function<byte[], byte[]> transformer) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(OpusSettings.OPUS_BUFFER_SIZE);
        ShortBuffer decoded = buffer.asShortBuffer();
        int result = codec.opus_decode(decoderPtr, packet.bytes, packet.bytes.length, decoded, OpusSettings.OPUS_FRAME_SIZE, 0);
        OpusCodec.assertOpusError(result);
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        return transformer.apply(bytes);
    }

    @Override
    public void close() {
        codec.opus_decoder_destroy(decoderPtr);
    }
}
