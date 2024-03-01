package net.hiralpatel.duplication;

import net.hiralpatel.analytics.StatisticsCollector;
import net.hiralpatel.hashing.AdaptiveHashing;
import net.hiralpatel.hashing.HashingMode;
import net.hiralpatel.hashing.HashingStrategy;
import net.hiralpatel.monitoring.EventPublisher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Scans files to identify duplicates based on their content hashes.
 */
public class HashScanner {
    private static final StatisticsCollector statsCollector = new StatisticsCollector();
    private static final EventPublisher eventPublisher = EventPublisher.INSTANCE;

    /**
     * Scans for duplicate files using a specified hashing strategy.
     * Initially computes hashes for segments of files to quickly identify potential duplicates,
     * then confirms duplicates by hashing entire file contents.
     *
     * @param filesBySize Map of files grouped by size, as potential duplicates are likely to have the same size.
     * @return Map of file hashes to lists of paths that have identical content.
     */
    public static Map<String, List<Path>> scanForFileDuplicates(Map<Long, List<Path>> filesBySize) {
        eventPublisher.publishEvent("scanForFileDuplicates Started");
        final HashingStrategy hashingStrategy = new AdaptiveHashing();

        eventPublisher.publishEvent("scanForFileDuplicates Converting Long to String Map");
        Map<String, List<Path>> filesGroupedBySize = filesBySize.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> String.valueOf(entry.getKey()),
                        Map.Entry::getValue
                ));

        // Compute hashes for initial segments of the files
        eventPublisher.publishEvent("scanForFileDuplicates identifyDuplicates HashingMode.INITIAL_SEGMENT");
        Map<String, List<Path>> potentialDuplicateHashes = identifyDuplicates(filesGroupedBySize, hashingStrategy, HashingMode.INITIAL_SEGMENT);

        // Confirm duplicates by hashing the full contents of the files
        eventPublisher.publishEvent("scanForFileDuplicates identifyDuplicates HashingMode.FULL_FILE");
        Map<String, List<Path>> confirmedDuplicateHashes = identifyDuplicates(potentialDuplicateHashes, hashingStrategy, HashingMode.FULL_FILE);

        // Generate a report on the scanning process
        eventPublisher.publishEvent("scanForFileDuplicates generateTabulatedReport");
        statsCollector.generateTabulatedReport();

        return confirmedDuplicateHashes;
    }

    /**
     * Identifies potential duplicate files based on hashing mode and groups them by hash.
     *
     * @param filesGrouped    Map of file groupings to process.
     * @param hashingStrategy Strategy to use for computing file hashes.
     * @param mode            Hashing mode to apply (initial segment or full file).
     * @return Map of file hashes to lists of paths with identical hashes.
     */
    private static Map<String, List<Path>> identifyDuplicates(Map<String, List<Path>> filesGrouped, HashingStrategy hashingStrategy, HashingMode mode) {
        Map<String, List<Path>> duplicateGroups = new HashMap<>();
        filesGrouped.forEach((key, paths) -> {
            if (paths.size() > 1) { // Only consider groups with more than one file
                duplicateGroups.putAll(hashAndGroupFiles(paths, mode, hashingStrategy));
            }
        });
        return duplicateGroups;
    }

    /**
     * Hashes a list of files using the provided hashing strategy and mode, and groups them by hash.
     *
     * @param paths           List of file paths to hash.
     * @param mode            Hashing mode (initial segment or full file).
     * @param hashingStrategy Strategy for computing file hashes.
     * @return Map of file hashes to lists of paths with that hash.
     */
    private static Map<String, List<Path>> hashAndGroupFiles(List<Path> paths, HashingMode mode, HashingStrategy hashingStrategy) {
        Map<String, List<Path>> hashGroups = new HashMap<>();
        paths.forEach(path -> {
            String hash = computeFileHash(path, mode, hashingStrategy);
            if (hash != null) {
                hashGroups.computeIfAbsent(hash, k -> new ArrayList<>()).add(path);
            }
        });
        return hashGroups;
    }

    /**
     * Computes the hash of a file based on the provided hashing mode and strategy.
     *
     * @param path            Path of the file to hash.
     * @param mode            Hashing mode (initial segment or full file).
     * @param hashingStrategy Strategy for computing the file hash.
     * @return The computed hash as a String, or null if an error occurs.
     */
    private static String computeFileHash(Path path, HashingMode mode, HashingStrategy hashingStrategy) {
        long startTime = System.nanoTime();
        String hash = null;

        try {
            long fileSize = Files.size(path);
            long sizeToRead = mode.determineSizeToRead(fileSize);
            hash = hashingStrategy.hashFileBySize(path, sizeToRead);
        } catch (IOException e) {
            System.err.println("Error computing hash for " + path + ": " + e.getMessage());
        } finally {
            long endTime = System.nanoTime();
            long processingTime = endTime - startTime;
            String fileType = "Unknown";
            try {
                fileType = Files.probeContentType(path);
            } catch (IOException e) {
                // Log or handle the exception if necessary
            }
            // Collect statistics about the file processing
            statsCollector.addFileStats(path, fileType, processingTime);
        }

        return hash;
    }
}
