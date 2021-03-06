package team.catgirl.vox.protocol;

import team.catgirl.vox.io.IO;

import java.io.*;
import java.util.UUID;

/**
 * Sent to the Audio producer socket to identify who we are and what stream we are subscribing to
 */
public final class IdentifyPacket {
    private static final int VERSION = 1;
    public final UUID owner;
    public final UUID channel;

    public IdentifyPacket(UUID owner, UUID channel) {
        this.owner = owner;
        this.channel = channel;
    }

    public IdentifyPacket(byte[] bytes) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            try (DataInputStream dataStream = new DataInputStream(inputStream)) {
                int version = dataStream.readInt();
                if (version != VERSION) {
                    throw new IllegalStateException("unknown version " + version);
                }
                owner = IO.readUUID(dataStream);
                channel = IO.readUUID(dataStream);
            }
        }
    }

    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (DataOutputStream dataStream = new DataOutputStream(outputStream)) {
                dataStream.writeInt(VERSION);
                IO.writeUUID(dataStream, owner);
                IO.writeUUID(dataStream, channel);
            }
            return outputStream.toByteArray();
        }
    }
}
