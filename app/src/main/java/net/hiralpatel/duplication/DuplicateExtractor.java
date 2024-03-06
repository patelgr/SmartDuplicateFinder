package net.hiralpatel.duplication;

import net.hiralpatel.model.filesystem.Directory;
import net.hiralpatel.model.filesystem.File;
import net.hiralpatel.model.filesystem.FileSystemObject;

import java.nio.file.Path;
import java.util.*;

public class DuplicateExtractor {

    /**
     * Extracts duplicate files and directories from a given root directory.
     *
     * @param rootDirectory The root directory to start the search from.
     * @return A map where keys are the sizes of the duplicate items and values are lists of paths to these duplicate items.
     */

    public static Map<String, List<Path>> extract(Directory rootDirectory) {
        Map<String, List<Path>> duplicateItems = new HashMap<>();
        Deque<Directory> directories = new ArrayDeque<>();
        directories.add(rootDirectory);

        while (!directories.isEmpty()) {
            Directory directory = directories.poll();

            if (directory.isDuplicate()) {
                duplicateItems.computeIfAbsent(directory.getHash(), k -> new ArrayList<>()).add(directory.getPath());
            } else {
                for (FileSystemObject child : directory.getChildren()) {
                    switch (child) {
                        case Directory dir -> directories.add(dir);
                        case File file when file.isDuplicate() -> {
                            duplicateItems.computeIfAbsent(file.getHash(), k -> new ArrayList<>()).add(file.getPath());
                        }
                        case File file when !(file.isDuplicate()) -> {
                            // Ignore this
                        }
                        default -> {
                            throw new IllegalArgumentException("Encountered a non-directory, non-duplicate file: " + child.getPath());
                        }
                    }
                }
            }
        }
        return duplicateItems;
    }

}
