package net.hiralpatel.analytics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StatisticsCollector {
    private static final Map<String, String> mimeTypeMap = new HashMap<>(128);

    static {
        // Text
        mimeTypeMap.put("text/html", "HTML");
        mimeTypeMap.put("text/x-python-script", "Python Script");

        // Image
        mimeTypeMap.put("image/jpeg", "JPEG");
        mimeTypeMap.put("image/png", "PNG");
        mimeTypeMap.put("image/gif", "GIF");

        // XML
        mimeTypeMap.put("application/xml", "XML");

        // Archive
        mimeTypeMap.put("application/x-rar-compressed", "Archive");

        // Multimedia Audio
        mimeTypeMap.put("audio/x-wav", "WAV Audio");

        // Office Documents
        mimeTypeMap.put("application/vnd.ms-excel", "Excel (Legacy)");
        mimeTypeMap.put("application/vnd.apple.keynote", "Keynote");
        mimeTypeMap.put("application/vnd.apple.pages", "Pages");
        mimeTypeMap.put("application/vnd.apple.numbers", "Numbers");
        mimeTypeMap.put("application/vnd.adobe.air-application-installer-package+zip", "Adobe AIR Installer");
        mimeTypeMap.put("application/vnd.amazon.ebook", "Kindle Ebook");
        mimeTypeMap.put("application/vnd.android.package-archive", "Android Package");
        mimeTypeMap.put("application/x-msdownload", "Windows Executable");
        mimeTypeMap.put("application/java-archive", "Java Archive");
        mimeTypeMap.put("application/vnd.ms-cab-compressed", "Microsoft Cabinet File");
        mimeTypeMap.put("application/x-tika-msoffice", "Microsoft Office Document");
        mimeTypeMap.put("application/vnd.oasis.opendocument.text", "OpenDocument Text");
        mimeTypeMap.put("application/vnd.oasis.opendocument.spreadsheet", "OpenDocument Spreadsheet");
        mimeTypeMap.put("application/vnd.oasis.opendocument.presentation", "OpenDocument Presentation");
        mimeTypeMap.put("application/vnd.ms-htmlhelp", "HTML Help File");
        mimeTypeMap.put("application/vnd.ms-works", "Microsoft Works Document");
        mimeTypeMap.put("application/vnd.lotus-wordpro", "Lotus WordPro Document");
        mimeTypeMap.put("application/vnd.lotus-1-2-3", "Lotus 1-2-3 Spreadsheet");
        mimeTypeMap.put("application/x-dos_ms_excel", "Excel (DOS)");
        mimeTypeMap.put("application/x-works-spreadsheet", "Microsoft Works Spreadsheet");
        mimeTypeMap.put("application/x-works", "Microsoft Works Database");
        mimeTypeMap.put("application/vnd.ms-wpl", "Windows Media Player Playlist");
        mimeTypeMap.put("application/vnd.ms-xpsdocument", "XPS Document");
        mimeTypeMap.put("application/x-pkcs12", "PKCS #12 File");

        // Others
        mimeTypeMap.put("application/postscript", "PostScript");
        mimeTypeMap.put("application/x-shockwave-flash", "Flash Animation");
        mimeTypeMap.put("application/octet-stream", "Binary Data");
        // Archive
        mimeTypeMap.put("application/x-tar", "Archive");
        mimeTypeMap.put("application/x-gzip", "Archive");
        mimeTypeMap.put("application/zip", "Archive");

        // Multimedia Audio
        mimeTypeMap.put("audio/vnd.wave", "Multimedia Audio");
        mimeTypeMap.put("audio/x-m4a", "Multimedia Audio");
        mimeTypeMap.put("audio/x-mpegurl", "Multimedia Audio");
        mimeTypeMap.put("audio/mp4", "Multimedia Audio");
        mimeTypeMap.put("audio/flac", "Multimedia Audio");
        mimeTypeMap.put("audio/mpeg", "Multimedia Audio");
        mimeTypeMap.put("audio/ogg", "Multimedia Audio");
        mimeTypeMap.put("audio/amr", "Multimedia Audio");
        mimeTypeMap.put("audio/aac", "Multimedia Audio");
        mimeTypeMap.put("audio/basic", "Multimedia Audio");

        // Multimedia Video
        mimeTypeMap.put("video/quicktime", "Multimedia Video");
        mimeTypeMap.put("video/mp4", "Multimedia Video");
        mimeTypeMap.put("video/x-ms-wmv", "Multimedia Video");
        mimeTypeMap.put("video/x-msvideo", "Multimedia Video");
        mimeTypeMap.put("video/x-matroska", "Multimedia Video");
        mimeTypeMap.put("video/ogg", "Multimedia Video");
        mimeTypeMap.put("video/webm", "Multimedia Video");
        mimeTypeMap.put("video/mpeg", "Multimedia Video");

        // Document
        mimeTypeMap.put("application/msword", "Document");
        mimeTypeMap.put("application/vnd.ms-powerpoint", "Document");
        mimeTypeMap.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Document");
        mimeTypeMap.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Document");
        mimeTypeMap.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", "Document");

        // PDF
        mimeTypeMap.put("application/pdf", "PDF");

        // Text
        mimeTypeMap.put("text/plain", "Text");
        mimeTypeMap.put("text/csv", "Text");
        mimeTypeMap.put("application/json", "Text");
    }

    private final Map<String, BinStats> binStatsMap = new TreeMap<>();

    // Updated method signature to remove 'scanType'
    public void addFileStats(Path filePath, String fileType, long processingTime) {
        try {
            long fileSize = Files.size(filePath);
            String bin = determineBin(fileSize);
            BinStats binStats = binStatsMap.computeIfAbsent(bin, k -> new BinStats());

            binStats.count.incrementAndGet();
            FileTypeStats fileTypeStats = binStats.fileTypeStatsMap.computeIfAbsent(fileType, k -> new FileTypeStats());
            fileTypeStats.count.incrementAndGet();

            // Determine if this is an initial or full scan based on processing time
            if (processingTime <= 1000) { // Assuming a threshold for initial processing time
                fileTypeStats.initialProcessingTime.addAndGet(processingTime);
            } else {
                fileTypeStats.fullProcessingTime.addAndGet(processingTime);
            }
        } catch (IOException e) {
            System.err.println("Error accessing file size for " + filePath + ": " + e.getMessage());
        }
    }

    public void generateTabulatedReport() {
        System.out.format("%-15s | %-50s | %-22s | %-20s | %-15s | %-10s%n",
                "Class Intervals", "File Types", "Initial Scan Time (ms)", "Full Scan Time (ms)", "Improvement", "Count");

        binStatsMap.forEach((bin, binStats) -> {
            binStats.fileTypeStatsMap.forEach((fileType, fileTypeStats) -> {
                String simplifiedFileType = simplifyFileType(fileType); // Simplify the file type

                double avgInitialTime = fileTypeStats.count.get() > 0 ?
                        (double) fileTypeStats.initialProcessingTime.get() / fileTypeStats.count.get() / 1_000_000.0 : 0;
                double avgFullTime = fileTypeStats.count.get() > 0 ?
                        (double) fileTypeStats.fullProcessingTime.get() / fileTypeStats.count.get() / 1_000_000.0 : 0;
                double improvement = avgInitialTime > 0 ? ((avgFullTime - avgInitialTime) / avgInitialTime) * 100 : 0;

                System.out.format("%-15s | %-50s | %-22.2f | %-20.2f | %-14.2f%% | %-10d%n",
                        bin, simplifiedFileType, avgInitialTime, avgFullTime, improvement, fileTypeStats.count.get());
            });

            System.out.println("-------------------------------------------------------------------------------------------------------------");
        });
    }

    private String determineBin(long fileSize) {
        if (fileSize < 1_048_576) {
            return "<1MB";
        } else if (fileSize < 100_000_000) {
            return "1MB-100MB";
        } else if (fileSize < 1_073_741_824) {
            return "100MB-1GB";
        } else {
            return "1GB+";
        }
    }

    private String simplifyFileType(String mimeType) {

        return mimeTypeMap.getOrDefault(mimeType, "Unknown:" + mimeType);
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
