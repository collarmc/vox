package team.catgirl.vox.audio;

import team.catgirl.vox.io.IO;

import java.io.*;
import java.nio.ByteBuffer;

public final class AudioPacket {

    public static AudioPacket SILENCE = new AudioPacket(ByteBuffer.allocate(0));

    private static final int VERSION = 1;

    final ByteBuffer audio;

    public AudioPacket(ByteBuffer audio) {
        this.audio = audio;
    }

    public static AudioPacket deserialize(byte[] bytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            try (DataInputStream dataStream = new DataInputStream(inputStream)) {
                int version = dataStream.readInt();
                if (version != VERSION) {
                    throw new IllegalArgumentException("unknown audio packet version " + version);
                }
                byte[] buff = IO.readBytes(dataStream);
                ByteBuffer buffer = ByteBuffer.allocateDirect(buff.length);
                buffer.put(buff);
                return new AudioPacket(buffer);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("could not deserialize audio packet", e);
        }
    }

    public byte[] serialize() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (DataOutputStream dataStream = new DataOutputStream(outputStream)) {
                dataStream.writeInt(VERSION);
                byte[] buff = new byte[audio.limit()];
                audio.position(audio.limit());
                audio.get(buff);
                IO.writeBytes(dataStream, buff);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("could not serialize audio packet", e);
        }
    }
}
