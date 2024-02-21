package net.hiralpatel.model;

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


    private final Path directoryPath;
    private final List<FileSystemObject> children;
    private final AtomicLong size = new AtomicLong(-1); // Use -1 to indicate that size is not yet computed

    @Override
    public String toString() {
        return "Directory{" + "directoryPath=" + directoryPath + '}';
    }


    public Directory(Path directoryPath) {
        this.directoryPath = directoryPath;
        this.children = new ArrayList<>();
        initializeChildren();
        refreshSize();
    }

    private void initializeChildren() {
        // Use try-with-resources to ensure the stream is closed properly
        try (Stream<Path> paths = Files.walk(directoryPath, 1)) { // Limit depth to 1 for immediate children
            paths.filter(path -> !path.equals(directoryPath)) // Exclude the directory itself
                    .forEach(path -> {
                        if (Files.isDirectory(path)) {
                            children.add(new Directory(path));
                        } else {
                            children.add(new File(path));
                        }
                    });
        } catch (IOException e) {
            System.out.println("Error reading directory: " + e.getMessage());
        }
    }
    private final Lock sizeLock = new ReentrantLock(); // Lock for size calculation

    private void calculateSize() {
        long currentSize = size.get();
        if (currentSize < 0) { // Size not yet computed
            sizeLock.lock(); // Acquire the lock
            try {
                // Double-check to avoid race conditions
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
}
