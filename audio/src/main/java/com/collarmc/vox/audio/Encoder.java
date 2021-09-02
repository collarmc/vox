package com.collarmc.vox.audio;

import com.collarmc.vox.protocol.AudioPacket;

import java.io.Closeable;
import java.util.function.Function;

/**
 * Audio encoder
 */
public interface Encoder extends Closeable {
    /**
     * Produces an opus audio packet
     * @param rawAudio audio
     * @return byte buffer containing opus audio packet
     */
    AudioPacket encodePacket(byte[] rawAudio);
}
