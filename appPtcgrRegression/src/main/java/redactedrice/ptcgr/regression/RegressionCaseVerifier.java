package redactedrice.ptcgr.regression;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** Checks headless log and work dir against expectations.yaml. */
public final class RegressionCaseVerifier {
    private RegressionCaseVerifier() {
    }

    static int verify(File expectationsFile, File logFile, File workDir) throws IOException {
        if (!expectationsFile.isFile()) {
            System.err.println("Expectations file not found: " + expectationsFile);
            return 1;
        }
        if (!logFile.isFile()) {
            System.err.println("Log file not found: " + logFile);
            return 1;
        }
        if (!workDir.isDirectory()) {
            System.err.println("Work directory not found: " + workDir);
            return 1;
        }

        RegressionExpectations expectations =
                RegressionExpectations.fromYamlMap(RegressionYaml.load(expectationsFile));
        String logText = Files.readString(logFile.toPath(), StandardCharsets.UTF_8);

        List<String> failures = new ArrayList<>();
        for (String needle : expectations.getLogContains()) {
            if (!logText.contains(needle)) {
                failures.add("Log missing expected text: " + needle);
            }
        }
        for (String forbidden : expectations.getLogMustNotContain()) {
            if (logText.contains(forbidden)) {
                failures.add("Log contains forbidden text: " + forbidden);
            }
        }
        for (String relativePath : expectations.getExpectedFiles()) {
            File file = new File(workDir, relativePath);
            if (!file.isFile()) {
                failures.add("Expected file missing: " + file.getAbsolutePath());
            }
        }

        if (failures.isEmpty()) {
            return 0;
        }
        for (String failure : failures) {
            System.err.println(failure);
        }
        return 1;
    }
}
