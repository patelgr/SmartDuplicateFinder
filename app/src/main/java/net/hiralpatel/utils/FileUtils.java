package net.hiralpatel.utils;

public class FileUtils {

    /**
     * Formats the size in bytes into a human-readable string with the appropriate unit (KB, MB, GB).
     *
     * @param size Size in bytes.
     * @return Formatted size string with unit.
     */
    public static String formatSize(long size) {
        if (size < 1024) {
            return size + " Bytes";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
