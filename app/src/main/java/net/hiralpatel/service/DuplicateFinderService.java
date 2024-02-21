package net.hiralpatel.service;

import net.hiralpatel.model.*;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DuplicateFinderService {
    private final Map<Long, List<HashNameKey>> filesAndDirectoryMap = new ConcurrentHashMap<>(8192);

    private final Map<FileSystemObject, Boolean> currentDuplicates = new HashMap<>();

    public void findAndDisplayDuplicates(String... directoryPaths) {
        // Populate the duplicatesMap with all FileSystemObjects from the provided paths
        // Assuming a constructor that takes path
        logMemoryUsage("Start");
        Arrays.stream(directoryPaths).map(Path::of)
                .map(Directory::new)
                .peek(dir -> logInfo("Created:" + dir + ",Dir size:" + dir.getSize()))
                .forEach(this::identifyAndGroupInitialDuplicateFiles);
        logMemoryUsage("End");

        // Mark duplicates in the duplicatesMap
        updateDuplicatesMap(filesAndDirectoryMap);
//
        // Report duplicates, starting with top-level directories
        // Assuming a constructor that takes path
//        Arrays.stream(directoryPaths).map(Directory::new).forEach(directory -> reportIfDuplicate(directory, null));
    }

    private void updateDuplicatesMap(Map<Long, List<HashNameKey>> filesAndDirectoryMap) {
        filesAndDirectoryMap
                .values()
                .stream()
                .filter(list->list.size() > 1)
                .map(this::recalculateAndUpdateDuplicate)
                .toList();

    }

    private Map<String,List<HashNameKey>> recalculateAndUpdateDuplicate(List<HashNameKey> list) {
//        list.forEach(item -> calculateHash(item));
        return new HashMap<>();
    }



    public void identifyAndGroupInitialDuplicateFiles(FileSystemObject root) {
        Deque<FileSystemObject> fsObjectQueue = new ArrayDeque<>();
        fsObjectQueue.push(root);
        while (!fsObjectQueue.isEmpty()) {
            FileSystemObject current = fsObjectQueue.pop();
            if (current instanceof Directory currentDir) {
                currentDir.getChildren().forEach(fsObjectQueue::push);

            }
            updateFsObjectMap(current);
        }
    }

    private void logMemoryUsage(String phase) {
        Runtime runtime = Runtime.getRuntime();
        double totalMB = runtime.totalMemory() / 1024.0 / 1024.0;
        double usedMB = (runtime.totalMemory() - runtime.freeMemory()) / 1024.0 / 1024.0;
        System.out.printf("%s: %s - Mem: Total=%.2fMB, Used=%.2fMB%n", LocalDateTime.now(), phase, totalMB, usedMB);
    }

    private void logInfo(String message) {
        System.out.printf("%s: %s%n", LocalDateTime.now(), message);
    }


    private void updateFsObjectMap(FileSystemObject fileSystemObject) {
        long sizeOfFile = fileSystemObject.getSize();
        filesAndDirectoryMap.computeIfAbsent(
                sizeOfFile,
                k -> new ArrayList<>()).add(new HashNameKey(String.valueOf(sizeOfFile), fileSystemObject.getPath()));
    }




    private void reportIfDuplicate(FileSystemObject obj, FileSystemObject parent) {
        if (obj instanceof Duplicatable && ((Duplicatable) obj).isDuplicate()) {
            if (!(parent instanceof Duplicatable) || !((Duplicatable) parent).isDuplicate()) {
                System.out.println("Duplicate found: " + obj.getPath());
            }
        }

        if (obj instanceof Directory) {
            for (FileSystemObject child : ((Directory) obj).getChildren()) {
                reportIfDuplicate(child, obj);
            }
        }
    }
}
