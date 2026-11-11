package redactedrice.ptcgr.regression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RegressionSuiteRunnerTest {

    @Test
    void findRandomizerJarPrefersLexicographicallyLatestName(@TempDir Path tempDir)
            throws IOException {
        Path app = tempDir.resolve("app");
        Files.createDirectories(app);
        Path versioned = app.resolve("PtcgRandomizer-0.9.0.jar");
        Files.writeString(versioned, "jar");
        Files.writeString(app.resolve("PtcgRandomizer-0.1.0.jar"), "jar");

        assertEquals(versioned.toFile(), RegressionSuiteRunner.findRandomizerJar(app.toFile()));
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
