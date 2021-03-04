package team.catgirl.vox.audio;

import club.minnced.opus.util.OpusLibrary;
import com.sun.jna.ptr.PointerByReference;
import tomp2p.opuswrapper.Opus;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public final class Decoder implements Closeable {

    static {
        try {
            OpusLibrary.loadFromJar();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final PointerByReference decoderPtr;

    public Decoder() {
        IntBuffer error = IntBuffer.allocate(1);
        decoderPtr = Opus.INSTANCE.opus_decoder_create(OpusSettings.OPUS_SAMPLE_RATE, OpusSettings.OPUS_CHANNEL_COUNT, error);
        AudioException.assertOpusError(error.get());
    }

    /**
     * Decodes Audio Packet into raw PCM data
     * @param packet to decode
     * @return bytes of PCM data
     */
    public byte[] decode(AudioPacket packet) {
        ByteBuffer backingBuffer = ByteBuffer.allocateDirect(4096);
        ShortBuffer decoded = backingBuffer.asShortBuffer();
        byte[] encodedAudio = packet.audio;
        int result = Opus.INSTANCE.opus_decode(decoderPtr, encodedAudio, encodedAudio.length, decoded, OpusSettings.OPUS_FRAME_SIZE, 0);
        IntBuffer lastDuration = IntBuffer.allocate(1);
        Opus.INSTANCE.opus_decoder_ctl(decoderPtr, Opus.OPUS_GET_LAST_PACKET_DURATION_REQUEST, lastDuration);
        System.out.println("Last duration " + lastDuration.get());
        AudioException.assertOpusError(result);
        byte[] bytes = new byte[backingBuffer.limit()];
        backingBuffer.get(bytes);
        return bytes;
    }

    @Override
    public void close() {
        Opus.INSTANCE.opus_decoder_destroy(decoderPtr);
    }
}
