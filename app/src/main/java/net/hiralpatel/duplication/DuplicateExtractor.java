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
//    public Map<Long, List<Path>> extract(Directory rootDirectory) {
//        Map<Long, List<Path>> duplicates = new HashMap<>();
//        Deque<FileSystemObject> stack = new ArrayDeque<>();
//        stack.push(rootDirectory);
//
//        while (!stack.isEmpty()) {
//            FileSystemObject current = stack.pop();
//
//            if (current instanceof Directory) {
//                Directory dir = (Directory) current;
//                // Check if the directory itself is marked as a duplicate
//                if (dir.isDuplicate()) {
//                    duplicates.computeIfAbsent(dir.getSize(), k -> new ArrayList<>()).add(dir.getPath());
//                }
//                // Add all children to the stack for further processing
//                stack.addAll(dir.getChildren());
//            } else if (current instanceof File) {
//                File file = (File) current;
//                // Check if the file is marked as a duplicate
//                if (file.isDuplicate()) {
//                    duplicates.computeIfAbsent(file.getSize(), k -> new ArrayList<>()).add(file.getPath());
//                }
//            }
//        }
//
//        return duplicates;
//    }
    public static Map<Long, List<Path>> extract(Directory rootDirectory) {
        Map<Long, List<Path>> duplicateItems = new HashMap<>();
        Deque<Directory> directories = new ArrayDeque<>();
        directories.add(rootDirectory);

        while (!directories.isEmpty()) {
            Directory directory = directories.poll();

            if (directory.isDuplicate()) {
                duplicateItems.computeIfAbsent(directory.getSize(), k -> new ArrayList<>()).add(directory.getPath());
            } else {
                for (FileSystemObject child : directory.getChildren()) {
                    switch (child) {
                        case Directory dir -> directories.add(dir);
                        case File file when file.isDuplicate() -> {
                            duplicateItems.computeIfAbsent(file.getSize(), k -> new ArrayList<>()).add(file.getPath());
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
