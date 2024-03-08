package net.hiralpatel.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MimeTypeMapper {
    private static final Map<String, String> mimeTypeMap = new HashMap<>();

    static {
        loadMappings("mime_type_mappings.txt");
    }

    private static void loadMappings(String resourceFileName) {
        try (InputStream inputStream = MimeTypeMapper.class.getClassLoader().getResourceAsStream(resourceFileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    mimeTypeMap.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            // Handle the error appropriately, e.g., log an error or throw a runtime exception
            throw new RuntimeException("Failed to load MIME type mappings from file: " + resourceFileName, e);
        }
    }

    public static String simplifyMimeType(String mimeType) {
        return mimeTypeMap.getOrDefault(mimeType, "Unknown:" + mimeType);
    }
}
