package com.collarmc.vox.server.services;

import com.collarmc.vox.api.Caller;
import com.collarmc.vox.api.Channel;
import com.collarmc.vox.api.http.ChannelService;
import com.collarmc.vox.security.TokenGenerator;
import com.google.common.io.BaseEncoding;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryChannelServiceImpl implements ChannelService {

    private final ConcurrentHashMap<String, String> permits = new ConcurrentHashMap<>();

    @Override
    public PermitAccessResponse permit(PermitAccessRequest req) {
            byte[] token = TokenGenerator.byteToken(16);
            permits.put(permitKey(req.channel, req.caller), BaseEncoding.base64().encode(token));
            return new PermitAccessResponse(token);
    }

    @Override
    public DenyAccessResponse deny(DenyAccessRequest req) {
        permits.remove(permitKey(req.channel, req.caller));
        return new DenyAccessResponse();
    }

    @Override
    public boolean isPermitted(Channel channel, Caller caller, byte[] permit) {
        String encodedToken = permits.get(permitKey(channel, caller));
        return encodedToken != null && Arrays.equals(BaseEncoding.base64().decode(encodedToken), permit);
    }

    @Override
    public boolean isPermitted(Channel channel, Caller caller) {
        String encodedToken = permits.get(permitKey(channel, caller));
        return encodedToken != null;
    }

    private static String permitKey(Channel channel, Caller caller) {
        return "vox:channel:permit" + channel.id + ":" + caller.id;
    }
}
