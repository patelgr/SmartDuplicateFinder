package net.hiralpatel.duplication;

import net.hiralpatel.monitoring.EventPublisher;
import net.hiralpatel.monitoring.Events;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileScanner {
    private static final EventPublisher publisher = EventPublisher.INSTANCE;

    public static Map<Long, List<Path>> getDuplicatesBySize(List<Path> paths) {
        publisher.publishEvent(Events.InfoEvent("Starting duplicate file scan in directory: " + paths));
        Map<Long, List<Path>> filesBySize = new HashMap<>(500_000);
        Predicate<Path> filterOperation = getPathPredicate();

        List<Path> files =  paths.stream().map(p ->walkFileTree(p,filterOperation)).flatMap(List::stream).toList();

        files.forEach(path -> {
            try {
                long size = Files.size(path);
                filesBySize.computeIfAbsent(size, k -> new ArrayList<>()).add(path);
            } catch (IOException e) {
                publisher.publishEvent(Events.InfoEvent("File Access Error: Unable to access file size for " + path + ". Error: " + e.getMessage()));
            }
        });

        publisher.publishEvent(Events.InfoEvent("Duplicate file scan completed. FileCount found: " + filesBySize.size()));
        Map<Long, List<Path>> duplicates = filesBySize.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        publisher.publishEvent(Events.InfoEvent("Duplicate file scan completed. Duplicates found: " + duplicates.size()));

        return duplicates;
    }

    private static Predicate<Path> getPathPredicate() {
        return path -> {
            try {
                String fileName = path.getFileName().toString();
                return !fileName.equals(".DS_Store") &&
                        !path.toString().contains(".Spotlight-V100") &&
                        Files.size(path) > 0;
            } catch (IOException e) {
                publisher.publishEvent(Events.InfoEvent("File Size Access Error: Unable to access size for " + path + ". Error: " + e.getMessage()));
                return false;
            }
        };
    }

    private static List<Path> walkFileTree(Path directory, Predicate<Path> filterOperation) {
        publisher.publishEvent(Events.InfoEvent("Walking file tree starting from directory: " + directory));
        List<Path> collectedPaths = new ArrayList<>();

        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        if (Files.isWritable(file) && Files.isReadable(file) && Files.isRegularFile(file)) {
                            if (filterOperation.test(file)) {
                                collectedPaths.add(file);
                            }
                        }
                    } catch (Exception e) {
                        publisher.publishEvent(Events.InfoEvent("Error processing file: " + file + ". Error: " + e.getMessage()));
                        // Continue processing other files even if an error occurs
                        return FileVisitResult.CONTINUE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    // Log errors but do not terminate
                    publisher.publishEvent(Events.InfoEvent("Failed to visit file: " + file + ". Error: " + exc.getMessage()));
                    return FileVisitResult.CONTINUE;
                }
            });
            publisher.publishEvent(Events.InfoEvent("File tree walk completed. Files collected: " + collectedPaths.size()));
        } catch (IOException e) {
            publisher.publishEvent(Events.InfoEvent("IO Error during file tree walk: " + e.getMessage()));
        }

        return collectedPaths;
    }

}
