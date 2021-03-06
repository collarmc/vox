package team.catgirl.vox.audio;

import team.catgirl.vox.protocol.AudioPacket;

import java.io.Closeable;
import java.util.function.Function;

public interface Encoder extends Closeable {
    AudioPacket encodePacket(byte[] rawAudio, Function<byte[], byte[]> transformer);

    @Override
    void close();
}
