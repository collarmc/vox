package team.catgirl.vox.protocol;

import java.io.*;
import java.util.UUID;

/**
 * Sent to the Audio producer socket to identify who we are and what stream we are subscribing to
 */
public final class IdentifyPacket {
    private static final int VERSION = 1;
    public final UUID identity;
    public final UUID channel;

    public IdentifyPacket(UUID identity, UUID channel) {
        this.identity = identity;
        this.channel = channel;
    }

    public IdentifyPacket(byte[] bytes) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            try (DataInputStream dataStream = new DataInputStream(inputStream)) {
                int version = dataStream.readInt();
                if (version != VERSION) {
                    throw new IllegalStateException("unknown version " + version);
                }
                long identityHigh = dataStream.readLong();
                long identityLow = dataStream.readLong();
                identity = new UUID(identityHigh, identityLow);
                long channelHigh = dataStream.readLong();
                long channelLow = dataStream.readLong();
                channel = new UUID(channelHigh, channelLow);
            }
        }
    }

    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (DataOutputStream dataStream = new DataOutputStream(outputStream)) {
                dataStream.writeInt(VERSION);
                dataStream.writeLong(identity.getMostSignificantBits());
                dataStream.writeLong(identity.getLeastSignificantBits());
                dataStream.writeLong(channel.getMostSignificantBits());
                dataStream.writeLong(channel.getLeastSignificantBits());
            }
            return outputStream.toByteArray();
        }
    }
}
