package team.catgirl.vox.audio;

import team.catgirl.vox.io.IO;

import java.io.*;
import java.nio.ByteBuffer;

public final class AudioPacket {
    private static final int VERSION = 1;

    final ByteBuffer buffer;

    public AudioPacket(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public AudioPacket(byte[] bytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            try (DataInputStream dataStream = new DataInputStream(inputStream)) {
                int version = dataStream.readInt();
                if (version != VERSION) {
                    throw new IllegalArgumentException("unknown audio packet version " + version);
                }
                buffer = ByteBuffer.wrap(IO.readBytes(dataStream));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("could not deserialize audio packet", e);
        }
    }

    public byte[] serialize() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (DataOutputStream dataStream = new DataOutputStream(outputStream)) {
                dataStream.writeInt(VERSION);
                IO.writeBytes(dataStream, buffer.array());
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("could not serialize audio packet", e);
        }
    }
}
