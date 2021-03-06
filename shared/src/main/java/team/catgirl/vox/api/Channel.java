package team.catgirl.vox.api;

import java.util.Objects;
import java.util.UUID;

public final class Channel {
    public final UUID id;

    public Channel(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Channel channel = (Channel) o;
        return id.equals(channel.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
