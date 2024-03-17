package net.hiralpatel.ui;

import net.hiralpatel.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;

public class ReportGenerator {
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

    private static final List<String> sortingCriteria = List.of(
            "originalInPath", "/2021 Onward/", "containsDigits", "containsCopy", "timestampCreated", "timestampAccessed"
    );

    private static final List<String> actionCriteria = List.of("originalInPath");


    public static void generateReport(Map<String, List<Path>> duplicates) throws IOException {
        if (duplicates.isEmpty()) {
            System.out.println("No duplicates found.");
            return;
        }

        try (BufferedWriter htmlWriter = new BufferedWriter(new FileWriter(System.getProperty("user.home") + "/DuplicateItemsReport2.html"));
             BufferedWriter scriptWriter = new BufferedWriter(new FileWriter(System.getProperty("user.home") + "/Downloads/dups.sh"))) {
            htmlWriter.write(HTML_HEADER);

            for (Map.Entry<String, List<Path>> entry : duplicates.entrySet()) {
                htmlWriter.append("<h2>Duplicate Files/Directory, Hash:").append(entry.getKey()).append(",Size:").append(String.valueOf(entry.getValue().size())).append("</h2>\n");
                List<Path> paths = entry.getValue();
                if (paths.size() > 1) {
                    htmlWriter.append("<table>\n");
                    htmlWriter.append("<tr><th>File Path</th><th>Size</th><th>Action</th></tr>\n");


                    sortPaths(paths);

                    Path originalFilePath = paths.getFirst(); // Assume the first path after sorting is the original

                    for (Path path : paths) {
                        long size = Files.size(path); // Get the size of the file.

                        if (size > 1024 * 1024) {
                            htmlWriter.append("<tr><td>").append(path.toString()).append("</td><td>").append(FileUtils.formatSize(size)).append("</td>");

                            boolean idOriginalPath = path.equals(originalFilePath);
                            if (idOriginalPath || meetsAnyActionCriteria(path, actionCriteria)) {
                                htmlWriter.append("<td>").append(idOriginalPath ? "Keep as original" : "Skipped due to criteria").append("</td>");
                            } else {
                                String command = String.format("compare_and_delete \"%s\" \"%s\"", originalFilePath, path);
                                htmlWriter.append("<td>").append(command).append("</td>");
                                scriptWriter.append(command).append("\n");
                            }

                            htmlWriter.append("</tr>\n");
                        }
                    }

                    htmlWriter.append("</table>\n");
                } else {
                    htmlWriter.append("<div> 1 File or less:").append(String.valueOf(paths)).append("</div>");
                }

            }

            htmlWriter.write(HTML_FOOTER);
            System.out.println("Report and script generated successfully.");
        } catch (IOException e) {
            System.err.println("Error writing report or script to file: " + e.getMessage());
        }
    }

    private static boolean meetsAnyActionCriteria(Path path, List<String> criteria) {
        for (String criterion : criteria) {
            if (checkCriterion(path, criterion)) {
                return true; // The path meets at least one criterion.
            }
        }
        return false; // The path does not meet any of the criteria.
    }

    private static boolean checkCriterion(Path path, String criterion) {
        switch (criterion) {
            case "originalInPath":
                return path.toString().toLowerCase().contains("original");
            // Add more cases for additional criteria as needed.
            default:
                return false;
        }
    }

    private static void sortPaths(List<Path> paths) {
        paths.sort((p1, p2) -> {
            for (String criterion : sortingCriteria) {
                int result = compareByCriterion(p1, p2, criterion);
                if (result != 0) {
                    return result; // Use the first non-zero result.
                }
            }
            return p1.compareTo(p2); // Default to natural ordering.
        });
    }

    private static int compareByCriterion(Path p1, Path p2, String criterion) {
        try {
            return switch (criterion) {
                case "originalInPath" -> Boolean.compare(checkCriterion(p2, criterion), checkCriterion(p1, criterion));
                case "/2021 Onward/" ->
                        Boolean.compare(p2.toString().contains(criterion), p1.toString().contains(criterion));
                case "containsDigits" ->
                        Boolean.compare(p2.getFileName().toString().matches(".*\\d+.*"), p1.getFileName().toString().matches(".*\\d+.*"));
                case "containsCopy" ->
                        Boolean.compare(p2.getFileName().toString().contains("Copy"), p1.getFileName().toString().contains("Copy"));
                case "timestampCreated" -> Files.readAttributes(p1, BasicFileAttributes.class).creationTime()
                        .compareTo(Files.readAttributes(p2, BasicFileAttributes.class).creationTime());
                case "timestampAccessed" -> Files.readAttributes(p1, BasicFileAttributes.class).lastAccessTime()
                        .compareTo(Files.readAttributes(p2, BasicFileAttributes.class).lastAccessTime());
                default -> 0;
            };
        } catch (IOException e) {
            e.printStackTrace();
            return 0; // In case of an error, don't sort based on this criterion.
        }
    }


}
