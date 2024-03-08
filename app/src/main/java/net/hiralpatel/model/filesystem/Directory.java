package net.hiralpatel.model.filesystem;


import net.hiralpatel.monitoring.EventPublisher;
import net.hiralpatel.monitoring.Events;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class Directory implements FileSystemObject {

    private static final EventPublisher eventPublisher = EventPublisher.INSTANCE;

    private final Path directoryPath;
    private final List<FileSystemObject> children;
    private final AtomicLong size = new AtomicLong(-1); // Use -1 to indicate that size is not yet computed
    private final Lock sizeLock = new ReentrantLock();
    private boolean isDuplicate = false;

    public Directory(Path directoryPath, Map<String, List<Path>> fileDuplicates) {
        this.directoryPath = directoryPath;
        int initialCapacity = countEntriesInDirectory(directoryPath);
        eventPublisher.publishEvent(Events.InfoEvent("Creating directory: " + directoryPath + " with " + initialCapacity + " initial entries."));
        this.children = new ArrayList<>(initialCapacity);
        try {
            initializeChildren(fileDuplicates);
            refreshSize();
            checkAndMarkDuplicate();
        } catch (Exception e) {
            eventPublisher.publishEvent(Events.InfoEvent("Error initializing directory " + directoryPath + ": " + e.getMessage()));
        }
        eventPublisher.publishEvent(Events.InfoEvent("Directory created: " + directoryPath + " with " + children.size() + " children."));
    }

    private static int countEntriesInDirectory(Path directoryPath) {
        try (Stream<Path> stream = Files.list(directoryPath)) {
            return (int) stream.count();
        }catch (Exception e){
            return 0;
        }
    }

    private void initializeChildren(Map<String, List<Path>> fileDuplicates) {
        eventPublisher.publishEvent(Events.InfoEvent("Initializing children for directory: " + directoryPath));
        try (Stream<Path> paths = Files.walk(directoryPath, 1)) {
            paths.filter(path -> !path.equals(directoryPath))
                    .filter(path -> !path.getFileName().toString().equals(".DS_Store"))
                    .forEach(path -> {
                        FileSystemObject child;
                        try {
                            if (Files.isDirectory(path)) {
                                child = new Directory(path, fileDuplicates);
                            } else {
                                child = new File(path);
                                fileDuplicates.forEach((hash, pathsList) -> {
                                    if (pathsList.contains(path)) {
                                        child.setDuplicate(true);
                                        child.setHash(hash);
                                    }
                                });
                            }
                            children.add(child);
                            eventPublisher.publishEvent(Events.InfoEvent("Added child: " + path));
                        } catch (Exception e) {
                            eventPublisher.publishEvent(Events.InfoEvent("Error adding child: " + path + " - " + e.getMessage()));
                        }
                    });
        } catch (IOException e) {
            eventPublisher.publishEvent(Events.InfoEvent("Error initializing children for directory " + directoryPath + ": " + e.getMessage()));
        }
    }


    private void calculateSize() {
        long currentSize = size.get();
        if (currentSize < 0) {
            eventPublisher.publishEvent(Events.InfoEvent("Calculating size for directory: " + directoryPath));
            sizeLock.lock();
            try {
                currentSize = size.get();
                if (currentSize < 0) {
                    currentSize = children.stream().mapToLong(FileSystemObject::getSize).sum();
                    size.set(currentSize);
                    eventPublisher.publishEvent(Events.InfoEvent("Size calculated for directory: " + directoryPath + " - Size: " + currentSize));
                } else {
                    eventPublisher.publishEvent(Events.InfoEvent("Size calculation skipped for directory: " + directoryPath + " - Already calculated."));
                }
            } finally {
                sizeLock.unlock();
            }
        } else {
            eventPublisher.publishEvent(Events.InfoEvent("Size retrieval for directory: " + directoryPath + " - Size: " + currentSize));
        }
    }


    @Override
    public String getName() {
        return directoryPath.getFileName().toString();
    }

    @Override
    public Path getPath() {
        return directoryPath;
    }

    @Override
    public long getSize() {
        return size.get();
    }

    public List<FileSystemObject> getChildren() {
        return children;
    }

    // Provide a method to reset or update the cached size if needed
    public void refreshSize() {
        size.set(-1);
        calculateSize();
    }

    // Method to check if the directory itself is a duplicate (all children are duplicates)
    public void checkAndMarkDuplicate() {
        isDuplicate = children.stream().allMatch(FileSystemObject::isDuplicate);
        eventPublisher.publishEvent(Events.InfoEvent("Duplicate check for directory: " + directoryPath + " - Is duplicate: " + isDuplicate));
    }


    @Override
    public boolean isDuplicate() {
        return isDuplicate;
    }

    @Override
    public void setDuplicate(boolean duplicate) {
        isDuplicate = duplicate;
    }

    private String hash;
    @Override
    public void setHash(String key) {
        // Use the directory size to generate the hash
        String sizeStr = String.valueOf(getSize());
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(sizeStr.getBytes(StandardCharsets.UTF_8));
            this.hash = bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // Fallback to a simpler hash in case SHA-256 is not available
            this.hash = String.valueOf(sizeStr.hashCode());
        }
    }

    // Helper method to convert byte array into a hexadecimal string
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

    @Override
    public String getHash() {
        return hash;
    }
}
