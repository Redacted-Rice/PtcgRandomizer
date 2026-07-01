package redactedrice.ptcgr.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FatJarResourceInstallTest {

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
        Path libsDir = Path.of("build/libs");
        try (Stream<Path> jars = Files.list(libsDir)) {
            return jars.filter(path -> path.getFileName().toString().endsWith("-all.jar"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No -all.jar found in " + libsDir.toAbsolutePath()));
        }
    }
}
