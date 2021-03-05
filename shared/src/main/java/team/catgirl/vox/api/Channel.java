package team.catgirl.vox.api;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Channel {
    public final UUID id;
    public final Map<UUID, Caller> participants;

    public Channel(UUID id, Map<UUID, Caller> participants) {
        this.id = id;
        this.participants = participants;
    }

    public Channel addParticipant(Caller caller) {
        HashMap<UUID, Caller> newParticipants = new HashMap<>(participants);
        newParticipants.put(caller.id, caller);
        return new Channel(id, newParticipants);
    }

    public Channel removeParticipant(Caller caller) {
        HashMap<UUID, Caller> newParticipants = new HashMap<>(participants);
        Caller removed = newParticipants.remove(caller.id);
        return removed == null ? this : new Channel(id, newParticipants);
    }
}
