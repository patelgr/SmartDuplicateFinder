package net.hiralpatel.model.filesystem;


import net.hiralpatel.monitoring.EventPublisher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

    public Directory(Path directoryPath, List<Path> fileDuplicates) {
        eventPublisher.publishEvent("Creating Dir:" + directoryPath);
        this.directoryPath = directoryPath;
        this.children = new ArrayList<>();
        initializeChildren(fileDuplicates);
        refreshSize();
        checkAndMarkDuplicate();
    }


    private void initializeChildren(List<Path> fileDuplicates) {
        try (Stream<Path> paths = Files.walk(directoryPath, 1)) {
            paths.filter(path -> !path.equals(directoryPath)) // Exclude the directory itself
                    .filter(path -> !path.getFileName().toString().equals(".DS_Store")) // Exclude .DS_Store files
                    .forEach(path -> {
                        FileSystemObject child;
                        if (Files.isDirectory(path)) {
                            child = new Directory(path, fileDuplicates);
                        } else {
                            child = new File(path);
                            // Check if the file is marked as a duplicate in fileDuplicates
                            for (Path d : fileDuplicates) {
                                if (d.equals(path)) {
                                    child.setDuplicate(true);
                                    break;
                                }
                            }
                        }
                        children.add(child);
                    });
        } catch (IOException e) {
            System.out.println("Error reading directory: " + e.getMessage());
        }
    }

    private void calculateSize() {
        long currentSize = size.get();
        if (currentSize < 0) {
            sizeLock.lock();
            try {
                currentSize = size.get();
                if (currentSize < 0) {
                    currentSize = children.stream().mapToLong(FileSystemObject::getSize).sum();
                    size.set(currentSize);
                }
            } finally {
                sizeLock.unlock(); // Ensure the lock is always released
            }
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
    }

    @Override
    public boolean isDuplicate() {
        return isDuplicate;
    }

    @Override
    public void setDuplicate(boolean duplicate) {
        isDuplicate = duplicate;
    }
}
