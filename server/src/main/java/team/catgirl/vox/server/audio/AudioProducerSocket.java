package team.catgirl.vox.server.audio;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import team.catgirl.vox.io.IO;
import team.catgirl.vox.protocol.AudioStreamPacket;
import team.catgirl.vox.protocol.IdentifyPacket;
import team.catgirl.vox.protocol.OutputAudioPacket;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@WebSocket
public class AudioProducerSocket {

    private final ConcurrentMap<UUID, Map<UUID, Session>> channelSessions = new ConcurrentHashMap<>();

    public void consume(UUID channel, List<AudioStreamPacket> packets) {
        Map<UUID, Session> sessions = channelSessions.get(channel);
        if (sessions == null) {
            return;
        }
        sessions.entrySet().parallelStream().forEach(entry -> {
            UUID owner = entry.getKey();
            Session session = entry.getValue();
            List<AudioStreamPacket> filtered = packets.stream().filter(streamPacket -> !streamPacket.owner.equals(owner)).collect(Collectors.toList());
            OutputAudioPacket voicePacket = new OutputAudioPacket(channel, filtered);
            byte[] bytes;
            try {
                bytes = voicePacket.serialize();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            session.getRemote().sendBytesByFuture(byteBuffer);
        });
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connected to AudioProducerSocket!");
    }

    @OnWebSocketMessage
    public void receivePacket(Session session, InputStream stream) throws IOException {
        byte[] bytes = IO.toByteArray(stream);
        IdentifyPacket packet = new IdentifyPacket(bytes);
        channelSessions.compute(packet.channel, (channelId, sessions) -> {
            sessions = sessions == null ? new ConcurrentHashMap<>() : sessions;
            sessions.put(packet.owner, session);
            return sessions;
        });
    }
}