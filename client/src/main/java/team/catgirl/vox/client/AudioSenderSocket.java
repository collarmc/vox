package team.catgirl.vox.client;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import team.catgirl.vox.audio.AudioPacket;
import team.catgirl.vox.audio.Encoder;
import team.catgirl.vox.audio.devices.InputDevice;
import team.catgirl.vox.protocol.IncomingVoicePacket;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static team.catgirl.vox.audio.OpusSettings.OPUS_FRAME_SIZE;

public class AudioSenderSocket extends WebSocketListener implements Closeable {

    private final InputDevice inputDevice;
    private final UUID identity;
    private final UUID channel;
    private final Encoder encoder = new Encoder();
    private Thread worker;

    public AudioSenderSocket(InputDevice inputDevice, UUID identity, UUID channel) {
        this.inputDevice = inputDevice;
        this.identity = identity;
        this.channel = channel;
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        super.onOpen(webSocket, response);
        worker = new Thread(new InputSourceWorker(webSocket));
        worker.start();
    }

    @Override
    public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        super.onClosing(webSocket, code, reason);
        if (worker != null && worker.isAlive()) {
            worker.interrupt();
            worker = null;
        }
    }

    @Override
    public void close() throws IOException {
        encoder.close();
    }

    public class InputSourceWorker implements Runnable {

        private final WebSocket webSocket;

        public InputSourceWorker(WebSocket webSocket) {
            this.webSocket = webSocket;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    byte[] buff = new byte[OPUS_FRAME_SIZE * 4];
                    int read = inputDevice.getLine().read(buff, 0, buff.length);
                    AudioPacket audioPacket;
                    if (read < 0) {
                        audioPacket = AudioPacket.SILENCE;
                    } else {
                        audioPacket = encoder.encodePacket(buff);
                    }
                    IncomingVoicePacket packet = new IncomingVoicePacket(identity, channel, audioPacket.serialize());
                    try {
                        webSocket.send(ByteString.of(packet.serialize()));
                    } catch (IOException e) {
                        throw new IllegalStateException("could not serialize packet");
                    }
                    try {
                        Thread.sleep(TimeUnit.MILLISECONDS.toMillis(50));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
