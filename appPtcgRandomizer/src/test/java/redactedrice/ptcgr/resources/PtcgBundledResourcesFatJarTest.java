package redactedrice.ptcgr.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import redactedrice.ptcgr.constants.PtcgRandomizerVersion;

class PtcgBundledResourcesFatJarTest {

    @Test
    void fatJarExtractsBundledResources(@TempDir Path tempDir) throws Exception {
        Path jar = findFatJar();
        assertTrue(Files.isRegularFile(jar), "Runnable JAR missing: " + jar);

        String javaBin = System.getProperty("java.home") + File.separator + "bin"
                + File.separator + "java";
        Process process = new ProcessBuilder(javaBin, "-cp", jar.toAbsolutePath().toString(),
                "redactedrice.ptcgr.resources.PtcgBundledResources",
                tempDir.toAbsolutePath().toString()).inheritIO().start();

        assertEquals(0, process.waitFor(), "Resource install from runnable JAR failed");
        PtcgBundledResources.verifyInstalled(tempDir.toFile());
    }

    private static Path findFatJar() throws Exception {
        Path appDir = Path.of("app");
        String expectedName = "PtcgRandomizer-" + PtcgRandomizerVersion.VERSION + ".jar";
        Path versionedJar = appDir.resolve(expectedName);
        if (Files.isRegularFile(versionedJar)) {
            return versionedJar;
        }
        try (Stream<Path> jars = Files.list(appDir)) {
            return jars.filter(path -> path.getFileName().toString().startsWith("PtcgRandomizer-")
                    && path.getFileName().toString().endsWith(".jar"))
                    .max(java.util.Comparator.comparing(path -> path.getFileName().toString()))
                    .orElseThrow(() -> new IllegalStateException(
                            "No runnable JAR found in " + appDir.toAbsolutePath()));
        }
    }
}
