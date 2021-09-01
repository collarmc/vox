package com.collarmc.vox.audio.devices;

import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

/**
 * A device that can provide an audio stream
 */
public class InputDevice extends Device<TargetDataLine> {
    public InputDevice(TargetDataLine line, Mixer.Info info) {
        super(line, info);
    }
}
