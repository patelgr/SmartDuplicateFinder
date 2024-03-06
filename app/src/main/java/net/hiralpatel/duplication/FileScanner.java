package net.hiralpatel.duplication;

import net.hiralpatel.monitoring.EventPublisher;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileScanner {
    private static final EventPublisher publisher = EventPublisher.INSTANCE;

    public static Map<Long, List<Path>> getDuplicatesBySize(Path commonAncestor, List<Path> filterPaths) {
        Map<Long, List<Path>> filesBySize = new HashMap<>(500_000);
        if (!Files.exists(commonAncestor) || !Files.isDirectory(commonAncestor)) {
            throw new IllegalArgumentException("Common ancestor must be an existing directory");
        }

        Predicate<Path> filterOperation = getPathPredicate();
        List<Path> files =  walkFileTree(commonAncestor,  filterOperation, filterPaths);

        files.forEach(path -> {
            try {
                long size = Files.size(path);
                filesBySize.computeIfAbsent(size, k -> new ArrayList<>()).add(path);
            } catch (IOException e) {
                publisher.publishEvent("File Access Error, Error accessing file: " + path);
            }
        });

        return filesBySize.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Predicate<Path> getPathPredicate() {
        return path -> {
            try {
                String fileName = path.getFileName().toString();
                return !fileName.equals(".DS_Store") &&
                        !path.toString().contains(".Spotlight-V100") &&
//                        Files.size(path) < 1024  * 1024 * 1024 && //128MB
                        Files.size(path) > 0; // 64MB
            } catch (IOException e) {
                return false;
            }
        };
    }

    private static List<Path> walkFileTree(Path directory, Predicate<Path> filterOperation, List<Path> filterPaths) {
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths.parallel()
                    .filter(Files::isWritable)
                    .filter(Files::isReadable)
                    .filter(Files::isRegularFile)
                    .filter(path -> filterPaths.isEmpty() || filterPaths.stream().anyMatch(path::startsWith))
                    .filter(filterOperation)
                    .toList();
        } catch (IOException e) {
            publisher.publishEvent("An IO error occurred: " + e.getMessage());
        }
        return filterPaths;
    }

}
