package net.hiralpatel.model.filesystem;

import java.nio.file.Path;

public interface FileSystemObject {

    String getName();

    Path getPath();

    long getSize();

    boolean isDuplicate();

    void setDuplicate(boolean duplicate);
}