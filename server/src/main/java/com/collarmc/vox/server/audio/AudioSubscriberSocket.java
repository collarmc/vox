package com.collarmc.vox.server.audio;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
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

    @OnWebSocketMessage
    public void receivePacket(Session session, InputStream stream) throws IOException {
        byte[] bytes = IO.toByteArray(stream);
        SourceAudioPacket packet = new SourceAudioPacket(bytes);
        multiplexer.receive(packet);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        LOGGER.log(Level.SEVERE, "Socket error", error);
        session.close(1500, "Socket Error");
    }
}
