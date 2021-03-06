package team.catgirl.vox.audio;

import team.catgirl.vox.protocol.AudioPacket;
import team.catgirl.vox.protocol.AudioStreamPacket;
import team.catgirl.vox.protocol.OutputAudioPacket;

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
}
