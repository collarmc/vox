package com.collarmc.vox.audio;

import com.collarmc.vox.protocol.AudioStreamPacket;
import com.collarmc.vox.protocol.OutputAudioPacket;
import com.collarmc.vox.api.Caller;
import com.collarmc.vox.protocol.AudioPacket;

import java.io.Closeable;
import java.util.function.Function;

/**
 * Responsible for mixing {@link AudioStreamPacket}'s into a single {@link AudioPacket}
 */
public interface Mixer extends Closeable {
    /**
     * Mixes multiple AudioStreamPacket's into a single AudioPacket
     * @param packets to mix
     * @param transformer to apply to each audio stream packet
     * @return packet ready for playback
     */
    AudioPacket mix(OutputAudioPacket packets, Function<AudioStreamPacket, byte[]> transformer);

    /**
     * Mute a caller
     * @param caller
     */
    void mute(Caller caller);

    /**
     * UnMute a caller
     * @param caller to unMute
     */
    void unMute(Caller caller);

    /**
     * Tests if caller is muted
     * @param caller to test
     * @return muted
     */
    boolean isMuted(Caller caller);
}
