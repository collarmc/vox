package team.catgirl.vox.server.channels;

import team.catgirl.vox.server.http.api.Channel;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChannelService {
    private final ConcurrentMap<UUID, Channel> channels = new ConcurrentHashMap<>();


}
