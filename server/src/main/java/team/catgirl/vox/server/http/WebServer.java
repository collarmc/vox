package team.catgirl.vox.server.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import redis.clients.jedis.Jedis;
import team.catgirl.vox.api.http.ChannelService;
import team.catgirl.vox.api.http.ChannelService.DenyAccessRequest;
import team.catgirl.vox.api.http.ChannelService.PermitAccessRequest;
import team.catgirl.vox.server.audio.AudioProducerSocket;
import team.catgirl.vox.server.audio.AudioSubscriberSocket;
import team.catgirl.vox.server.channels.Multiplexer;
import team.catgirl.vox.server.http.HttpException.UnauthorisedException;
import team.catgirl.vox.server.services.ChannelServiceImpl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static spark.Spark.*;

public class WebServer {

    private static final Logger LOGGER = Logger.getLogger(WebServer.class.getName());

    public void start() throws Exception {
        port(httpPort());
        Jedis jedis = createRedis();
        String token = System.getenv("API_TOKEN");
        if (token == null) {
            throw new IllegalStateException("API_TOKEN not set");
        }
        ObjectMapper mapper = new JsonMapper();
        ChannelService channels = new ChannelServiceImpl(jedis);
        AudioProducerSocket producerSocket = new AudioProducerSocket(channels);
        Multiplexer multiplexer = new Multiplexer(channels, producerSocket::sendPackets);
        AudioSubscriberSocket subscriberSocket = new AudioSubscriberSocket(multiplexer);

        exception(HttpException.class, (e, request, response) -> {
            response.status(e.code);
            try {
                response.body(mapper.writeValueAsString(new ErrorResponse(e.getMessage())));
            } catch (JsonProcessingException jsonProcessingException) {
                throw new RuntimeException(e);
            }
            LOGGER.log(Level.SEVERE, request.pathInfo(), e);
        });
        exception(Exception.class, (e, request, response) -> {
            response.status(500);
            try {
                response.body(mapper.writeValueAsString(new ErrorResponse(e.getMessage())));
            } catch (JsonProcessingException jsonProcessingException) {
                throw new RuntimeException(e);
            }
            LOGGER.log(Level.SEVERE, request.pathInfo(), e);
        });

        webSocket("/api/1/audio/send", subscriberSocket);
        webSocket("/api/1/audio/listen", producerSocket);

        before("/api/1/channels", (request, response) -> {
            String authorization = request.headers("Authorization");
            if (authorization.startsWith("Bearer ")) {
                String tokenString = authorization.substring(authorization.indexOf(" ") + 1);
                if (tokenString.equals(token)) {
                    throw new UnauthorisedException("expired token");
                }
            } else {
                throw new UnauthorisedException("bad authorization header");
            }
        });

        path("/api/1/channels", () -> {
            post("permit/accept", (request, response) -> {
                PermitAccessRequest req = mapper.readValue(request.bodyAsBytes(), PermitAccessRequest.class);
                return channels.permit(req);
            });
            post("permit/deny", (request, response) -> {
                DenyAccessRequest req = mapper.readValue(request.bodyAsBytes(), DenyAccessRequest.class);
                return channels.deny(req);
            });
        });

        get("/", (request, response) -> "Vox");
    }

    private Jedis createRedis() throws URISyntaxException {
        Jedis jedis;
        String redisUrl = System.getenv("REDIS_URL");
        if (redisUrl == null) {
            jedis = new Jedis();
        } else {
            jedis = new Jedis(new URI(redisUrl));
        }
        return jedis;
    }

    private static int httpPort() {
        String portValue = System.getenv("PORT");
        return portValue != null ? Integer.parseInt(portValue) : 4000;
    }

    public static final class ErrorResponse {
        public final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
