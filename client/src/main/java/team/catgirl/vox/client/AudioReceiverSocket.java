package team.catgirl.vox.client;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.catgirl.vox.api.Caller;
import team.catgirl.vox.api.Channel;
import team.catgirl.vox.audio.opus.OpusMixer;
import team.catgirl.vox.audio.opus.OpusDecoder;
import team.catgirl.vox.security.Cipher;
import team.catgirl.vox.audio.Mixer;
import team.catgirl.vox.protocol.AudioPacket;
import team.catgirl.vox.audio.Decoder;
import team.catgirl.vox.audio.devices.OutputDevice;
import team.catgirl.vox.protocol.IdentifyPacket;
import team.catgirl.vox.protocol.OutputAudioPacket;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AudioReceiverSocket extends WebSocketListener implements Closeable {

    private static final Logger LOGGER = Logger.getLogger(AudioReceiverSocket.class.getName());

    private final OutputDevice outputDevice;
    private final Cipher cipher;
    private final Caller identity;
    private final Channel channel;
    private final Decoder decoder = new OpusDecoder();
    private final Mixer mixer = new OpusMixer();
    private final LinkedBlockingDeque<OutputAudioPacket> packets = new LinkedBlockingDeque<>(Short.MAX_VALUE);
    private final Thread soundPlayer = new Thread(new SoundPlayer());

    public AudioReceiverSocket(OutputDevice outputDevice, Cipher cipher, Caller identity, Channel channel) {
        this.outputDevice = outputDevice;
        this.cipher = cipher;
        this.identity = identity;
        this.channel = channel;
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        t.printStackTrace();
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        super.onOpen(webSocket, response);
        soundPlayer.start();
        IdentifyPacket identifyPacket = new IdentifyPacket(identity, channel);
        try {
            webSocket.send(ByteString.of(identifyPacket.serialize()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        super.onClosing(webSocket, code, reason);
        if (soundPlayer != null && soundPlayer.isAlive()) {
            soundPlayer.interrupt();
        }
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
        OutputAudioPacket packet;
        try {
            packet = new OutputAudioPacket(bytes.toByteArray());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not deserialize OutputAudioPacket", e);
            return;
        }
        packets.offer(packet);
    }

    class SoundPlayer implements Runnable {
        @Override
        public void run() {
            try (SourceDataLine line = outputDevice.getLine()) {
                try {
                    line.open();
                } catch (LineUnavailableException e) {
                    throw new RuntimeException(e);
                }
                line.start();
                while (true) {
                    OutputAudioPacket packet = packets.poll();
                    if (packet == null) {
                        continue;
                    }
                    AudioPacket audioPacket = mixer.mix(packet, streamPacket -> cipher.decrypt(streamPacket.owner, packet.channel, streamPacket.audio.bytes));
                    if (audioPacket.isEmpty()) {
                        continue;
                    }
                    byte[] pcm = decoder.decode(audioPacket, bytes -> bytes);
                    line.write(pcm, 0, pcm.length);
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        decoder.close();
        mixer.close();
    }
}
