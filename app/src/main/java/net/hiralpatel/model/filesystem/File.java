package net.hiralpatel.model.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class File implements FileSystemObject {
    private final Path path;
    private final AtomicLong size = new AtomicLong(-1); // Use -1 to indicate that size is not yet computed
    private final Lock sizeLock = new ReentrantLock(); // Lock for size calculation
    private String hash;
    private boolean isDuplicate = false;

    public File(Path filePath) {
        this.path = filePath;
        refreshSize();
    }

    @Override
    public String getName() {
        return path.getFileName().toString();
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public long getSize() {
        return size.get();
    }

    private void calculateSize() {
        long currentSize = size.get();
        if (currentSize < 0) { // Size not yet computed
            sizeLock.lock(); // Acquire the lock
            try {
                // Double-check to avoid race conditions
                currentSize = size.get();
                if (currentSize < 0) {
                    size.set(Files.size(path));
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            } finally {
                sizeLock.unlock(); // Ensure the lock is always released
            }
        }
    }

    public void refreshSize() {
        size.set(-1);
        calculateSize();
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
