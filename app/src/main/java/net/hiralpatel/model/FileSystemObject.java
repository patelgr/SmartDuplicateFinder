package net.hiralpatel.model;

import java.nio.file.Path;

public interface FileSystemObject {

    String getName();

    Path getPath();

    long getSize();



}