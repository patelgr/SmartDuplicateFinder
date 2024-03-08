package net.hiralpatel.duplication;

import net.hiralpatel.analytics.StatisticsCollector;
import net.hiralpatel.hashing.CRC32Hashing;
import net.hiralpatel.hashing.HashingMode;
import net.hiralpatel.hashing.HashingStrategy;
import net.hiralpatel.hashing.SHA256HashingStrategy;
import net.hiralpatel.monitoring.EventPublisher;
import net.hiralpatel.monitoring.Events;

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
        eventPublisher.publishEvent(Events.InfoEvent("Starting duplicate file scan. Number of file groups to process: " + filesBySize.size()));

        // Convert the file size map keys from Long to String for consistency
        Map<String, List<Path>> filesGroupedBySize = convertKeysToString(filesBySize);

        // Step 1: Identify potential duplicates by hashing initial file segments with CRC32
        Map<String, List<Path>> initialSegmentDuplicates = performInitialHashing(filesGroupedBySize);

        // Step 2: Refine the list of potential duplicates by hashing initial segments with SHA256
        Map<String, List<Path>> refinedInitialSegmentDuplicates = refineDuplicatesWithSHA256(initialSegmentDuplicates);

        // Step 3: Confirm duplicates by hashing the full contents of the potential duplicates using CRC32
        Map<String, List<Path>> confirmedDuplicates = confirmDuplicatesWithCRC32(refinedInitialSegmentDuplicates);

        // Generate a report on the scanning process
        generateDuplicateScanningReport();
        eventPublisher.publishEvent(Events.InfoEvent("Duplicate file scan completed. Refined potential duplicates found: " + refinedInitialSegmentDuplicates.size()));

        return confirmedDuplicates;
    }

    private static Map<String, List<Path>> convertKeysToString(Map<Long, List<Path>> filesBySize) {
        Map<String, List<Path>> filesGroupedBySize = new HashMap<>();
        filesBySize.forEach((key, paths) -> {
            String keyAsString = String.valueOf(key);
            filesGroupedBySize.put(keyAsString, paths);
            eventPublisher.publishEvent(Events.InfoEvent("Processed and converted file size from Long to String for size: " + keyAsString));
        });
        return filesGroupedBySize;
    }


    private static Map<String, List<Path>> performInitialHashing(Map<String, List<Path>> filesGroupedBySize) {
        HashingStrategy initialHashingStrategy = new CRC32Hashing();
        Map<String, List<Path>> initialSegmentDuplicates = new HashMap<>();

        int totalEntries = filesGroupedBySize.size();
        int processedEntries = 0;

        for (Map.Entry<String, List<Path>> entry : filesGroupedBySize.entrySet()) {
            String sizeGroup = entry.getKey();
            List<Path> paths = entry.getValue();

            // Perform hashing for the current group
            Map<String, List<Path>> hashedFilesForGroup = identifyDuplicatesForGroup(paths, initialHashingStrategy, HashingMode.INITIAL_SEGMENT);
            initialSegmentDuplicates.putAll(hashedFilesForGroup);

            processedEntries++; // Increment the counter after processing each group
            double progressPercentage = ((double) processedEntries / totalEntries) * 100;
            // Publish an event after processing each group with progress information
            eventPublisher.publishEvent(Events.InfoEvent(String.format("Processed initial hashing for file size group: %s. Progress: %.2f%%", sizeGroup, progressPercentage)));
        }

        return initialSegmentDuplicates;
    }



    private static Map<String, List<Path>> refineDuplicatesWithSHA256(Map<String, List<Path>> initialSegmentDuplicates) {
        HashingStrategy refinedHashingStrategy = new SHA256HashingStrategy();
        Map<String, List<Path>> refinedDuplicates = new HashMap<>();

        int totalEntries = initialSegmentDuplicates.size();
        int processedEntries = 0;

        for (Map.Entry<String, List<Path>> entry : initialSegmentDuplicates.entrySet()) {
            String hash = entry.getKey();
            List<Path> potentialDuplicates = entry.getValue();

            Map<String, List<Path>> refinedFilesForHash = identifyDuplicatesForGroup(potentialDuplicates, refinedHashingStrategy, HashingMode.INITIAL_SEGMENT);
            refinedDuplicates.putAll(refinedFilesForHash);

            processedEntries++; // Increment the counter on a separate line for clarity
            double progressPercentage = ((double) processedEntries / totalEntries) * 100;
            eventPublisher.publishEvent(Events.InfoEvent(String.format("Refined duplicates for hash: %s. Progress: %.2f%%", hash, progressPercentage)));
        }

        return refinedDuplicates;
    }

    private static Map<String, List<Path>> identifyDuplicatesForGroup(List<Path> paths, HashingStrategy hashingStrategy, HashingMode mode) {
        Map<String, List<Path>> hashGroups = new HashMap<>();
        paths.forEach(path -> {
            try {
                String hash = computeFileHash(path, mode, hashingStrategy).orElse(String.valueOf(path.hashCode()));
                hashGroups.computeIfAbsent(hash, k -> new ArrayList<>()).add(path);
            } catch (Exception e) {
                eventPublisher.publishEvent(Events.InfoEvent("Error computing hash for file: " + path + ". Error: " + e.getMessage()));
            }
        });

        // Filter out non-duplicates (entries with only one path)
        return hashGroups.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map<String, List<Path>> confirmDuplicatesWithCRC32(Map<String, List<Path>> potentialDuplicates) {
        HashingStrategy fullFileHashingStrategy = new CRC32Hashing();
        Map<String, List<Path>> confirmedDuplicates = new HashMap<>();

        int totalEntries = potentialDuplicates.size();
        int processedEntries = 0;

        for (Map.Entry<String, List<Path>> entry : potentialDuplicates.entrySet()) {
            String hash = entry.getKey();
            List<Path> paths = entry.getValue();

            // Process each group of potential duplicates for the full file CRC32 hash
            Map<String, List<Path>> confirmedDuplicatesForGroup = identifyDuplicatesForGroup(paths, fullFileHashingStrategy, HashingMode.FULL_FILE);

            // Merge the confirmed duplicates from this group into the overall map
            confirmedDuplicatesForGroup.forEach((confirmedHash, confirmedPaths) ->
                    confirmedDuplicates.computeIfAbsent(confirmedHash, k -> new ArrayList<>()).addAll(confirmedPaths));

            processedEntries++; // Increment the counter after processing each group
            double progressPercentage = ((double) processedEntries / totalEntries) * 100;
            // Publish an event after processing each group with progress information
            eventPublisher.publishEvent(Events.InfoEvent(String.format("Processed full file CRC32 hash for potential duplicate group with hash: %s. Progress: %.2f%%", hash, progressPercentage)));
        }

        // Log the delta in potential duplicates after this step
        long totalPotentialDuplicates = potentialDuplicates.values().stream().mapToLong(Collection::size).sum();
        long totalConfirmedDuplicates = confirmedDuplicates.values().stream().mapToLong(Collection::size).sum();
        long delta = totalPotentialDuplicates - totalConfirmedDuplicates;
        eventPublisher.publishEvent(Events.InfoEvent(String.format("Step Complete: Reduced from %d potential to %d confirmed duplicates. Delta: %d", totalPotentialDuplicates, totalConfirmedDuplicates, delta)));

        return confirmedDuplicates;
    }



    private static void generateDuplicateScanningReport() {
        eventPublisher.publishEvent(Events.InfoEvent("Generating duplicate scanning report."));
        statsCollector.generateTabulatedReport(); // Consider passing the finalConfirmedDuplicates map for detailed reporting
    }






    private static Optional<String> computeFileHash(Path path, HashingMode mode, HashingStrategy hashingStrategy) {
        long startTime = System.nanoTime();
        Optional<String> hash = Optional.empty();

        try {
            long fileSize = Files.size(path);
            long sizeToRead = mode.determineSizeToRead(fileSize);
            hash = Optional.of(hashingStrategy.hashFileBySize(path, sizeToRead));
            eventPublisher.publishEvent(Events.InfoEvent("File hashed successfully: " + path + ". Hash: " + hash.get()));
        } catch (IOException e) {
            System.err.println("Error computing hash for " + path + ": " + e.getMessage());
        } finally {
            long endTime = System.nanoTime();
            long processingTime = endTime - startTime;
            statsCollector.addFileStats(path,  processingTime,mode);
        }

        return hash;
    }
}
