package com.collarmc.vox.server.http;

import com.collarmc.vox.server.audio.AudioProducerSocket;
import com.collarmc.vox.server.audio.AudioSubscriberSocket;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import com.collarmc.vox.api.http.ChannelService;
import com.collarmc.vox.api.http.ChannelService.DenyAccessRequest;
import com.collarmc.vox.api.http.ChannelService.PermitAccessRequest;
import com.collarmc.vox.server.channels.Multiplexer;
import com.collarmc.vox.server.http.HttpException.UnauthorisedException;
import com.collarmc.vox.server.services.InMemoryChannelServiceImpl;
import com.collarmc.vox.utils.Utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static spark.Spark.*;

public class WebServer {

    private static final Logger LOGGER = Logger.getLogger(WebServer.class.getName());

    public void start() throws Exception {
        port(httpPort());
        String token = System.getenv("API_TOKEN");
        if (token == null) {
            throw new IllegalStateException("API_TOKEN not set");
        }

//        JedisPool jedisPool = createRedis();

        ObjectMapper mapper = Utils.jsonMapper();
        ChannelService channels = new InMemoryChannelServiceImpl();
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

        path("/api/1/channels", () -> {
            before((request, response) -> {
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
            post("/permit/accept", (request, response) -> {
                PermitAccessRequest req = mapper.readValue(request.body(), PermitAccessRequest.class);
                return channels.permit(req);
            }, model -> Utils.jsonMapper().writeValueAsString(model));
            post("/permit/deny", (request, response) -> {
                DenyAccessRequest req = mapper.readValue(request.body(), DenyAccessRequest.class);
                return channels.deny(req);
            }, model -> Utils.jsonMapper().writeValueAsString(model));
        });

        get("/", (request, response) -> "Vox");
    }

    private JedisPool createRedis() throws URISyntaxException {
//        // https://devcenter.heroku.com/articles/heroku-redis#connecting-in-java
//        // yay....
//        TrustManager bogusTrustManager = new X509TrustManager() {
//            public X509Certificate[] getAcceptedIssuers() {
//                return null;
//            }
//
//            public void checkClientTrusted(X509Certificate[] certs, String authType) {
//            }
//
//            public void checkServerTrusted(X509Certificate[] certs, String authType) {
//            }
//        };
//
//        SSLContext sslContext;
//        try {
//            sslContext = SSLContext.getInstance("SSL");
//        } catch (NoSuchAlgorithmException e) {
//            throw new IllegalStateException(e);
//        }
//        try {
//            sslContext.init(null, new TrustManager[]{ bogusTrustManager }, new SecureRandom());
//        } catch (KeyManagementException e) {
//            throw new IllegalStateException(e);
//        }
//
//        HostnameVerifier bogusHostnameVerifier = (hostname, session) -> true;

        JedisPool jedis;
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(40);
        poolConfig.setMaxIdle(40);
        poolConfig.setBlockWhenExhausted(true);
        String redisUrl = System.getenv("REDIS_URL");
        if (redisUrl == null) {
            jedis = new JedisPool(poolConfig);
        } else {
//            jedis = new JedisPool(poolConfig, new URI(redisUrl), null, null, bogusHostnameVerifier);
            jedis = new JedisPool(poolConfig, new URI(redisUrl));
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
