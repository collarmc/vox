package team.catgirl.vox.audio;

import org.junit.Test;
import team.catgirl.vox.audio.devices.Devices;
import team.catgirl.vox.audio.devices.InputDevice;
import team.catgirl.vox.audio.devices.OutputDevice;
import team.catgirl.vox.audio.opus.OpusSettings;
import team.catgirl.vox.protocol.AudioPacket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class RoundTripTest {

    @Test
    public void roundTrip() throws Exception {
        OpusSettings.initializeCodec();

        // A classy song to play while griefing a base
        URL song = RoundTripTest.class.getClassLoader().getResource("song.wav");
        if (song == null) {
            throw new IOException("could not find song");
        }

        try (InputStream audioStream = song.openStream()) {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            int sampleSize = (int) ((format.getSampleRate() / format.getSampleSizeInBits()) * format.getChannels());
            byte[] bytes = new byte[sampleSize];
            try (Encoder encoder = new Encoder()) {
                try (Decoder decoder = new Decoder()) {
                    try (SourceDataLine sourceLine = AudioSystem.getSourceDataLine(format)) {
                        sourceLine.open();
                        sourceLine.start();
                        while (audioStream.read(bytes) >= 0) {
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
