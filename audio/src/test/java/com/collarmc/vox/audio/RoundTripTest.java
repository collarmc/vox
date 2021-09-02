package com.collarmc.vox.audio;

import com.collarmc.vox.audio.devices.InputDevice;
import com.collarmc.vox.audio.devices.OutputDevice;
import com.collarmc.vox.audio.dsp.EchoCanceller;
import com.collarmc.vox.audio.opus.OpusCodec;
import com.collarmc.vox.audio.opus.OpusDecoder;
import com.collarmc.vox.audio.opus.OpusEncoder;
import com.collarmc.vox.audio.opus.OpusSettings;
import com.collarmc.vox.protocol.AudioPacket;
import org.apache.commons.codec.binary.Hex;
import org.junit.Ignore;
import org.junit.Test;
import com.collarmc.vox.audio.devices.Devices;

import javax.sound.sampled.*;
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

    @Test
    public void echoTest() throws Exception {
        Devices devices = new Devices();
        InputDevice inputDevice = devices.getDefaultInputDevice();
        System.out.println("Input Device " + inputDevice.getName() + " " + inputDevice.getVendor());

        OutputDevice outputDevice = devices.getDefaultOutputDevice();
        System.out.println("Output Device " + outputDevice.getName() + " " + outputDevice.getVendor());

        try (TargetDataLine inputLine = inputDevice.getLine()) {
            inputLine.open();
            inputLine.start();

            int filterLength = (int) (inputDevice.getLine().getFormat().getSampleRate());
            EchoCanceller echoCanceller = new EchoCanceller(inputDevice.getLine().getFormat().getFrameSize(), filterLength, inputDevice, outputDevice);

            byte[] bytes = new byte[inputDevice.getLine().getFormat().getFrameSize()];

            try (SourceDataLine outputLine = outputDevice.getLine()) {
                outputLine.open();
                outputLine.start();
                while (inputLine.read(bytes, 0, bytes.length) >= 0) {

                    System.out.println("mic " + Hex.encodeHexString(bytes));
                    byte[] canceled = echoCanceller.processMicrophoneInput(bytes);
                    System.out.println("spk " + Hex.encodeHexString(canceled));
//                    echoCanceller.captureOutputToSoundCard(canceled);
                    outputLine.write(canceled, 0, canceled.length);
                    echoCanceller.captureOutputToSoundCard(canceled);
                }
            }
        }
    }
}
