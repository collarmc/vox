package team.catgirl.vox.protocol;

import team.catgirl.vox.io.IO;

import java.io.*;
import java.util.UUID;

/**
 * Audio packet coming from a caller
 */
public final class AudioStreamPacket {
    private static final int VERSION = 1;

    public final UUID owner;
    public final AudioPacket audio;

    public AudioStreamPacket(UUID owner, AudioPacket audio) {
        this.owner = owner;
        this.audio = audio;
    }

    public AudioStreamPacket(byte[] bytes) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            try (DataInputStream dataStream = new DataInputStream(inputStream)) {
                int version = dataStream.readInt();
                if (version != VERSION) {
                    throw new IllegalStateException("unknown version " + version);
                }
                owner = IO.readUUID(dataStream);
                audio = new AudioPacket(IO.readBytes(dataStream));
            }
        }
    }

    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (DataOutputStream dataStream = new DataOutputStream(outputStream)) {
                dataStream.writeInt(VERSION);
                IO.writeUUID(dataStream, owner);
                IO.writeBytes(dataStream, audio.serialize());
            }
            return outputStream.toByteArray();
        }
    }
}
