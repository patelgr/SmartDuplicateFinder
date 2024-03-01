package net.hiralpatel.model.duplicate;

import java.nio.file.Path;
import java.util.List;

public class FileDuplicateItem implements DuplicateItem {
    private final Path filePath;
    private final List<Path> duplicateFiles;

    public FileDuplicateItem(Path filePath, List<Path> duplicateFiles) {
        this.filePath = filePath;
        this.duplicateFiles = duplicateFiles;
    }

    @Override
    public String getIdentifier() {
        // Return the file path as a String to serve as its unique identifier
        return filePath.toString();
    }

    @Override
    public List<Path> getItems() {
        // Return the list of duplicate files
        return duplicateFiles;
    }

}
