package team.catgirl.vox.server.audio;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import team.catgirl.vox.api.Caller;
import team.catgirl.vox.api.Channel;
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

    private final ConcurrentMap<Channel, Map<Caller, Session>> channelSessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<Session, Channel> sessionToChannelId = new ConcurrentHashMap<>();

    public void sendPackets(Channel channel, List<AudioStreamPacket> packets) {
        Map<Caller, Session> sessions = channelSessions.get(channel);
        if (sessions == null) {
            return;
        }
        sessions.entrySet().parallelStream().forEach(entry -> {
            Caller owner = entry.getKey();
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

    @OnWebSocketClose
    public void onClose(Session session) {
        Channel channel = sessionToChannelId.remove(session);
//        if (channel != null) {
//            channelSessions.compute(channel, (uuid, sessionMap) -> {
////                sessionMap.remove(c
//            });
//        }
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
        sessionToChannelId.putIfAbsent(session, packet.channel);
    }
}
