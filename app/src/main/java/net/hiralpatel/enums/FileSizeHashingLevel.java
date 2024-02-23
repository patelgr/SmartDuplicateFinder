package net.hiralpatel.enums;

public enum FileSizeHashingLevel {

    BASE(0),
    SIZE_64KB(64 * 1024L),       // 64 KB in bytes
    SIZE_1MB(1024 * 1024L),  // 1 MB in bytes
    SIZE_1GB(1024 * 1024 * 1024L),  // 1 GB in bytes
    SIZE_10GB(10 * 1024 * 1024 * 1024L), // 10 GB in bytes
    SIZE_1TB((long) 1024 * 1024 * 1024 * 1024); // 1 TB in bytes

    private final long size;

    // Constructor to set the file size for each level
    FileSizeHashingLevel(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public FileSizeHashingLevel nextLevel() {
        int nextOrdinal = this.ordinal() + 1;
        if (nextOrdinal >= FileSizeHashingLevel.values().length) {
            return null;
        }
        return FileSizeHashingLevel.values()[nextOrdinal];
    }
}
