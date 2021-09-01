package com.collarmc.vox.server.services;

import com.collarmc.vox.api.Caller;
import com.collarmc.vox.api.Channel;
import com.collarmc.vox.api.http.ChannelService;
import com.collarmc.vox.security.TokenGenerator;
import com.google.common.io.BaseEncoding;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.function.Supplier;

public class ReddisChannelServiceImpl implements ChannelService {

    private final Supplier<Jedis> redis;

    public ReddisChannelServiceImpl(Supplier<Jedis> redis) {
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
