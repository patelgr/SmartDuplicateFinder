package net.hiralpatel.model.duplicate;

import java.nio.file.Path;
import java.util.List;

public class DirectoryDuplicateItem implements DuplicateItem {
    private final Path directoryPath;
    private final List<Path> duplicateDirectories;

    public DirectoryDuplicateItem(Path directoryPath, List<Path> duplicateDirectories) {
        this.directoryPath = directoryPath;
        this.duplicateDirectories = duplicateDirectories;
    }

    @Override
    public String getIdentifier() {
        // Return the directory path as a String to serve as its unique identifier
        return directoryPath.toString();
    }

    @Override
    public List<Path> getItems() {
        // Return the list of duplicate directories
        return duplicateDirectories;
    }

    // Additional getters if needed
    public Path getDirectoryPath() {
        return directoryPath;
    }

    public List<Path> getDuplicateDirectories() {
        return duplicateDirectories;
    }
}
