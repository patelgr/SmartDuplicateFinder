package net.hiralpatel.hashing;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SHA256HashingStrategy implements HashingStrategy {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }


    @Override
    public String hashFileBySize(Path filePath, long size) throws IOException {
        MessageDigest sha256Digest;
        try {
            sha256Digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }

        final long fileSize = Files.size(filePath); // Get the actual file size
        final long effectiveSize = Math.min(size, fileSize); // Use the smaller of the desired size or the file size

        // Use a buffer size that is a factor of the effective size for efficient reading, with a minimum of 8KB
        final int bufferSize = (int) Math.max(8192, effectiveSize / 16);
        try (InputStream fis = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[bufferSize];
            int nRead;
            long totalRead = 0; // Track the total number of bytes read

            while ((nRead = fis.read(buffer, 0, (int)Math.min(buffer.length, effectiveSize - totalRead))) != -1 && totalRead < effectiveSize) {
                // Update the digest with the bytes read
                sha256Digest.update(buffer, 0, nRead);
                totalRead += nRead;
            }
        }

        // Convert the byte array to a hex string
        byte[] hashBytes = sha256Digest.digest();
        return bytesToHex(hashBytes); // Using the optimized bytesToHex method
    }
}
