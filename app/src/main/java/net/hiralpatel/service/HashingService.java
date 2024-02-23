package net.hiralpatel.service;

import net.hiralpatel.model.File;
import net.hiralpatel.model.FileSystemObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.CRC32;

public class HashingService {

    public static List<FileSystemObject> processAndIdentifyDuplicates(List<FileSystemObject> fsObjects) {
        Map<Long, List<FileSystemObject>> crcToFsObjectMap = new HashMap<>();

        for (FileSystemObject fsObject : fsObjects) {
            if (fsObject instanceof File) { // Check if the FileSystemObject is a file
                try {
                    long crcHash = computeCRC32(fsObject, 1024 * 1024); // Process only for files, 1024KB or 1MB
                    crcToFsObjectMap.computeIfAbsent(crcHash, k -> new ArrayList<>()).add(fsObject);
                } catch (IOException e) {
                    System.err.println("Error processing file " + fsObject.getPath() + ": " + e.getMessage());
                }
            }
        }

        // Identify and mark duplicates as before
        List<FileSystemObject> duplicates = new ArrayList<>();
        for (List<FileSystemObject> potentialDuplicates : crcToFsObjectMap.values()) {
            if (potentialDuplicates.size() > 1) {
                for (FileSystemObject duplicate : potentialDuplicates) {
                    duplicate.setDuplicate(true); // Mark as duplicate
                    duplicates.add(duplicate);
                }
            }
        }

        return duplicates;
    }

    private static long computeCRC32(FileSystemObject fsObject, long maxSizeToCheck) throws IOException {
        CRC32 crc = new CRC32();
        try (InputStream inputStream = Files.newInputStream(fsObject.getPath())) {
            byte[] buffer = new byte[1024 * 1024]; // 1024KB buffer
            int bytesRead;
            long totalBytesRead = 0;

            do {
                bytesRead = inputStream.read(buffer);
                if (bytesRead != -1) {
                    crc.update(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
            } while (bytesRead != -1 && totalBytesRead < maxSizeToCheck);
        }

        return crc.getValue();
    }
}
