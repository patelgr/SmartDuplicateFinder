package net.hiralpatel.duplication;

import net.hiralpatel.aggregation.DirectoryAggregator;
import net.hiralpatel.analytics.StepWrapper;
import net.hiralpatel.model.filesystem.Directory;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class responsible for finding duplicate files within a given set of paths.
 */
public class DuplicateFinder {

    /**
     * Finds and returns duplicates among the provided file paths.
     *
     * @param paths A list of paths to scan for duplicate files.
     * @return A map where keys are file sizes and values are lists of paths to duplicate files of that size.
     */
    public Map<String, List<Path>> findDuplicates(List<Path> paths) {
        StepWrapper wrapper = new StepWrapper();

        // Step 1: Find common ancestor

        Path commonAncestor = wrapper.execute("Find Common Ancestor", () ->
                DirectoryAggregator.findCommonAncestor(paths)
        );

        // Step 2: Scan for potential duplicates based on file size
        Map<Long, List<Path>> filesBySize = wrapper.execute("Scan Duplicates by Size", () ->
                FileScanner.getDuplicatesBySize(commonAncestor, paths)
        );

        // Step 3: Refine duplicate detection by hashing
        Map<String, List<Path>> fileHashes = wrapper.execute("Hashing for Duplicate Detection", () ->
                HashScanner.scanForFileDuplicates(filesBySize)
        );

        // Step 4: Build directory structure with confirmed duplicates
        Directory rootDirectory = wrapper.execute("Build Directory Structure", () ->
                new Directory(commonAncestor, fileHashes)
        );

        // Step 5: Extract duplicates from the directory structure

        Map<String, List<Path>> extractDuplicates = wrapper.execute("Extract Duplicates", () ->
                DuplicateExtractor.extract(rootDirectory)
        );

        StepWrapper.displayStatsReport();
        return extractDuplicates;
    }


    /**
     * Flattens a map of file hashes to a list of file paths, discarding the hash values.
     *
     * @param fileHashes A map where keys are file hashes and values are lists of file paths with that hash.
     * @return A list of file paths that were grouped by their hash values.
     */
    private List<Path> flattenFileHashes(Map<String, List<Path>> fileHashes) {
        return fileHashes.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

}
