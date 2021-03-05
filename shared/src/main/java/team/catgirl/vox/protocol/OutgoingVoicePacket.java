package team.catgirl.vox.protocol;

import team.catgirl.vox.io.IO;

import java.io.*;
import java.util.UUID;

public final class OutgoingVoicePacket {
    private static final int VERSION = 1;
    public final UUID channel;
    public final byte[] audio;

    public OutgoingVoicePacket(UUID channel, byte[] audio) {
        this.channel = channel;
        this.audio = audio;
    }

    public OutgoingVoicePacket(byte[] bytes) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            try (DataInputStream dataStream = new DataInputStream(inputStream)) {
                int version = dataStream.readInt();
                if (version != VERSION) {
                    throw new IllegalStateException("unknown version " + version);
                }
                long channelHigh = dataStream.readLong();
                long channelLow = dataStream.readLong();
                channel = new UUID(channelHigh, channelLow);
                audio = IO.readBytes(dataStream);
            }
        }
    }

    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (DataOutputStream dataStream = new DataOutputStream(outputStream)) {
                dataStream.writeInt(VERSION);
                dataStream.writeLong(channel.getMostSignificantBits());
                dataStream.writeLong(channel.getLeastSignificantBits());
                IO.writeBytes(dataStream, audio);
            }
            return outputStream.toByteArray();
        }
    }
}
