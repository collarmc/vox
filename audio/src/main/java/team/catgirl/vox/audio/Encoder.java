package team.catgirl.vox.audio;

import team.catgirl.vox.protocol.AudioPacket;

import java.io.Closeable;
import java.util.function.Function;

/**
 * Audio encoder
 */
public interface Encoder extends Closeable {
    /**
     * Produces an opus audio packet
     * @param rawAudio audio
     * @param transformer for byte payload
     * @return byte buffer containing opus audio packet
     */
    AudioPacket encodePacket(byte[] rawAudio, Function<byte[], byte[]> transformer);
}
