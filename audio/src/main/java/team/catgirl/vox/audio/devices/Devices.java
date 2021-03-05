package team.catgirl.vox.audio.devices;

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
        TargetDataLine line;
        final DataLine.Info info = new DataLine.Info(TargetDataLine.class, OUTPUT_FORMAT);
        if (!AudioSystem.isLineSupported(info)) { return null; }
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
        } catch (final Exception ex) {
            return null;
        }
        refresh();
        return inputDevices.stream()
                .filter(inputDevice -> inputDevice.getLine().getLineInfo().equals(line.getLineInfo()))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return default output device
     */
    public OutputDevice getDefaultOutputDevice() {
        SourceDataLine line;
        final DataLine.Info info = new DataLine.Info(SourceDataLine.class, OUTPUT_FORMAT);
        if (!AudioSystem.isLineSupported(info)) { return null; }
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
        } catch (final Exception ex) {
            return null;
        }
        refresh();
        return outputDevices.stream()
                .filter(outputDevice -> outputDevice.getLine().getLineInfo().equals(line.getLineInfo()))
                .findFirst()
                .orElse(null);
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
                inputDevices.add(new InputDevice(line, mixer.getMixerInfo()));
            } catch (LineUnavailableException|IllegalArgumentException ignored) {}
            try {
                SourceDataLine line = (SourceDataLine) mixer.getLine(outputInfo);
                outputDevices.add(new OutputDevice(line, mixer.getMixerInfo()));
            } catch (LineUnavailableException|IllegalArgumentException ignored) {}
        }

    }
}
