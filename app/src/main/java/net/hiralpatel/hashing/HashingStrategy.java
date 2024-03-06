package net.hiralpatel.hashing;


import java.io.IOException;
import java.nio.file.Path;

public interface HashingStrategy {

    String hashFileBySize(Path filePath, long size) throws IOException;
}
