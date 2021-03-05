package team.catgirl.vox.client;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.catgirl.vox.audio.Mixer;
import team.catgirl.vox.protocol.AudioPacket;
import team.catgirl.vox.audio.Decoder;
import team.catgirl.vox.audio.devices.OutputDevice;
import team.catgirl.vox.protocol.IdentifyPacket;
import team.catgirl.vox.protocol.OutputAudioPacket;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;

public class AudioReceiverSocket extends WebSocketListener implements Closeable {

    private final OutputDevice outputDevice;
    private final UUID identity;
    private final UUID channel;
    private final Decoder decoder = new Decoder();
    private final Mixer mixer = new Mixer();

    public AudioReceiverSocket(OutputDevice outputDevice, UUID identity, UUID channel) {
        this.outputDevice = outputDevice;
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
        IdentifyPacket identifyPacket = new IdentifyPacket(identity, channel);
        try {
            webSocket.send(ByteString.of(identifyPacket.serialize()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
        try {
            OutputAudioPacket packet = new OutputAudioPacket(bytes.toByteArray());
            AudioPacket audioPacket = mixer.mix(packet.streamPackets);
            byte[] pcm = decoder.decode(audioPacket);
            System.out.println("Packet size " + audioPacket.audio.length);
            outputDevice.getLine().write(pcm, 0, pcm.length);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        decoder.close();
        mixer.close();
    }
}
