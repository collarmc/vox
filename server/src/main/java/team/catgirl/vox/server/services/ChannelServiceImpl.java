package team.catgirl.vox.server.services;

import com.google.common.io.BaseEncoding;
import redis.clients.jedis.Jedis;
import team.catgirl.vox.api.Caller;
import team.catgirl.vox.api.Channel;
import team.catgirl.vox.api.http.ChannelService;
import team.catgirl.vox.security.TokenGenerator;

import java.util.Arrays;
import java.util.function.Supplier;

public class ChannelServiceImpl implements ChannelService {

    private final Supplier<Jedis> redis;

    public ChannelServiceImpl(Supplier<Jedis> redis) {
        this.redis = redis;
    }

    @Override
    public PermitAccessResponse permit(PermitAccessRequest req) {
        try (Jedis jedis = redis.get()) {
            byte[] token = TokenGenerator.byteToken(16);
            jedis.set(permitKey(req.channel, req.caller), BaseEncoding.base64().encode(token));
            return new PermitAccessResponse(token);
        }
    }

    @Override
    public DenyAccessResponse deny(DenyAccessRequest req) {
        try (Jedis jedis = redis.get()) {
            jedis.del(permitKey(req.channel, req.caller));
            return new DenyAccessResponse();
        }
    }

    @Override
    public boolean isPermitted(Channel channel, Caller caller, byte[] permit) {
        try (Jedis jedis = redis.get()) {
            String encodedToken = jedis.get(permitKey(channel, caller));
            return encodedToken != null && Arrays.equals(BaseEncoding.base64().decode(encodedToken), permit);
        }
    }

    @Override
    public boolean isPermitted(Channel channel, Caller caller) {
        try (Jedis jedis = redis.get()) {
            String encodedToken = jedis.get(permitKey(channel, caller));
            return encodedToken != null;
        }
    }

    private static String permitKey(Channel channel, Caller caller) {
        return "vox:channel:permit" + channel.id + ":" + caller.id;
    }
}
