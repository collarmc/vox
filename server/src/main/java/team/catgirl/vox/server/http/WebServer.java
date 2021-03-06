package team.catgirl.vox.server.http;

import team.catgirl.vox.server.audio.AudioProducerSocket;
import team.catgirl.vox.server.audio.AudioSubscriberSocket;
import team.catgirl.vox.server.channels.Multiplexer;

import static spark.Spark.*;

public class WebServer {
    public void start() {
        port(httpPort());

        AudioProducerSocket producerSocket = new AudioProducerSocket();
        Multiplexer multiplexer = new Multiplexer(producerSocket::sendPackets);
        AudioSubscriberSocket subscriberSocket = new AudioSubscriberSocket(multiplexer);

        webSocket("/api/1/audio/send", subscriberSocket);
        webSocket("/api/1/audio/listen", producerSocket);

        get("/", (request, response) -> "Vox");

        exception(Exception.class, (exception, request, response) -> {
            exception.printStackTrace();
        });
    }

    private static int httpPort() {
        String portValue = System.getenv("PORT");
        return portValue != null ? Integer.parseInt(portValue) : 4000;
    }
}
