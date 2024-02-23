package net.hiralpatel.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class PrintUtil {
    public static String printFocusedDirectoryTree(Path rootPath, List<Path> relevantPaths) throws IOException {
        StringBuilder builder = new StringBuilder();
        Set<Path> relevantDirectories = new HashSet<>();
        for (Path path : relevantPaths) {
            Path relative = rootPath.relativize(path);
            while (relative != null) {
                relevantDirectories.add(rootPath.resolve(relative));
                relative = relative.getParent();
            }
        }

        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            private final AtomicInteger level = new AtomicInteger(0);

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (!relevantDirectories.contains(dir) && level.get() > 0) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                indent(builder, level.getAndIncrement());
                builder.append(dir.getFileName()).append("/\n");
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                level.decrementAndGet();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (relevantPaths.contains(file)) {
                    indent(builder, level.get());
                    builder.append(file.getFileName()).append("\n");
                }
                return FileVisitResult.CONTINUE;
            }

            private void indent(StringBuilder builder, int level) {
                for (int i = 0; i < level; i++) {
                    builder.append("  ");
                }
            }
        });

        return builder.toString();
    }
}
