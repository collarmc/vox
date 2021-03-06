package team.catgirl.vox.audio;

import team.catgirl.vox.protocol.AudioPacket;

import java.io.Closeable;
import java.util.function.Function;

public interface Decoder extends Closeable {
    byte[] decode(AudioPacket packet, Function<byte[], byte[]> transformer);

    @Override
    void close();
}
