package com.collarmc.vox.client;

import io.mikael.urlbuilder.UrlBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import com.collarmc.vox.api.Caller;
import com.collarmc.vox.api.Channel;
import com.collarmc.vox.security.Cipher;
import com.collarmc.vox.audio.opus.OpusSettings;
import com.collarmc.vox.audio.devices.InputDevice;
import com.collarmc.vox.audio.devices.OutputDevice;

import java.io.Closeable;
import java.io.IOException;

public final class Vox implements Closeable {

    static {
        OpusSettings.initializeCodec();
    }

    private final OkHttpClient http;
    private final UrlBuilder baseUrl;
    private final AudioSenderSocket senderSocket;
    private final AudioReceiverSocket receiverSocket;
    WebSocket audioSenderSocket;
    WebSocket audioReceiverSocket;

    public Vox(OkHttpClient http, String baseUrl, byte[] token, Caller identity, Channel channel, InputDevice inputDevice, OutputDevice outputDevice, Cipher cipher) {
        this.http = http;
        this.baseUrl = UrlBuilder.fromString(baseUrl);
        this.senderSocket = new AudioSenderSocket(inputDevice, cipher, identity, channel);
        this.receiverSocket = new AudioReceiverSocket(outputDevice, cipher, identity, channel, token);
    }

    /**
     * Connects to the Channel and starts streaming
     */
    public void connect() {
        audioReceiverSocket = http.newWebSocket(new Request.Builder().url(baseUrl.withPath("api/1/audio/listen").toUrl()).build(), receiverSocket);
        audioReceiverSocket.request();
        audioSenderSocket = http.newWebSocket(new Request.Builder().url(baseUrl.withPath("api/1/audio/send").toUrl()).build(), senderSocket);
        audioSenderSocket.request();
    }

    /**
     * Disconnects from the channel
     */
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
