package net.hiralpatel.hashing;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class AdaptiveHashing implements HashingStrategy {

    @Override
    public String hashFileBySize(Path filePath, long size) throws IOException {
        Checksum checksum = new CRC32();
        final long fileSize = Files.size(filePath); // Get the actual file size
        final int minBufferSize = 8192; // 8KB minimum buffer size
        final long effectiveSize = Math.max(minBufferSize, Math.min(size, fileSize)); // Ensure size is between 8KB and file size

        try (InputStream fis = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[minBufferSize]; // Use an 8KB buffer for reading
            int nRead;
            long totalRead = 0;
            while ((nRead = fis.read(buffer)) != -1 && totalRead < effectiveSize) {
                checksum.update(buffer, 0, nRead);
                totalRead += nRead;
            }
        }
        return Long.toHexString(checksum.getValue());
    }
}
