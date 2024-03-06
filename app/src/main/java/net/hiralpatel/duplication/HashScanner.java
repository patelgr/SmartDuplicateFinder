package net.hiralpatel.duplication;

import net.hiralpatel.analytics.StatisticsCollector;
import net.hiralpatel.hashing.CRC32Hashing;
import net.hiralpatel.hashing.HashingMode;
import net.hiralpatel.hashing.HashingStrategy;
import net.hiralpatel.hashing.SHA256HashingStrategy;
import net.hiralpatel.monitoring.EventPublisher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Scans files to identify duplicates based on their content hashes.
 */
public class HashScanner {
    private static final StatisticsCollector statsCollector = new StatisticsCollector();
    private static final EventPublisher eventPublisher = EventPublisher.INSTANCE;


    public static Map<String, List<Path>> scanForFileDuplicates(Map<Long, List<Path>> filesBySize) {
        eventPublisher.publishEvent("Duplicate scan initiated.");

        // Convert the file size map keys from Long to String for consistency
        Map<String, List<Path>> filesGroupedBySize = convertKeysToString(filesBySize);

        // Step 1: Identify potential duplicates by hashing initial file segments with CRC32
        Map<String, List<Path>> initialSegmentDuplicates = performInitialHashing(filesGroupedBySize);

        // Step 2: Refine the list of potential duplicates by hashing initial segments with SHA256
        Map<String, List<Path>> refinedInitialSegmentDuplicates = refineDuplicatesWithSHA256(initialSegmentDuplicates);

        // Step 3: Confirm duplicates by hashing the full contents of the potential duplicates using CRC32
//        Map<String, List<Path>> confirmedDuplicates = confirmDuplicatesWithCRC32(refinedInitialSegmentDuplicates);

        // Generate a report on the scanning process
        generateDuplicateScanningReport();

        return refinedInitialSegmentDuplicates;
    }

    private static Map<String, List<Path>> convertKeysToString(Map<Long, List<Path>> filesBySize) {
        return filesBySize.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> String.valueOf(entry.getKey()),
                        Map.Entry::getValue
                ));
    }

    private static Map<String, List<Path>> performInitialHashing(Map<String, List<Path>> filesGroupedBySize) {
        HashingStrategy initialHashingStrategy = new CRC32Hashing();
        long totalFiles = filesGroupedBySize.values().stream().mapToLong(Collection::size).sum();
        eventPublisher.publishEvent(String.format("Step 1: Hashing initial segments of %d files using CRC32 to identify potential duplicates.", totalFiles));

        Map<String, List<Path>> initialSegmentDuplicates = identifyDuplicates(filesGroupedBySize, initialHashingStrategy, HashingMode.INITIAL_SEGMENT);
        logDelta("Step 1", totalFiles, initialSegmentDuplicates);

        return initialSegmentDuplicates;
    }

    private static Map<String, List<Path>> refineDuplicatesWithSHA256(Map<String, List<Path>> potentialDuplicates) {
        HashingStrategy refinedHashingStrategy = new SHA256HashingStrategy();
        long totalPotentialDuplicates = potentialDuplicates.values().stream().mapToLong(Collection::size).sum();
        eventPublisher.publishEvent(String.format("Step 2: Refining potential duplicates among %d files using SHA256 on initial segments.", totalPotentialDuplicates));

        Map<String, List<Path>> refinedDuplicates = identifyDuplicates(potentialDuplicates, refinedHashingStrategy, HashingMode.INITIAL_SEGMENT);
        logDelta("Step 2", totalPotentialDuplicates, refinedDuplicates);

        return refinedDuplicates;
    }

    private static Map<String, List<Path>> confirmDuplicatesWithCRC32(Map<String, List<Path>> potentialDuplicates) {
        HashingStrategy initialHashingStrategy = new CRC32Hashing();
        long totalPotentialDuplicates = potentialDuplicates.values().stream().mapToLong(Collection::size).sum();
        eventPublisher.publishEvent(String.format("Step 3: Confirming duplicates among %d files by hashing full file contents with CRC32.", totalPotentialDuplicates));

        Map<String, List<Path>> confirmedDuplicates = identifyDuplicates(potentialDuplicates, initialHashingStrategy, HashingMode.FULL_FILE);
        logDelta("Step 3", totalPotentialDuplicates, confirmedDuplicates);

        return confirmedDuplicates;
    }

    private static void logDelta(String step, long previousCount, Map<String, List<Path>> currentDuplicates) {
        long currentCount = currentDuplicates.values().stream().mapToLong(Collection::size).sum();
        long delta = previousCount - currentCount;
        eventPublisher.publishEvent(String.format("%s Complete: Delta of %d files reduced to %d potential duplicates.", step, delta, currentCount));
    }

    private static void generateDuplicateScanningReport() {
        eventPublisher.publishEvent("Generating duplicate scanning report.");
        statsCollector.generateTabulatedReport(); // Consider passing the finalConfirmedDuplicates map for detailed reporting
    }

    private static Map<String, List<Path>> identifyDuplicates(Map<String, List<Path>> filesGrouped, HashingStrategy hashingStrategy, HashingMode mode) {
        Map<String, List<Path>> duplicateGroups = new HashMap<>(filesGrouped.size());
        filesGrouped.forEach((key, paths) -> {
            if (paths.size() > 1) { // Only consider groups with more than one file
                // hashAndGroupFiles should return a Map where each key is a hash and the value is a list of paths that have that hash
                Map<String, List<Path>> hashedFiles = hashAndGroupFiles(paths, mode, hashingStrategy);
                // Filter hashedFiles to include only those entries with a list size of 2 or more
                hashedFiles.forEach((hash, groupedPaths) -> {
                    if (groupedPaths.size() > 1) {
                        duplicateGroups.put(hash, groupedPaths);
                    }
                });
            }
        });
        return duplicateGroups;
    }


    private static Map<String, List<Path>> hashAndGroupFiles(List<Path> paths, HashingMode mode, HashingStrategy hashingStrategy) {
        Map<String, List<Path>> hashGroups = new HashMap<>(paths.size());
        paths.forEach(path -> {
            String hash = computeFileHash(path, mode, hashingStrategy);
            if (hash != null) {
                hashGroups.computeIfAbsent(hash, k -> new ArrayList<>()).add(path);
            }
        });
        return hashGroups;
    }

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
