package net.hiralpatel.aggregation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DirectoryAggregator {
    public static Path findCommonAncestor(List<Path> paths) {
        if (paths == null || paths.isEmpty()) {
            return null; // Return null for null or empty input list
        }

        if (paths.size() == 1) {
            return paths.get(0); // Return the single path as its own ancestor
        }

        Path commonAncestor = paths.get(0);

        for (Path path : paths) {
            commonAncestor = findCommonAncestorForTwo(commonAncestor, path);
            if (commonAncestor == null) {
                break; // No common ancestor found
            }
        }

        // Ensure the resulting path starts with "/" if the original paths were absolute
        if (commonAncestor != null && paths.get(0).isAbsolute() && !commonAncestor.startsWith("/")) {
            commonAncestor = Paths.get("/").resolve(commonAncestor);
        }

        return commonAncestor;
    }

    private static Path findCommonAncestorForTwo(Path path1, Path path2) {
        int count = Math.min(path1.getNameCount(), path2.getNameCount());
        Path result = Paths.get("");

        for (int i = 0; i < count; i++) {
            if (path1.getName(i).equals(path2.getName(i))) {
                result = result.resolve(path1.getName(i));
            } else {
                break;
            }
        }

        return result.getNameCount() > 0 ? result : null;
    }
}
