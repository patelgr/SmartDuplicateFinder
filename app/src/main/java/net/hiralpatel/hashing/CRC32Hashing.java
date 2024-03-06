package net.hiralpatel.hashing;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class CRC32Hashing implements HashingStrategy {

    @Override
    public String hashFileBySize(Path filePath, long size) throws IOException {
        Checksum checksum = new CRC32();
        final long fileSize = Files.size(filePath); // Get the actual file size
        final long effectiveSize = Math.min(size, fileSize); // Use the smaller of the desired size or the file size

        // Use a buffer size that is a factor of the effective size for efficient reading, with a minimum of 8KB
        final int bufferSize = (int) Math.max(8192, effectiveSize / 16);
        try (InputStream fis = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[bufferSize];
            int nRead;
            long totalRead = 0;
            while ((nRead = fis.read(buffer, 0, (int)Math.min(buffer.length, effectiveSize - totalRead))) != -1 && totalRead < effectiveSize) {
                checksum.update(buffer, 0, nRead);
                totalRead += nRead;
            }
        }
        return Long.toHexString(checksum.getValue());
    }
}
