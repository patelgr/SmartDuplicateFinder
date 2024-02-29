package net.hiralpatel.service;

import net.hiralpatel.cli.AppCli;
import net.hiralpatel.model.Directory;
import net.hiralpatel.model.FileSystemObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DuplicateFinderService {
    // Maps file size to a list of FileSystemObjects with that size

    public static List<List<Path>> findDuplicates(List<String> paths) throws IOException, NoSuchAlgorithmException {
        Map<String, List<Path>> hashGroups = new HashMap<>();

        // Compute a preliminary hash for each file (e.g., using the first few KBs)
        for (String filePath : paths) {
            Path path = Path.of(filePath);
            String partialHash = computePartialHash(path);
            hashGroups.putIfAbsent(partialHash, new ArrayList<>());
            hashGroups.get(partialHash).add(path);
        }

        List<List<Path>> duplicates = new ArrayList<>();
        // For each group with more than one file, compute full hashes to confirm duplicates
        for (List<Path> group : hashGroups.values()) {
            if (group.size() > 1) {
                Map<String, List<Path>> fullHashGroups = new HashMap<>();
                for (Path path : group) {
                    String fullHash = computeFullHash(path);
                    fullHashGroups.putIfAbsent(fullHash, new ArrayList<>());
                    fullHashGroups.get(fullHash).add(path);
                }
                // Add confirmed duplicates (groups with more than one file after full hash)
                fullHashGroups.values().stream()
                        .filter(g -> g.size() > 1)
                        .forEach(duplicates::add);
            }
        }

        return duplicates;
    }

    private static String computePartialHash(Path path) throws IOException, NoSuchAlgorithmException {
        // Implement a method to compute a hash of the first few KBs of the file
        // Similar to computeFullHash but only reads part of the file
    }

    private static String computeFullHash(Path path) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fileContent = Files.readAllBytes(path);
        byte[] hashBytes = digest.digest(fileContent);
        return bytesToHex(hashBytes);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }


}
