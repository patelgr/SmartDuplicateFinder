package net.hiralpatel.enums;

public enum FileSizeHashingLevel {
    SIZE_64KB(64 * 1024L),       // 64 KB in bytes
    SIZE_1MB(1 * 1024 * 1024L),  // 1 MB in bytes
    SIZE_1GB(1 * 1024 * 1024 * 1024L),  // 1 GB in bytes
    SIZE_10GB(10 * 1024 * 1024 * 1024L), // 10 GB in bytes
    SIZE_1TB(1L * 1024 * 1024 * 1024 * 1024); // 1 TB in bytes

    private final long size;

    // Constructor to set the file size for each level
    FileSizeHashingLevel(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public static FileSizeHashingLevel nextLevel(FileSizeHashingLevel current) {
        FileSizeHashingLevel[] levels = FileSizeHashingLevel.values();
        int currentIndex = current.ordinal();

        // Check if the current level is not the last one
        if (currentIndex < levels.length - 1) {
            // Return the next level
            return levels[currentIndex + 1];
        }

        // Return the current level if it's the last one, indicating no next level
        return current;
    }
}
