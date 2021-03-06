package com.collarmc.vox.audio.devices;

import com.collarmc.vox.audio.AudioException;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

public final class Devices {

    private static final AudioFormat OUTPUT_FORMAT = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000.0F, 16, 1, 2, 16000.0F, false);
    private static final AudioFormat INPUT_FORMAT = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000.0F, 16, 1, 2, 16000.0F, false);

    private final List<InputDevice> inputDevices = new ArrayList<>();
    private final List<OutputDevice> outputDevices = new ArrayList<>();

    public Devices() {
        refresh();
    }

    /**
     * @return input devices
     */
    public List<InputDevice> getInputDevices() {
        refresh();
        return inputDevices;
    }

    /**
     * @return output devices
     */
    public List<OutputDevice> getOutputDevices() {
        refresh();
        return outputDevices;
    }

    /**
     * @return default input device
     */
    public InputDevice getDefaultInputDevice() {
        refresh();
        return inputDevices.stream()
                .findFirst()
                .orElseThrow(() -> new AudioException("input device not found"));
    }

    /**
     * @return default output device
     */
    public OutputDevice getDefaultOutputDevice() {
        refresh();
        return outputDevices.stream()
                .findFirst()
                .orElseThrow(() -> new AudioException("output device not found"));
    }

    /**
     * Refresh all devices available
     */
    public void refresh() {
        inputDevices.clear();
        outputDevices.clear();
        final Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        DataLine.Info inputInfo = new DataLine.Info(TargetDataLine.class, INPUT_FORMAT);
        DataLine.Info outputInfo = new DataLine.Info(SourceDataLine.class, OUTPUT_FORMAT);
        for (final Mixer.Info info : mixers) {
            final Mixer mixer = AudioSystem.getMixer(info);
            try {
                TargetDataLine line = (TargetDataLine) mixer.getLine(inputInfo);
                if (line != null) {
                    inputDevices.add(new InputDevice(line, mixer.getMixerInfo()));
                }
            } catch (LineUnavailableException|IllegalArgumentException ignored) {}
            try {
                SourceDataLine line = (SourceDataLine) mixer.getLine(outputInfo);
                if (line != null) {
                    outputDevices.add(new OutputDevice(line, mixer.getMixerInfo()));
                }
            } catch (LineUnavailableException|IllegalArgumentException ignored) {}
        }

    }
}
