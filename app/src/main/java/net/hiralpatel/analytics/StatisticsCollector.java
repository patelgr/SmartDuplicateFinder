package net.hiralpatel.analytics;

import net.hiralpatel.hashing.HashingMode;
import net.hiralpatel.utils.MimeTypeMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StatisticsCollector {
    private final Map<String, BinStats> binStatsMap = new TreeMap<>();

    public void addFileStats(Path filePath, long processingTime, HashingMode mode) {
        try {
            String mimeType = Files.probeContentType(filePath);
            String simplifiedType = MimeTypeMapper.simplifyMimeType(mimeType); // Use MimeTypeMapper to simplify the MIME type
            long fileSize = Files.size(filePath);
            String bin = determineBin(fileSize);
            BinStats binStats = binStatsMap.computeIfAbsent(bin, k -> new BinStats());

            binStats.count.incrementAndGet();
            FileTypeStats fileTypeStats = binStats.fileTypeStatsMap.computeIfAbsent(simplifiedType, k -> new FileTypeStats());
            fileTypeStats.count.incrementAndGet();
            if (mode.equals(HashingMode.INITIAL_SEGMENT)) {
                fileTypeStats.initialProcessingTime.addAndGet(processingTime);
            } else {
                fileTypeStats.fullProcessingTime.addAndGet(processingTime);
            }
        } catch (IOException e) {
            System.err.println("Error accessing file properties for " + filePath + ": " + e.getMessage());
        }
    }

    public void generateTabulatedReport() {
        System.out.format("%-15s | %-50s | %-22s | %-20s | %-15s | %-10s%n",
                "Class Intervals", "File Types", "Initial Scan Time (ms)", "Full Scan Time (ms)", "Improvement", "Count");

        binStatsMap.forEach((bin, binStats) -> {
            binStats.fileTypeStatsMap.forEach((fileType, fileTypeStats) -> {
                double avgInitialTime = fileTypeStats.count.get() > 0 ?
                        (double) fileTypeStats.initialProcessingTime.get() / fileTypeStats.count.get() / 1_000_000.0 : 0;
                double avgFullTime = fileTypeStats.count.get() > 0 ?
                        (double) fileTypeStats.fullProcessingTime.get() / fileTypeStats.count.get() / 1_000_000.0 : 0;
                double improvement = avgInitialTime > 0 ? ((avgFullTime - avgInitialTime) / avgInitialTime) * 100 : 0;

                System.out.format("%-15s | %-50s | %-22.2f | %-20.2f | %-14.2f%% | %-10d%n",
                        bin, fileType, avgInitialTime, avgFullTime, improvement, fileTypeStats.count.get());
            });

            System.out.println("-------------------------------------------------------------------------------------------------------------");
        });
    }

    private String determineBin(long fileSize) {
        int category = fileSize < 1_048_576 ? 0 : fileSize < 100_000_000 ? 1 : fileSize < 1_073_741_824 ? 2 : 3;
        return switch (category) {
            case 0 -> "<1MB";
            case 1 -> "1MB-100MB";
            case 2 -> "100MB-1GB";
            default -> "1GB+";
        };
    }

    private static class BinStats {
        AtomicInteger count = new AtomicInteger();
        Map<String, FileTypeStats> fileTypeStatsMap = new HashMap<>();
    }

    private static class FileTypeStats {
        AtomicInteger count = new AtomicInteger();
        AtomicLong initialProcessingTime = new AtomicLong();
        AtomicLong fullProcessingTime = new AtomicLong();
    }
}
