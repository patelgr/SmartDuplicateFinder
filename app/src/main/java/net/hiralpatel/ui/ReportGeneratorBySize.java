package net.hiralpatel.ui;

import net.hiralpatel.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ReportGeneratorBySize {

    private static final String HTML_HEADER = """
            <!DOCTYPE html>
            <html>
            <head>
            <title>Duplicate Items Report</title>
            <style>table { width: 100%; border-collapse: collapse; } th, td { border: 1px solid black; padding: 8px; } th { background-color: #f2f2f2; }</style>
            </head>
            <body>
            <h1>Duplicate Items Report</h1>
            """;

    private static final String HTML_FOOTER = """
            </body>
            </html>
            """;

    // Define a record to represent a pair of directories
    record DirectoryPair(Path leftDirectory, Path rightDirectory) implements Comparable<DirectoryPair> {
        @Override
        public int compareTo(DirectoryPair o) {
            int leftComp = leftDirectory.compareTo(o.leftDirectory);
            return leftComp == 0 ? rightDirectory.compareTo(o.rightDirectory) : leftComp;
        }
    }

    private static class DirectoryComparison {
        Set<DirectoryPair> directoryPairs = ConcurrentHashMap.newKeySet();
        int duplicateFiles;
        long duplicateSize;

        // Method to add a directory pair, ensuring no duplicates
        void addDirectoryPair(Path dir1, Path dir2) {
            Path left = dir1.compareTo(dir2) <= 0 ? dir1 : dir2;
            Path right = dir1.compareTo(dir2) > 0 ? dir1 : dir2;
            directoryPairs.add(new DirectoryPair(left, right));
        }
    }

    private static final Map<Path, Long> fileSizeCache = new ConcurrentHashMap<>();

    private static long getFileSize(Path path) throws IOException {
        return fileSizeCache.computeIfAbsent(path, p -> {
            try {
                return Files.size(p);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public static void generateReport(Map<String, List<Path>> duplicates) throws IOException {
        if (duplicates.isEmpty()) {
            System.out.println("No duplicates found.");
            return;
        }

        Map<DirectoryPair, DirectoryComparison> comparisons = new ConcurrentHashMap<>();

        // Process paths in parallel to handle large datasets efficiently
        duplicates.values().parallelStream().forEach(paths -> {
            Path original = paths.getFirst(); // Assuming the first one is the original
            Path dir1 = original.getParent();

            for (int i = 1; i < paths.size(); i++) {
                Path duplicate = paths.get(i);
                Path dir2 = duplicate.getParent();

                DirectoryPair pair = new DirectoryPair(dir1.compareTo(dir2) <= 0 ? dir1 : dir2, dir1.compareTo(dir2) > 0 ? dir1 : dir2);
                DirectoryComparison comparison = comparisons.computeIfAbsent(pair, k -> new DirectoryComparison());
                comparison.addDirectoryPair(dir1, dir2);

                try {
                    comparison.duplicateFiles += 1;
                    comparison.duplicateSize += getFileSize(duplicate);
                } catch (UncheckedIOException | IOException e) {
                    System.err.println("Failed to get file size for " + duplicate + ": " + e.getCause().getMessage());
                }
            }
        });

        // Generate the report
        Path reportPath = Paths.get(System.getProperty("user.home"), "DuplicateItemsReport.html");
        try (BufferedWriter htmlWriter = Files.newBufferedWriter(reportPath)) {
            htmlWriter.write(HTML_HEADER);
            htmlWriter.append("<table>\n");
            htmlWriter.append("<tr><th>Directory 1</th><th>Directory 2</th><th>Duplicate Files</th><th>Duplicate Size</th></tr>\n");

            comparisons.values().stream().sorted((o1, o2) -> Long.compare(o2.duplicateSize, o1.duplicateSize)).forEach(comparison -> {
                comparison.directoryPairs.forEach(pair -> {
                    try {
                        htmlWriter.append(String.format("<tr><td>%s</td><td>%s</td><td>%d</td><td>%s</td></tr>\n",
                                pair.leftDirectory(), pair.rightDirectory(), comparison.duplicateFiles, FileUtils.formatSize(comparison.duplicateSize)));
                    } catch (IOException e) {
                        System.err.println("Error writing to report file: " + e.getMessage());
                    }
                });
            });

            htmlWriter.append("</table>\n");
            htmlWriter.write(HTML_FOOTER);
            System.out.println("Report generated successfully.");
        } catch (IOException e) {
            System.err.println("Error writing report to file: " + e.getMessage());
        }
    }
}
