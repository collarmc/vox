package com.collarmc.vox.audio.devices;

import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

/**
 * A device that can emit audio
 */
public class OutputDevice extends Device<SourceDataLine> {
    public OutputDevice(SourceDataLine line, Mixer.Info info) {
        super(line, info);
    }
}
