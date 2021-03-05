package team.catgirl.vox.client;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import team.catgirl.vox.audio.AudioPacket;
import team.catgirl.vox.audio.Decoder;
import team.catgirl.vox.audio.devices.OutputDevice;
import team.catgirl.vox.protocol.OutgoingVoicePacket;

import java.io.Closeable;
import java.io.IOException;

public class AudioReceiverSocket extends WebSocketListener implements Closeable {

    private final OutputDevice outputDevice;
    private final Decoder decoder = new Decoder();

    public AudioReceiverSocket(OutputDevice outputDevice) {
        this.outputDevice = outputDevice;
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
        try {
            OutgoingVoicePacket packet = new OutgoingVoicePacket(bytes.toByteArray());
            AudioPacket audioPacket = AudioPacket.deserialize(packet.audio);
            byte[] pcm = decoder.decode(audioPacket);
            outputDevice.getLine().write(pcm, 0, pcm.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        decoder.close();
    }
}
