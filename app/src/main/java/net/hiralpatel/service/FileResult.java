package net.hiralpatel.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

// Sealed interface declaration
public sealed interface FileResult permits DuplicateFileGroup, ErrorFileGroup, UniqueFileGroup, UniqueNotReportedFileGroup {
    FileStatus getStatus();
    Map<Long,List<Path>> getFiles();
}

// Record implementation for duplicate files
record DuplicateFileGroup(Map<Long,List<Path>> files) implements FileResult {
    public FileStatus getStatus() {
        return FileStatus.DUPLICATE;
    }

    public Map<Long,List<Path>> getFiles() {
        return files;
    }
}

// Record implementation for unique files
record UniqueFileGroup(Map<Long,List<Path>> files) implements FileResult {
    public FileStatus getStatus() {
        return FileStatus.UNIQUE;
    }

    public Map<Long,List<Path>> getFiles() {
        return files;
    }
}

record UniqueNotReportedFileGroup(Map<Long,List<Path>> files) implements FileResult {
    public FileStatus getStatus() {
        return FileStatus.UNIQUE_NOT_REPORTED;
    }

    public Map<Long,List<Path>> getFiles() {
        return files;
    }
}

// Record implementation for files that had an error
record ErrorFileGroup(Map<Long,List<Path>> files) implements FileResult {
    public FileStatus getStatus() {
        return FileStatus.ERROR;
    }

    public Map<Long,List<Path>> getFiles() {
        return files;
    }
}