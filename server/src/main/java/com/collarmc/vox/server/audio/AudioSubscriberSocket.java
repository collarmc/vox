package com.collarmc.vox.server.audio;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import com.collarmc.vox.io.IO;
import com.collarmc.vox.protocol.SourceAudioPacket;
import com.collarmc.vox.server.channels.Multiplexer;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebSocket
public class AudioSubscriberSocket {

    private static final Logger LOGGER = Logger.getLogger(AudioSubscriberSocket.class.getName());

    private final Multiplexer multiplexer;

    public AudioSubscriberSocket(Multiplexer multiplexer) {
        this.multiplexer = multiplexer;
    }

    @OnWebSocketConnect
    public void onConnected(Session session) {
        LOGGER.log(Level.INFO, "Subscriber connection established");
    }

    @OnWebSocketMessage
    public void receivePacket(Session session, InputStream stream) throws IOException {
        byte[] bytes = IO.toByteArray(stream);
        SourceAudioPacket packet = new SourceAudioPacket(bytes);
        multiplexer.receive(packet);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        LOGGER.log(Level.INFO, "Closing socket. Status: " + statusCode + " Reason: " + reason);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        LOGGER.log(Level.SEVERE, "Socket error", error);
        session.close(1500, "Socket Error");
    }
}
