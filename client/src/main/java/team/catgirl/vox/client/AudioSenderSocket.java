package team.catgirl.vox.client;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import team.catgirl.vox.api.Caller;
import team.catgirl.vox.api.Channel;
import team.catgirl.vox.audio.opus.OpusCodec;
import team.catgirl.vox.audio.opus.OpusEncoder;
import team.catgirl.vox.audio.opus.OpusSettings;
import team.catgirl.vox.security.Cipher;
import team.catgirl.vox.protocol.AudioPacket;
import team.catgirl.vox.audio.Encoder;
import team.catgirl.vox.audio.devices.InputDevice;
import team.catgirl.vox.protocol.SourceAudioPacket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static team.catgirl.vox.audio.opus.OpusSettings.OPUS_FRAME_SIZE;

class AudioSenderSocket extends WebSocketListener implements Closeable {

    private static final Logger LOGGER = Logger.getLogger(AudioSenderSocket.class.getName());
    private final InputDevice inputDevice;
    private final Cipher cipher;
    private final Caller caller;
    private final Channel channel;
    private final OpusCodec codec = new OpusCodec();
    private final Encoder encoder = new OpusEncoder(codec);
    private Thread worker;

    public AudioSenderSocket(InputDevice inputDevice, Cipher cipher, Caller caller, Channel channel) {
        this.inputDevice = inputDevice;
        this.cipher = cipher;
        this.caller = caller;
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
                TargetDataLine line = inputDevice.getLine();
                if (!line.isRunning()) {
                    line.open();
                    line.start();
                }

                AudioInputStream inputStream = new AudioInputStream(line);

                final AudioFormat targetFormat = new AudioFormat(
                        inputStream.getFormat().getEncoding(),
                        OpusSettings.OPUS_SAMPLE_RATE, // target sample rate
                        inputStream.getFormat().getSampleSizeInBits(),
                        inputStream.getFormat().getChannels(),
                        inputStream.getFormat().getFrameSize(),
                        inputStream.getFormat().getFrameRate(), // target frame rate
                        inputStream.getFormat().isBigEndian()
                );
                // Sample up or down for OPUS encoding
                try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(targetFormat, inputStream)) {
                    while (true) {
                        byte[] buff = new byte[OpusSettings.OPUS_BUFFER_SIZE];

                        int read = audioStream.read(buff, 0, buff.length);
                        AudioPacket audioPacket;
                        if (read < 0) {
                            audioPacket = AudioPacket.SILENCE;
                        } else {
                            audioPacket = encoder.encodePacket(buff, bytes -> cipher.crypt(caller, channel, bytes));
                        }
                        SourceAudioPacket packet = new SourceAudioPacket(caller, channel, audioPacket);
                        try {
                            webSocket.send(ByteString.of(packet.serialize()));
                        } catch (Throwable e) {
                            LOGGER.log(Level.SEVERE, "could not serialize packet", e);
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
