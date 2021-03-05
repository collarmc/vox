package team.catgirl.vox.client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import team.catgirl.vox.audio.OpusSettings;
import team.catgirl.vox.audio.devices.InputDevice;
import team.catgirl.vox.audio.devices.OutputDevice;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;

public final class Vox implements Closeable {

    static {
        OpusSettings.initializeCodec();
    }

    private final OkHttpClient http;
    private final String baseUrl;
    private final AudioSenderSocket senderSocket;
    private final AudioReceiverSocket receiverSocket;
    WebSocket audioSenderSocket;
    WebSocket audioReceiverSocket;

    public Vox(OkHttpClient http, String baseUrl, UUID identity, UUID channel, InputDevice inputDevice, OutputDevice outputDevice) {
        this.http = http;
        this.baseUrl = baseUrl;
        this.senderSocket = new AudioSenderSocket(inputDevice, identity, channel);
        this.receiverSocket = new AudioReceiverSocket(outputDevice, identity, channel);
    }

    public void connect() {
        audioReceiverSocket = http.newWebSocket(new Request.Builder().url(baseUrl + "/api/1/audio/listen").build(), receiverSocket);
        audioReceiverSocket.request();
        audioSenderSocket = http.newWebSocket(new Request.Builder().url(baseUrl + "/api/1/audio/send").build(), senderSocket);
        audioSenderSocket.request();
    }

    public void disconnect() {
        audioReceiverSocket.cancel();
        audioReceiverSocket = null;
        audioSenderSocket.cancel();
        audioSenderSocket = null;
    }

    @Override
    public void close() throws IOException {
        receiverSocket.close();
        senderSocket.close();
        disconnect();
    }
}
