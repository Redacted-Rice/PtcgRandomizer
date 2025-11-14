package redactedrice.ptcgr.randomizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

// TODO: Make a common extractor for this an universal randomizer Java
public class PtcgrResourceExtractor {
    private static final String RANDOMIZER_RESOURCE_PATH = "modules";

    // Map of subdirectory to files in it
    private static final Map<String, String[]> MODULE_FILES = new HashMap<>();

    static {
        MODULE_FILES.put("actions", new String[] {"randomize_hp.lua"});
        MODULE_FILES.put("prescripts",
                new String[] {"changedetector_setup.lua", "changedetector_snapshot.lua"});
        MODULE_FILES.put("postscripts", new String[] {"changedetector_detect.lua"});
    }

    private static String extractionPath = "modules";

    public static void setPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }
        extractionPath = path;
    }

    public static String getPath() {
        return extractionPath;
    }

    public static void extract(boolean overwriteExisting) throws IOException {
        File targetDir = new File(extractionPath);
        if (!targetDir.exists()) {
            if (!targetDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + extractionPath);
            }
        }

        if (!targetDir.isDirectory()) {
            throw new IOException("Target path is not a directory: " + extractionPath);
        }

        // Extract all module files by subdirectory
        for (Map.Entry<String, String[]> entry : MODULE_FILES.entrySet()) {
            String subdirectory = entry.getKey();
            String[] files = entry.getValue();

            // Create subdirectory
            File subDir = new File(targetDir, subdirectory);
            if (!subDir.exists()) {
                if (!subDir.mkdirs()) {
                    throw new IOException("Failed to create directory: " + subDir.getPath());
                }
            }

            // Extract each file in the subdirectory
            for (String fileName : files) {
                extractFile(subdirectory, fileName, subDir, overwriteExisting);
            }
        }
    }

    private static void extractFile(String subdirectory, String fileName, File targetDir,
            boolean overwriteExisting) throws IOException {
        File targetFile = new File(targetDir, fileName);

        // Skip if file exists and were not overwriting
        if (targetFile.exists() && !overwriteExisting) {
            return;
        }

        String resourcePath = RANDOMIZER_RESOURCE_PATH + "/" + subdirectory + "/" + fileName;
        InputStream resourceStream =
                PtcgrResourceExtractor.class.getClassLoader().getResourceAsStream(resourcePath);

        if (resourceStream == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        try {
            Path targetPath = targetFile.toPath();
            if (overwriteExisting) {
                Files.copy(resourceStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(resourceStream, targetPath);
            }
        } finally {
            resourceStream.close();
        }
    }
}

