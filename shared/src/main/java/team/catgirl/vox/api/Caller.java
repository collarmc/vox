package team.catgirl.vox.api;

import java.util.Objects;
import java.util.UUID;

public final class Caller {
    public final UUID id;

    public Caller(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Caller caller = (Caller) o;
        return id.equals(caller.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
