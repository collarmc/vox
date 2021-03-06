package com.collarmc.vox.audio;

import com.collarmc.vox.protocol.AudioPacket;

import java.io.Closeable;
import java.util.function.Function;

/**
 * Audio decoder
 */
public interface Decoder extends Closeable {
    /**
     * Decodes Audio Packet into raw PCM data
     * @param packet to decode
     * @param transformer for byte payload
     * @return bytes of PCM data
     */
    byte[] decode(AudioPacket packet, Function<byte[], byte[]> transformer);
}
