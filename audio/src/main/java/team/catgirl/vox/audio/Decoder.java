package team.catgirl.vox.audio;

import team.catgirl.vox.protocol.AudioPacket;

import java.io.Closeable;
import java.util.function.Function;

public interface Decoder extends Closeable {
    /**
     * Decodes Audio Packet into raw PCM data
     * @param packet to decode
     * @param transformer for byte payload
     * @return bytes of PCM data
     */
    byte[] decode(AudioPacket packet, Function<byte[], byte[]> transformer);
}
