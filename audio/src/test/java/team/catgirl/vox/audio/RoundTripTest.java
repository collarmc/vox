package team.catgirl.vox.audio;

import org.junit.Test;
import team.catgirl.vox.protocol.AudioPacket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.io.InputStream;
import java.net.URL;

import static team.catgirl.vox.audio.OpusSettings.OPUS_FRAME_SIZE;
import static team.catgirl.vox.audio.OpusSettings.OPUS_SAMPLE_RATE;


public class RoundTripTest {

    @Test
    public void roundTrip() throws Exception {
        OpusSettings.initializeCodec();

        // A classy song to play while griefing a base
        URL song = RoundTripTest.class.getClassLoader().getResource("song.wav");

//        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(song)) {
        try (InputStream audioStream = song.openStream()) {
            int sampleSize = ((44100 / 16) * 2);
            byte[] bytes = new byte[OPUS_FRAME_SIZE * 4];
            try (Encoder encoder = new Encoder()) {
                try (Decoder decoder = new Decoder()) {
                    AudioFormat format = new AudioFormat(OPUS_SAMPLE_RATE, 16, 2, true, false);
                    try (SourceDataLine sourceLine = AudioSystem.getSourceDataLine(format)) {
                        sourceLine.open();
                        sourceLine.start();
                        while (audioStream.read(bytes) >= 0) {
                            AudioPacket audioPacket = encoder.encodePacket(bytes);
                            byte[] output = decoder.decode(audioPacket);
                            sourceLine.write(output, 0, output.length);
                        }
                        sourceLine.stop();
                    }
                }
            }
        }
    }
}
