package redactedrice.ptcgr.regression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RegressionSuiteRunnerTest {

    @Test
    void findRandomizerJarPrefersHighestVersion(@TempDir Path tempDir) throws IOException {
        Path app = tempDir.resolve("app");
        Files.createDirectories(app);
        Path lowerVersion = app.resolve("PtcgRandomizer-0.9.0.jar");
        Path higherVersion = app.resolve("PtcgRandomizer-0.10.0.jar");
        Files.writeString(lowerVersion, "jar");
        Files.writeString(higherVersion, "jar");
        // newer mtime on the older version should not win
        Files.setLastModifiedTime(lowerVersion, FileTime.from(Instant.parse("2025-01-01T00:00:00Z")));
        Files.setLastModifiedTime(higherVersion, FileTime.from(Instant.parse("2020-01-01T00:00:00Z")));

        assertEquals(higherVersion.toFile(), RegressionSuiteRunner.findRandomizerJar(app.toFile()));
    }

    @Test
    void findRandomizerJarReturnsNullWhenAppDirMissing(@TempDir Path tempDir) throws IOException {
        assertNull(RegressionSuiteRunner.findRandomizerJar(tempDir.resolve("missing").toFile()));
    }

    @Test
    void rejectsUnknownArgs() {
        assertEquals(RegressionSuiteRunner.USAGE_ERROR,
                RegressionSuiteRunner.run(new String[] {"--nope"}));
    }
}
