package redactedrice.ptcgr.utils;

import java.io.File;

public final class FileExtensionUtils {
    private FileExtensionUtils() {}

    public static File ensureExtension(File file, String extension) {
        if (!extension.startsWith(".")) {
            throw new IllegalArgumentException("extension must start with '.'");
        }

        String name = file.getName();
        String alternateExtension = alternateExtension(extension);
        if (alternateExtension != null && name.endsWith(alternateExtension)) {
            return new File(file.getParentFile(),
                    name.substring(0, name.length() - alternateExtension.length()) + extension);
        }
        if (!name.endsWith(extension)) {
            return new File(file.getPath() + extension);
        }
        return file;
    }

    private static String alternateExtension(String extension) {
        if (".yaml".equals(extension)) {
            return ".yml";
        }
        return null;
    }
}
