package net.hiralpatel.hashing;

public enum HashingMode {
    INITIAL_SEGMENT {
        @Override
        public long determineSizeToRead(long fileSize) {
            if (fileSize < 1_048_576) { // Less than 1MB
                return 1_024; // Read the whole file for small files
            } else if (fileSize < 100_000_000) { // Between 1MB and 100MB
                return 4_096; // Start with 4KB
            } else { // Larger than 100MB
                return 8_388_608; // Start with 8MB for very large files
            }
        }
    },
    FULL_FILE {
        @Override
        public long determineSizeToRead(long fileSize) {
            return fileSize; // Read the entire file
        }
    };

    public abstract long determineSizeToRead(long fileSize);
}
