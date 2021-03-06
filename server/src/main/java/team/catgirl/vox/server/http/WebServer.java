package team.catgirl.vox.server.http;

import redis.clients.jedis.Jedis;
import sun.rmi.runtime.Log;
import team.catgirl.vox.api.http.ChannelService;
import team.catgirl.vox.server.audio.AudioProducerSocket;
import team.catgirl.vox.server.audio.AudioSubscriberSocket;
import team.catgirl.vox.server.channels.Multiplexer;
import team.catgirl.vox.server.services.ChannelServiceImpl;

import java.util.logging.Level;
import java.util.logging.Logger;

import static spark.Spark.*;

public class WebServer {

    private static final Logger LOGGER = Logger.getLogger(WebServer.class.getName());

    public void start() {
        port(httpPort());
        Jedis jedis = new Jedis();
        ChannelService channels = new ChannelServiceImpl(jedis);

        AudioProducerSocket producerSocket = new AudioProducerSocket();
        Multiplexer multiplexer = new Multiplexer(producerSocket::sendPackets);
        AudioSubscriberSocket subscriberSocket = new AudioSubscriberSocket(multiplexer);

        webSocket("/api/1/audio/send", subscriberSocket);
        webSocket("/api/1/audio/listen", producerSocket);

        get("/", (request, response) -> "Vox");

        exception(Exception.class, (exception, request, response) -> {
            LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
        });
    }

    private static int httpPort() {
        String portValue = System.getenv("PORT");
        return portValue != null ? Integer.parseInt(portValue) : 4000;
    }
}
