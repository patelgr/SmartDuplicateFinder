package net.hiralpatel.service;

import net.hiralpatel.cli.AppCli;
import net.hiralpatel.model.Directory;
import net.hiralpatel.model.FileSystemObject;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DuplicateFinderService {
    // Maps file size to a list of FileSystemObjects with that size

    public List<FileResult> findDuplicates(List<String> directoryPaths) {
        List<Directory> directories = directoryPaths.stream()
                .map(Path::of)
                .map(Directory::new)
                .peek(dir -> AppCli.logInfo(String.format("Initialized: %s, Size: %d", dir, dir.getSize())))
                .toList();

        final Map<Long, List<FileSystemObject>> sizeToFileObjectsMap = directories.stream()
                .map(this::groupPotentialDuplicatesBySize)
                .reduce(new ConcurrentHashMap<>(), (acc, map) -> {
                    map.forEach((key, value) -> acc.merge(key, value, (existingList, newList) -> {
                        existingList.addAll(newList);
                        return existingList;
                    }));
                    return acc;
                });

        Map<Long, List<FileSystemObject>> verifiedDuplicates = verifyAndFilterDuplicates(sizeToFileObjectsMap);
        markDuplicates(verifiedDuplicates);


        Map<Long,List<Path>> duplicateResults = new HashMap<>();
        verifiedDuplicates.forEach((size, fsObjects) -> {
            List<Path> paths = fsObjects.stream().map(FileSystemObject::getPath).toList();
            if (!paths.isEmpty()) {
                duplicateResults.put(size,paths); // Assuming all verified duplicates are to be grouped together
            }
        });

        // New code to identify unique files
        Map<Long,List<Path>> uniqueResults = new HashMap<>();
        sizeToFileObjectsMap.forEach((size, fsObjects) -> {
            if (fsObjects.size() == 1) { // This means the file is unique
                List<Path> paths = fsObjects.stream().map(FileSystemObject::getPath).toList();
                uniqueResults.put(size,paths);
            }
        });

        List<FileResult> results = new ArrayList<>();
        results.add(new DuplicateFileGroup(duplicateResults));
        results.add(new UniqueFileGroup(uniqueResults));

        return results;
    }


    public void searchAndReportDuplicates(String... directoryPaths) {
        findDuplicates(Arrays.asList(directoryPaths));
    }


    private Map<Long, List<FileSystemObject>> groupPotentialDuplicatesBySize(FileSystemObject root) {
        final Map<Long, List<FileSystemObject>> sizeToFileObjectsMap = new HashMap<>();
        Deque<FileSystemObject> traversalStack = new ArrayDeque<>();
        traversalStack.push(root);

        while (!traversalStack.isEmpty()) {
            FileSystemObject current = traversalStack.pop();
            if (current instanceof Directory) {
                traversalStack.addAll(((Directory) current).getChildren());
            }
            sizeToFileObjectsMap.computeIfAbsent(current.getSize(), k -> new ArrayList<>()).add(current);
        }
        return sizeToFileObjectsMap;
    }

    private Map<Long, List<FileSystemObject>> verifyAndFilterDuplicates(Map<Long, List<FileSystemObject>> potentialDuplicates) {
        Map<Long, List<FileSystemObject>> confirmedDuplicates = new HashMap<>();
        potentialDuplicates.forEach((size, fsObjects) -> {
            if (fsObjects.size() > 1) {
                List<FileSystemObject> duplicateFiles = HashingService.processAndIdentifyDuplicates(fsObjects).stream()
                        .filter(FileSystemObject::isDuplicate)
                        .toList();

                if (!duplicateFiles.isEmpty()) {
                    confirmedDuplicates.put(size, duplicateFiles);
                }
            }
        });
        return confirmedDuplicates;
    }

    private void markDuplicates(Map<Long, List<FileSystemObject>> duplicates) {
        duplicates.values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .forEach(fsObject -> {
                    if (fsObject != null) {
                        fsObject.setDuplicate(true);
                    }
                });
    }

}
