package team.catgirl.vox.protocol;

import team.catgirl.vox.io.IO;

import java.io.*;

/**
 * A tiny bit of Opus encoded audio
 */
public final class AudioPacket {

    public static AudioPacket SILENCE = new AudioPacket(new byte[0]);

    private static final int VERSION = 1;

    public final byte[] audio;

    private AudioPacket(byte[] audio) {
        this.audio = audio;
    }

    public static AudioPacket fromEncodedBytes(byte[] audio) {
        return new AudioPacket(audio);
    }

    public static AudioPacket deserialize(byte[] bytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            try (DataInputStream dataStream = new DataInputStream(inputStream)) {
                int version = dataStream.readInt();
                if (version != VERSION) {
                    throw new IllegalArgumentException("unknown audio packet version " + version);
                }
                return AudioPacket.fromEncodedBytes(IO.readBytes(dataStream));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("could not deserialize audio packet", e);
        }
    }

    public byte[] serialize() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (DataOutputStream dataStream = new DataOutputStream(outputStream)) {
                dataStream.writeInt(VERSION);
                IO.writeBytes(dataStream, audio);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("could not serialize audio packet", e);
        }
    }

    public boolean isEmpty() {
        return audio.length == 0;
    }
}
