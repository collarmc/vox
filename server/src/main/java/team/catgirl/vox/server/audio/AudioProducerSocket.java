package team.catgirl.vox.server.audio;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import team.catgirl.vox.audio.AudioPacket;
import team.catgirl.vox.io.IO;
import team.catgirl.vox.protocol.IdentifyPacket;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@WebSocket
public class AudioProducerSocket {

    private final ConcurrentMap<UUID, Set<Session>> channelSessions = new ConcurrentHashMap<>();

    public void consume(UUID channel, AudioPacket packet) {
        byte[] bytes = packet.serialize();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        Set<Session> sessions = channelSessions.get(channel);
        if (sessions == null) {
            return;
        }
        sessions.forEach(session -> session.getRemote().sendBytesByFuture(byteBuffer));
    }

    @OnWebSocketMessage
    public void receivePacket(Session session, InputStream stream) throws IOException {
        byte[] bytes = IO.toByteArray(stream);
        IdentifyPacket packet = new IdentifyPacket(bytes);
        channelSessions.compute(packet.channel, (channelId, sessions) -> {
            sessions = sessions == null ? new HashSet<>() : sessions;
            sessions.add(session);
            return sessions;
        });
    }

    @OnWebSocketClose
    public void onClose(Session session) {
        // TODO: cleanup
    }
}
