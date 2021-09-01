package com.collarmc.vox.protocol;

import com.collarmc.vox.api.Caller;
import com.collarmc.vox.api.Channel;
import com.collarmc.vox.io.IO;

import java.io.*;

/**
 * Sent to the Audio producer socket to identify who we are and what stream we are subscribing to
 */
public final class IdentifyPacket {
    private static final int VERSION = 1;
    public final Caller caller;
    public final Channel channel;
    public final byte[] token;

    public IdentifyPacket(Caller caller, Channel channel, byte[] token) {
        this.caller = caller;
        this.channel = channel;
        this.token = token;
    }

    public IdentifyPacket(byte[] bytes) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            try (DataInputStream dataStream = new DataInputStream(inputStream)) {
                int version = dataStream.readInt();
                if (version != VERSION) {
                    throw new IllegalStateException("unknown version " + version);
                }
                caller = new Caller(IO.readUUID(dataStream));
                channel = new Channel(IO.readUUID(dataStream));
                token = IO.readBytes(dataStream);
            }
        }
    }

    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (DataOutputStream dataStream = new DataOutputStream(outputStream)) {
                dataStream.writeInt(VERSION);
                IO.writeUUID(dataStream, caller.id);
                IO.writeUUID(dataStream, channel.id);
                IO.writeBytes(dataStream, token);
            }
            return outputStream.toByteArray();
        }
    }
}
