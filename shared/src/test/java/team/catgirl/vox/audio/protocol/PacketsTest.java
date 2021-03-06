package team.catgirl.vox.audio.protocol;


import org.junit.Assert;
import org.junit.Test;
import team.catgirl.vox.api.Caller;
import team.catgirl.vox.api.Channel;
import team.catgirl.vox.protocol.AudioPacket;
import team.catgirl.vox.protocol.AudioStreamPacket;
import team.catgirl.vox.protocol.OutputAudioPacket;
import team.catgirl.vox.protocol.SourceAudioPacket;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.UUID;

public class PacketsTest {
    @Test
    public void outputAudioPacket() throws IOException {
        byte[] audioData = new byte[512];
        new SecureRandom().nextBytes(audioData);
        ArrayList<AudioStreamPacket> packets = new ArrayList<>();
        packets.add(new AudioStreamPacket(new Caller(UUID.randomUUID()), AudioPacket.fromEncodedBytes(audioData)));
        packets.add(new AudioStreamPacket(new Caller(UUID.randomUUID()), AudioPacket.fromEncodedBytes(audioData)));
        OutputAudioPacket packet = new OutputAudioPacket(new Channel(UUID.randomUUID()), packets);

        byte[] bytes = packet.serialize();
        OutputAudioPacket newPacket = new OutputAudioPacket(bytes);

        Assert.assertEquals(packet.channel, newPacket.channel);
        Assert.assertEquals(2, packet.streamPackets.size());
        Assert.assertEquals(packet.streamPackets.get(0).owner, packet.streamPackets.get(0).owner);
        Assert.assertArrayEquals(packet.streamPackets.get(0).audio.bytes, packet.streamPackets.get(0).audio.bytes);

        Assert.assertEquals(packet.streamPackets.get(1).owner, packet.streamPackets.get(1).owner);
        Assert.assertArrayEquals(packet.streamPackets.get(1).audio.bytes, packet.streamPackets.get(1).audio.bytes);
    }

    @Test
    public void sourceAudioPacket() throws IOException {
        byte[] audioData = new byte[512];
        new SecureRandom().nextBytes(audioData);
        SourceAudioPacket packet = new SourceAudioPacket(new Caller(UUID.randomUUID()), new Channel(UUID.randomUUID()), AudioPacket.fromEncodedBytes(audioData));
        byte[] bytes = packet.serialize();
        SourceAudioPacket newPacket = new SourceAudioPacket(bytes);
        Assert.assertEquals(packet.owner, newPacket.owner);
        Assert.assertEquals(packet.channel, newPacket.channel);
        Assert.assertArrayEquals(packet.audio.bytes, newPacket.audio.bytes);
    }

    @Test
    public void audioPacket() {
        byte[] audioData = new byte[512];
        new SecureRandom().nextBytes(audioData);
        AudioPacket packet = AudioPacket.fromEncodedBytes(audioData);
        byte[] bytes = packet.serialize();
        AudioPacket newPacket = AudioPacket.deserialize(bytes);
        Assert.assertArrayEquals(packet.bytes, newPacket.bytes);
    }
}
