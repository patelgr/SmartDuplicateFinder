package net.hiralpatel.model.duplicate;

import java.nio.file.Path;
import java.util.List;

public interface DuplicateItem {
    String getIdentifier(); // Returns a unique identifier (file path or directory path)

    List<Path> getItems(); // Returns a list of duplicate items (files or directories)
}

