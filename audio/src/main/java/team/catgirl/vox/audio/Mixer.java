package team.catgirl.vox.audio;

import club.minnced.opus.util.OpusLibrary;
import com.sun.jna.ptr.PointerByReference;
import tomp2p.opuswrapper.Opus;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static tomp2p.opuswrapper.Opus.OPUS_OK;

public class Mixer implements Closeable {

    static {
        try {
            OpusLibrary.loadFromJar();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final PointerByReference opusRepacketizerPrt;

    public Mixer() {
        this.opusRepacketizerPrt = Opus.INSTANCE.opus_repacketizer_create();
    }

    public AudioPacket mix(List<AudioPacket> packets) {
        for (AudioPacket packet : packets) {
            packet.audio.rewind();
            byte[] buff = new byte[packet.audio.remaining()];
            packet.audio.get(buff);
            int result = Opus.INSTANCE.opus_repacketizer_cat(opusRepacketizerPrt, buff, buff.length);
            if (result != Opus.OPUS_OK) {
                throw new AudioException("could not mix packets");
            }
        }
        ByteBuffer buffer = ByteBuffer.allocateDirect(1276);
        int size = Opus.INSTANCE.opus_repacketizer_out(this.opusRepacketizerPrt, buffer, 1276);
        if (size < 0) {
            throw new AudioException("could not mix audio");
        }
        buffer.flip();
        return new AudioPacket(buffer);
    }

    @Override
    public void close() {
        Opus.INSTANCE.opus_repacketizer_destroy(opusRepacketizerPrt);
    }
}
