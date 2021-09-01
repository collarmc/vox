package team.catgirl.vox.audio.opus;

import com.sun.jna.ptr.PointerByReference;
import team.catgirl.vox.audio.Encoder;
import team.catgirl.vox.protocol.AudioPacket;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Function;

import static tomp2p.opuswrapper.Opus.OPUS_APPLICATION_AUDIO;

public final class OpusEncoder implements Encoder {

    private final OpusCodec codec;
    private final PointerByReference encoder;

    public OpusEncoder(OpusCodec codec) {
        this.codec = codec;
        IntBuffer error = IntBuffer.allocate(1);
        encoder = codec.opus_encoder_create(OpusSettings.OPUS_SAMPLE_RATE, OpusSettings.OPUS_CHANNEL_COUNT, OPUS_APPLICATION_AUDIO, error);
        OpusCodec.assertOpusError(error.get());
    }

    public AudioPacket encodePacket(byte[] rawAudio, Function<byte[], byte[]> transformer) {
        ByteBuffer nonEncodedBuffer = ByteBuffer.allocateDirect(rawAudio.length);
        nonEncodedBuffer.put(rawAudio);
        nonEncodedBuffer.limit(nonEncodedBuffer.position());
        nonEncodedBuffer.position(0);

        ByteBuffer encoded = ByteBuffer.allocateDirect(4096);
        int result = codec.opus_encode(encoder, nonEncodedBuffer.asShortBuffer(), OpusSettings.OPUS_FRAME_SIZE, encoded, encoded.capacity());
        OpusCodec.assertOpusError(result);
        byte[] encodedByte = new byte[result];
        encoded.get(encodedByte);
        return AudioPacket.fromEncodedBytes(transformer.apply(encodedByte));
    }

    @Override
    public void close() {
        codec.opus_encoder_destroy(encoder);
    }
}
