package net.hiralpatel.duplication;

import net.hiralpatel.monitoring.EventPublisher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileScanner {

    public static Map<Long, List<Path>> getDuplicatesBySize(Path commonAncestor, List<Path> filterPaths) {
        EventPublisher publisher = EventPublisher.INSTANCE;
        Map<Long, List<Path>> filesBySize = new HashMap<>();
        if (!Files.exists(commonAncestor) || !Files.isDirectory(commonAncestor)) {
            throw new IllegalArgumentException("Common ancestor must be an existing directory");
        }
        Predicate<Path> isNotDS_Store = path -> !path.getFileName().toString().equals(".DS_Store");

        try (Stream<Path> paths = Files.walk(commonAncestor)) {
            paths.peek(path -> {
                if (Files.isDirectory(path)) {
                    publisher.publishEvent("Directory scanned: " + path.toAbsolutePath());
                }
            }).filter(Files::isRegularFile).filter(path -> filterPaths.stream().anyMatch(path::startsWith)).filter(isNotDS_Store).forEach(path -> {
                try {
                    long size = Files.size(path);
                    filesBySize.computeIfAbsent(size, k -> new java.util.ArrayList<>()).add(path);
                } catch (IOException e) {
                    System.err.println("Error accessing file: " + path);
                }
            });
        } catch (IOException e) {
            System.err.println("Error scanning directory: " + commonAncestor);
        }

        return filesBySize.entrySet().stream().filter(entry -> entry.getValue().size() > 1).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
