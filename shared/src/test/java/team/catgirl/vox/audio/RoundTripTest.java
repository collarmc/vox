package team.catgirl.vox.audio;

import club.minnced.opus.util.OpusLibrary;
import org.junit.Test;

import javax.sound.sampled.*;
import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import static team.catgirl.vox.audio.Encoder.FRAME_SIZE;

public class RoundTripTest {
    @Test
    public void roundTrip() throws Exception {
        OpusLibrary.loadFromJar();

        URL song = RoundTripTest.class.getClassLoader().getResource("song.wav");

        AudioFormat format = new AudioFormat(48000, 16, 2, true, false);
        SourceDataLine sourceLine = AudioSystem.getSourceDataLine(format);

        sourceLine.start();
        sourceLine.open();

        try (Encoder encoder = new Encoder()) {
            try (Decoder decoder = new Decoder()) {
                byte[] bytes = new byte[FRAME_SIZE];
                try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(song)) {
                    while (audioStream.read(bytes) > 0) {
                        ByteBuffer alloc = ByteBuffer.allocateDirect(bytes.length);
                        alloc.put(bytes);
                        AudioPacket audioPacket = encoder.encodePacket(alloc.asShortBuffer());
                        byte[] rawAudioBytes = decoder.decode(audioPacket);
                        sourceLine.write(rawAudioBytes, 0, rawAudioBytes.length);
                        sourceLine.drain();
                    }
                }
            }
        }

    }
}
