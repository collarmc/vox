package com.collarmc.vox.protocol;

import com.collarmc.vox.api.Caller;
import com.collarmc.vox.api.Channel;
import com.collarmc.vox.io.IO;

import java.io.*;

/**
 * Represents an Audio packet to be sent to subscribers of a specific channel
 */
public final class SourceAudioPacket {
    private static final int VERSION = 1;
    /**
     * Owner of the packet
     */
    public final Caller owner;
    /**
     * Channel the packet is destined to be broadcast to
     */
    public final Channel channel;
    /**
     * Audio to be sent
     */
    public final AudioPacket audio;

    public SourceAudioPacket(Caller owner, Channel channel, AudioPacket audio) {
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
                owner = new Caller(IO.readUUID(dataStream));
                channel = new Channel(IO.readUUID(dataStream));
                audio = AudioPacket.deserialize(IO.readBytes(dataStream));
            }
        }
    }

    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (DataOutputStream dataStream = new DataOutputStream(outputStream)) {
                dataStream.writeInt(VERSION);
                IO.writeUUID(dataStream, owner.id);
                IO.writeUUID(dataStream, channel.id);
                IO.writeBytes(dataStream, audio.serialize());
            }
            return outputStream.toByteArray();
        }
    }
}
