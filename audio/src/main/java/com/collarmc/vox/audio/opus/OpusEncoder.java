package com.collarmc.vox.audio.opus;

import com.collarmc.vox.audio.Encoder;
import com.collarmc.vox.audio.Filter;
import com.collarmc.vox.protocol.AudioPacket;
import com.sun.jna.ptr.PointerByReference;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

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

    public AudioPacket encodePacket(byte[] rawAudio) {
        ByteBuffer nonEncodedBuffer = ByteBuffer.allocateDirect(rawAudio.length);
        nonEncodedBuffer.put(rawAudio);
        nonEncodedBuffer.flip();

        ByteBuffer encoded = ByteBuffer.allocateDirect(OpusSettings.OPUS_BUFFER_SIZE);
        int result = codec.opus_encode(encoder, nonEncodedBuffer.asShortBuffer(), OpusSettings.OPUS_FRAME_SIZE, encoded, encoded.capacity());
        OpusCodec.assertOpusError(result);
        byte[] encodedByte = new byte[result];
        encoded.get(encodedByte);
        return AudioPacket.fromEncodedBytes(encodedByte);
    }

    @Override
    public void close() {
        codec.opus_encoder_destroy(encoder);
    }
}
