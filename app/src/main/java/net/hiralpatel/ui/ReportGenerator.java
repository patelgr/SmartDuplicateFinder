package net.hiralpatel.ui;

import net.hiralpatel.utils.FileUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportGenerator {
    private static final String HTML_TEMPLATE = """
            <!DOCTYPE html>
            <html>
            <head>
            <title>Duplicate Items Report</title>
            <style>table { width: 100%%; border-collapse: collapse; } th, td { border: 1px solid black; padding: 8px; } th { background-color: #f2f2f2; }</style>
            </head>
            <body>
            <h1>Duplicate Items Report</h1>
            %s
            </body>
            </html>
            """;

    public static void generateReport(Map<Long, List<Path>> duplicates) {
        if (duplicates.isEmpty()) {
            System.out.println("No duplicates found.");
            return;
        }

        List<Map.Entry<Long, List<Path>>> sortedEntries = duplicates.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .collect(Collectors.toList());

        StringBuilder reportContent = new StringBuilder();
        for (Map.Entry<Long, List<Path>> entry : sortedEntries) {
            reportContent.append("<h2>Duplicate Files/Directory with size: ").append(FileUtils.formatSize(entry.getKey())).append("</h2>\n");
            reportContent.append("<table>\n");
            reportContent.append("<tr><th>File Path</th><th>Size</th></tr>\n");
            for (Path path : entry.getValue()) {
                reportContent.append("<tr><td>").append(path.toString()).append("</td><td>").append(FileUtils.formatSize(entry.getKey())).append("</td></tr>\n");
            }
            reportContent.append("</table>\n");
        }

        String finalHtml = String.format(HTML_TEMPLATE, reportContent);

        try (FileWriter fileWriter = new FileWriter(System.getProperty("user.home") + "/DuplicateItemsReport.html")) {
            fileWriter.write(finalHtml);
            System.out.println("Report generated successfully.");
        } catch (IOException e) {
            System.err.println("Error writing report to file: " + e.getMessage());
        }
    }


}
