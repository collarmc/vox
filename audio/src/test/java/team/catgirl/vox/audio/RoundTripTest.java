package team.catgirl.vox.audio;

import com.sun.jna.Native;
import org.junit.Ignore;
import org.junit.Test;
import team.catgirl.vox.audio.devices.Devices;
import team.catgirl.vox.audio.devices.InputDevice;
import team.catgirl.vox.audio.devices.OutputDevice;
import team.catgirl.vox.audio.opus.OpusCodec;
import team.catgirl.vox.audio.opus.OpusEncoder;
import team.catgirl.vox.audio.opus.OpusDecoder;
import team.catgirl.vox.audio.opus.OpusSettings;
import team.catgirl.vox.protocol.AudioPacket;

import javax.sound.sampled.*;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URL;


public class RoundTripTest {

    @Test
    @Ignore
    public void roundTrip() throws Exception {
        OpusCodec codec = new OpusCodec();

        // A classy song to play while griefing a base
        URL song = RoundTripTest.class.getClassLoader().getResource("song.wav");
        if (song == null) {
            throw new IOException("could not find song");
        }

        try (AudioInputStream inputStream = AudioSystem.getAudioInputStream(song)) {

            final AudioFormat targetFormat = new AudioFormat(
                    inputStream.getFormat().getEncoding(),
                    OpusSettings.OPUS_SAMPLE_RATE, // target sample rate
                    inputStream.getFormat().getSampleSizeInBits(),
                    inputStream.getFormat().getChannels(),
                    inputStream.getFormat().getFrameSize(),
                    inputStream.getFormat().getFrameRate(), // target frame rate
                    inputStream.getFormat().isBigEndian()
            );

            AudioInputStream resampledStream = AudioSystem.getAudioInputStream(targetFormat, inputStream);

            byte[] bytes = new byte[OpusSettings.OPUS_BUFFER_SIZE];
            try (Encoder encoder = new OpusEncoder(codec)) {
                try (Decoder decoder = new OpusDecoder(codec)) {
                    try (SourceDataLine sourceLine = AudioSystem.getSourceDataLine(targetFormat)) {
                        sourceLine.open();
                        sourceLine.start();
                        while (resampledStream.read(bytes) >= 0) {
                            AudioPacket audioPacket = encoder.encodePacket(bytes, bytes1 -> bytes1);
                            byte[] output = decoder.decode(audioPacket, decoded -> decoded);
                            sourceLine.write(output, 0, output.length);
                        }
                        sourceLine.stop();
                    }
                }
            }
        }
    }

    @Test
    @Ignore
    public void echoTest() throws Exception {
        Devices devices = new Devices();
        InputDevice inputDevice = devices.getDefaultInputDevice();
        System.out.println("Input Device " + inputDevice.getName() + " " + inputDevice.getVendor());
        try (TargetDataLine inputLine = inputDevice.getLine()) {
            inputLine.open();
            inputLine.start();

            byte[] bytes = new byte[inputLine.getBufferSize() / 5];

            OutputDevice outputDevice = devices.getDefaultOutputDevice();
            System.out.println("Output Device " + outputDevice.getName() + " " + outputDevice.getVendor());
            try (SourceDataLine outputLine = outputDevice.getLine()) {
                outputLine.open();
                outputLine.start();
                while (inputLine.read(bytes, 0, bytes.length) >= 0) {
                    outputLine.write(bytes, 0, bytes.length);
                }
            }
        }
    }
}
