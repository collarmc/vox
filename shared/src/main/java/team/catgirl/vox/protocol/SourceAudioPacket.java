package team.catgirl.vox.protocol;

import team.catgirl.vox.io.IO;

import java.io.*;
import java.util.UUID;

/**
 * Represents an Audio packet to be sent to subscribers of a specific channel
 */
public final class SourceAudioPacket {
    private static final int VERSION = 1;
    /**
     * Owner of the packet
     */
    public final UUID owner;
    /**
     * Channel the packet is destined to be broadcast to
     */
    public final UUID channel;
    /**
     * Audio to be sent
     */
    public final AudioPacket audio;

    public SourceAudioPacket(UUID owner, UUID channel, AudioPacket audio) {
        this.owner = owner;
        this.channel = channel;
        this.audio = audio;
    }

    public SourceAudioPacket(byte[] bytes) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            try (DataInputStream dataStream = new DataInputStream(inputStream)) {
                int version = dataStream.readInt();
                if (version != VERSION) {
                    throw new IllegalStateException("unknown version " + version);
                }
                owner = IO.readUUID(dataStream);
                channel = IO.readUUID(dataStream);
                audio = new AudioPacket(IO.readBytes(dataStream));
            }
        }
    }

    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (DataOutputStream dataStream = new DataOutputStream(outputStream)) {
                dataStream.writeInt(VERSION);
                IO.writeUUID(dataStream, owner);
                IO.writeUUID(dataStream, channel);
                IO.writeBytes(dataStream, audio.serialize());
            }
            return outputStream.toByteArray();
        }
    }
}
