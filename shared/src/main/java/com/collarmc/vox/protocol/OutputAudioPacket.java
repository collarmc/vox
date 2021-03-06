package com.collarmc.vox.protocol;

import com.collarmc.vox.api.Channel;
import com.collarmc.vox.io.IO;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class OutputAudioPacket {
    private static final int VERSION = 1;
    public final Channel channel;

    /**
     * Audio streams from various clients
     */
    public final List<AudioStreamPacket> streamPackets;

    public OutputAudioPacket(Channel channel, List<AudioStreamPacket> streamPackets) {
        this.channel = channel;
        this.streamPackets = streamPackets;
    }

    public OutputAudioPacket(byte[] bytes) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            try (DataInputStream dataStream = new DataInputStream(inputStream)) {
                int version = dataStream.readInt();
                if (version != VERSION) {
                    throw new IllegalStateException("unknown version " + version);
                }
                channel = new Channel(IO.readUUID(dataStream));
                int packetCount = dataStream.readInt();
                this.streamPackets = new ArrayList<>();
                for (int i = 0; i < packetCount; i++) {
                    streamPackets.add(new AudioStreamPacket(IO.readBytes(dataStream)));
                }
            }
        }
    }

    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (DataOutputStream dataStream = new DataOutputStream(outputStream)) {
                dataStream.writeInt(VERSION);
                IO.writeUUID(dataStream, channel.id);
                dataStream.writeInt(streamPackets.size());
                for (AudioStreamPacket streamPacket : streamPackets) {
                    IO.writeBytes(dataStream, streamPacket.serialize());
                }
            }
            return outputStream.toByteArray();
        }
    }
}
