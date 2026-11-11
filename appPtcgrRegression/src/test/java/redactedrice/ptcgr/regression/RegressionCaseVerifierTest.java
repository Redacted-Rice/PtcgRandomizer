package redactedrice.ptcgr.regression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RegressionCaseVerifierTest {

    @Test
    void verifyPassesWhenLogAndFilesMatch(@TempDir Path tempDir) throws IOException {
        Path workDir = tempDir.resolve("work");
        Files.createDirectories(workDir);
        Files.writeString(workDir.resolve("ptcg_randomized.bps"), "patch");

        Path log = tempDir.resolve("run.log");
        Files.writeString(log, """
                [INFO ] : hp_cards_together_and_stage
                HP table
                Wrote patch: somewhere
                """);

        Path expectations = tempDir.resolve("expectations.yaml");
        Files.writeString(expectations, """
                logContains:
                  - "Wrote patch:"
                  - "hp_cards_together_and_stage"
                  - "HP"
                logMustNotContain:
                  - "[ERROR ]"
                expectedFiles:
                  - ptcg_randomized.bps
                """);

        assertEquals(0, RegressionCaseVerifier.verify(expectations.toFile(), log.toFile(),
                workDir.toFile()));
    }

    @Test
    void verifyFailsOnMissingLogLineAndForbiddenText(@TempDir Path tempDir) throws IOException {
        Path workDir = tempDir.resolve("work");
        Files.createDirectories(workDir);

        Path log = tempDir.resolve("run.log");
        Files.writeString(log, "[ERROR ] something broke\n");

        Path expectations = tempDir.resolve("expectations.yaml");
        Files.writeString(expectations, """
                logContains:
                  - "Wrote patch:"
                logMustNotContain:
                  - "[ERROR ]"
                """);

        assertEquals(1, RegressionCaseVerifier.verify(expectations.toFile(), log.toFile(),
                workDir.toFile()));
    }
}
