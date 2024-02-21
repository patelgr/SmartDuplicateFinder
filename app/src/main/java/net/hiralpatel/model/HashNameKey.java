package net.hiralpatel.model;

import java.nio.file.Path;
import java.util.Objects;

public final class HashNameKey implements Duplicatable {
    private final String hash;
    private final Path path;

    private boolean duplicate = false;

    @Override
    public boolean isDuplicate() {
        return duplicate;
    }

    @Override
    public void setDuplicate(boolean isDuplicate) {
        this.duplicate = isDuplicate;
    }
    public HashNameKey(String hash, Path path) {
        this.hash = hash;
        this.path = path;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashNameKey that = (HashNameKey) o;
        return hash.equals(that.hash) && path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, path);
    }
}
