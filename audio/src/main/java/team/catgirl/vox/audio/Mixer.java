package team.catgirl.vox.audio;

import club.minnced.opus.util.OpusLibrary;
import com.sun.jna.ptr.PointerByReference;
import team.catgirl.vox.protocol.AudioPacket;
import team.catgirl.vox.protocol.AudioStreamPacket;
import team.catgirl.vox.protocol.OutputAudioPacket;
import tomp2p.opuswrapper.Opus;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

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

    public AudioPacket mix(OutputAudioPacket packets) {
        // TODO: mix packets instead of just returning the first one
        return packets.streamPackets.stream().findFirst().map(streamPacket -> streamPacket.audio).orElse(AudioPacket.SILENCE);
//        for (AudioPacket packet : packets) {
//            int result = Opus.INSTANCE.opus_repacketizer_cat(opusRepacketizerPrt, packet.audio, packet.audio.length);
//            if (result != Opus.OPUS_OK) {
//                throw new AudioException("could not mix packets");
//            }
//        }
//        ByteBuffer buffer = ByteBuffer.allocateDirect(1276);
//        int size = Opus.INSTANCE.opus_repacketizer_out(this.opusRepacketizerPrt, buffer, 1276);
//        if (size < 0) {
//            throw new AudioException("could not mix audio");
//        }
//        buffer.flip();
//        byte[] out = new byte[buffer.limit()];
//        buffer.get(out);
//        return new AudioPacket(out);
    }

    @Override
    public void close() {
        Opus.INSTANCE.opus_repacketizer_destroy(opusRepacketizerPrt);
    }
}
