package net.hiralpatel.duplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DuplicateExtractor {

    /**
     * Extracts duplicate files and directories from a given root directory.
     *
     * @param rootDirectory The root directory to start the search from.
     * @return A map where keys are the sizes of the duplicate items and values are lists of paths to these duplicate items.
     */

//    public static Map<String, List<Path>> extract(Directory rootDirectory) {
//        Map<String, List<Path>> duplicateItems = new HashMap<>();
//        Deque<Directory> directories = new ArrayDeque<>();
//        directories.add(rootDirectory);
//
//        while (!directories.isEmpty()) {
//            Directory directory = directories.poll();
//
//            if (directory.isDuplicate()) {
//                duplicateItems.computeIfAbsent(directory.getHash(), k -> new ArrayList<>()).add(directory.getPath());
//            } else {
//                for (FileSystemObject child : directory.getChildren()) {
//                    switch (child) {
//                        case Directory dir -> directories.add(dir);
//                        case File file when file.isDuplicate() -> {
//                            duplicateItems.computeIfAbsent(file.getHash(), k -> new ArrayList<>()).add(file.getPath());
//                        }
//                        case File file when !(file.isDuplicate()) -> {
//                            // Ignore this
//                        }
//                        default -> {
//                            throw new IllegalArgumentException("Encountered a non-directory, non-duplicate file: " + child.getPath());
//                        }
//                    }
//                }
//            }
//        }
//        return duplicateItems;
//    }
//    public static Map<String, List<Path>> extract(Map<String, List<Path>> duplicateMap) {
//        Map<String, List<Path>> duplicateItems = new HashMap<>();
//
//        // Iterate over each path in the list
//        for (Map.Entry<String, List<Path>> duplicate : duplicateMap.entrySet()) {
//            String hash = duplicate.getKey();
//            List<Path> listOfFiles = duplicate.getValue();
//            for (Path path : listOfFiles) {
//                // Check if the path is a file and not a directory, and then process it
//                if (Files.isRegularFile(path)) {
//                    duplicateItems.computeIfAbsent(hash, k -> new ArrayList<>()).add(path);
//                }
//            }
//        }
//
//        // Filter out entries with only one item, as those are not duplicates
//        return duplicateItems.entrySet().stream()
//                .filter(entry -> entry.getValue().size() > 1)
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//    }
//


}
