package team.catgirl.vox.protocol;

import team.catgirl.vox.api.Caller;
import team.catgirl.vox.api.Channel;
import team.catgirl.vox.io.IO;

import java.io.*;
import java.util.UUID;

/**
 * Sent to the Audio producer socket to identify who we are and what stream we are subscribing to
 */
public final class IdentifyPacket {
    private static final int VERSION = 1;
    public final Caller owner;
    public final Channel channel;

    public IdentifyPacket(Caller owner, Channel channel) {
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
                owner = new Caller(IO.readUUID(dataStream));
                channel = new Channel(IO.readUUID(dataStream));
            }
        }
    }

    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (DataOutputStream dataStream = new DataOutputStream(outputStream)) {
                dataStream.writeInt(VERSION);
                IO.writeUUID(dataStream, owner.id);
                IO.writeUUID(dataStream, channel.id);
            }
            return outputStream.toByteArray();
        }
    }
}
