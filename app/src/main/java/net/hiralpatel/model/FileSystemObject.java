package net.hiralpatel.model;

import java.nio.file.Path;

public interface FileSystemObject extends Duplicatable {

    String getName();

    Path getPath();

    long getSize();
}