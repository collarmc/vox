package team.catgirl.vox.server.audio;

import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import team.catgirl.vox.io.IO;
import team.catgirl.vox.protocol.IncomingVoicePacket;
import team.catgirl.vox.server.channels.Multiplexer;

import java.io.IOException;
import java.io.InputStream;

@WebSocket
public class AudioSubscriberSocket {

    private final Multiplexer multiplexer;

    public AudioSubscriberSocket(Multiplexer multiplexer) {
        this.multiplexer = multiplexer;
    }

    @OnWebSocketMessage
    public void receivePacket(InputStream stream) throws IOException {
        byte[] bytes = IO.toByteArray(stream);
        IncomingVoicePacket packet = new IncomingVoicePacket(bytes);
        multiplexer.receive(packet);
    }
}
