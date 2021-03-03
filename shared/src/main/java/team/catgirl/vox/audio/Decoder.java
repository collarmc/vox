package team.catgirl.vox.audio;

import com.sun.jna.ptr.PointerByReference;
import tomp2p.opuswrapper.Opus;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public final class Decoder implements Closeable {
    private static final int DEFAULT_SAMPLE_RATE = 48000;
    private static final int FRAME_SIZE = 960;
    private static final int MAX_PACKET_SIZE = 3*1276;

    private final PointerByReference decoderPtr;
    private final ByteBuffer BUFFER = ByteBuffer.allocateDirect(2*FRAME_SIZE);

    public Decoder() {
        IntBuffer error = IntBuffer.allocate(1);
        decoderPtr = Opus.INSTANCE.opus_decoder_create(DEFAULT_SAMPLE_RATE, 2, error);
        if (error.get() < 0) {
            throw new AudioException("could not create decoder");
        }
    }

    /**
     * Decodes Audio Packet into raw PCM data
     * @param packet to decode
     * @return bytes of PCM data
     */
    public byte[] decode(AudioPacket packet) {
        BUFFER.clear();
        ShortBuffer output = ShortBuffer.allocate(FRAME_SIZE);
        byte[] bytes = packet.buffer.array();
        Opus.INSTANCE.opus_decode(decoderPtr, bytes, bytes.length, output, FRAME_SIZE, 0);
        for (short aShort :output.array()){
            BUFFER.putShort(aShort);
        }
        return BUFFER.array();
    }

    @Override
    public void close() {
        Opus.INSTANCE.opus_decoder_destroy(decoderPtr);
    }
}
