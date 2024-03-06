package net.hiralpatel.ui;

import net.hiralpatel.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ReportGenerator {
    private static final String HTML_HEADER = """
            <!DOCTYPE html>
            <html>
            <head>
            <title>Duplicate Items Report</title>
            <style>table { width: 100%%; border-collapse: collapse; } th, td { border: 1px solid black; padding: 8px; } th { background-color: #f2f2f2; }</style>
            </head>
            <body>
            <h1>Duplicate Items Report</h1>
            """;

    private static final String HTML_FOOTER = """
            </body>
            </html>
            """;

    public static void generateReport(Map<String, List<Path>> duplicates) throws IOException {
        if (duplicates.isEmpty()) {
            System.out.println("No duplicates found.");
            return;
        }

        try (BufferedWriter htmlWriter = new BufferedWriter(new FileWriter(System.getProperty("user.home") + "/DuplicateItemsReport.html"));
             BufferedWriter scriptWriter = new BufferedWriter(new FileWriter(System.getProperty("user.home") + "/Downloads/dups.sh"))) {
            htmlWriter.write(HTML_HEADER);

            for (Map.Entry<String, List<Path>> entry : duplicates.entrySet()) {
                htmlWriter.append("<h2>Duplicate Files/Directory, Hash:").append(entry.getKey()).append("</h2>\n");
                htmlWriter.append("<table>\n");
                htmlWriter.append("<tr><th>File Path</th><th>Size</th><th>Action</th></tr>\n");

                List<Path> paths = entry.getValue();
                sortPaths(paths);

                Path originalFilePath = paths.getFirst(); // The first path after sorting is the original

                for (Path path : paths) {
                    long size = Files.size(path); // Get the size of the file.

                    htmlWriter.append("<tr><td>").append(path.toString()).append("</td><td>").append(FileUtils.formatSize(size)).append("</td>");

                    if (!path.equals(originalFilePath) && !hasOriginalInPath(path)) {
                        String command = String.format(
                                "(cmp --silent \"%s\" \"%s\" && rm \"%s\" && echo \"%s: success\") || echo \"%s: failure\"",
                                originalFilePath, path, path, path, path);
                        htmlWriter.append("<td>").append(command).append("</td>");
                        scriptWriter.append(command).append("\n");
                    } else {
                        htmlWriter.append("<td>").append(path.equals(originalFilePath) ? "Keep as original" : "Skipped due to 'Original' in path").append("</td>");
                    }

                    htmlWriter.append("</tr>\n");
                }

                htmlWriter.append("</table>\n");
            }


            htmlWriter.write(HTML_FOOTER);

            System.out.println("Report and script generated successfully.");
        } catch (IOException e) {
            System.err.println("Error writing report or script to file: " + e.getMessage());
        }
    }

    private static boolean hasOriginalInPath(Path path) {
        Path current = path.getParent();
        while (current != null) {
            if (current.getFileName().toString().toLowerCase().contains("original")) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private static void sortPaths(List<Path> paths) {
        paths.sort(new Comparator<Path>() {
            public int compare(Path p1, Path p2) {
                boolean is2021Onward1 = p1.toString().contains("/2021 Onward/");
                boolean is2021Onward2 = p2.toString().contains("/2021 Onward/");
                if (is2021Onward1 && !is2021Onward2) return -1;
                if (!is2021Onward1 && is2021Onward2) return 1;

                boolean hasDigitsOrCopy1 = p1.getFileName().toString().matches(".*(\\d+|Copy).*");
                boolean hasDigitsOrCopy2 = p2.getFileName().toString().matches(".*(\\d+|Copy).*");
                if (!hasDigitsOrCopy1 && hasDigitsOrCopy2) return -1;
                if (hasDigitsOrCopy1 && !hasDigitsOrCopy2) return 1;

                if (hasDigitsOrCopy1) {
                    boolean hasDigits1 = p1.getFileName().toString().matches(".*\\d+.*");
                    boolean hasDigits2 = p2.getFileName().toString().matches(".*\\d+.*");
                    if (hasDigits1 && hasDigits2) {
                        int num1 = extractNumber(p1.getFileName().toString());
                        int num2 = extractNumber(p2.getFileName().toString());
                        return Integer.compare(num1, num2);
                    } else if (hasDigits1) {
                        return 1; // p2 (without digits) should come before p1 (with digits)
                    } else if (hasDigits2) {
                        return -1; // p1 (without digits) should come before p2 (with digits)
                    }
                }

                return p1.compareTo(p2); // Default alphabetical sorting
            }

            private int extractNumber(String fileName) {
                String number = fileName.replaceAll(".*?(\\d+).*", "$1");
                try {
                    return Integer.parseInt(number);
                } catch (NumberFormatException e) {
                    return -1; // In case no number is found, return -1 to indicate this
                }
            }
        });
    }

}
