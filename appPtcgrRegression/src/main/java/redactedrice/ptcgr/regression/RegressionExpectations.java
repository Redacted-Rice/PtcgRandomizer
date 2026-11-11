package redactedrice.ptcgr.regression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Parsed regression case expectations YAML. */
public final class RegressionExpectations {
    static final String LOG_CONTAINS_KEY = "logContains";
    static final String LOG_MUST_NOT_CONTAIN_KEY = "logMustNotContain";
    static final String EXPECTED_FILES_KEY = "expectedFiles";

    static final List<String> DEFAULT_LOG_CONTAINS = List.of("Wrote patch:");
    static final List<String> DEFAULT_LOG_MUST_NOT_CONTAIN =
            List.of("[ERROR ]", "Exception", " FAIL");
    static final List<String> DEFAULT_EXPECTED_FILES = List.of("ptcg_randomized.bps");

    private final List<String> logContains;
    private final List<String> logMustNotContain;
    private final List<String> expectedFiles;

    RegressionExpectations(List<String> logContains, List<String> logMustNotContain,
            List<String> expectedFiles) {
        this.logContains = List.copyOf(logContains);
        this.logMustNotContain = List.copyOf(logMustNotContain);
        this.expectedFiles = List.copyOf(expectedFiles);
    }

    public List<String> getLogContains() {
        return logContains;
    }

    public List<String> getLogMustNotContain() {
        return logMustNotContain;
    }

    public List<String> getExpectedFiles() {
        return expectedFiles;
    }

    public static RegressionExpectations fromYamlMap(Map<String, Object> root) {
        if (root == null) {
            root = Map.of();
        }
        List<String> logContains = root.containsKey(LOG_CONTAINS_KEY)
                ? readStringList(root.get(LOG_CONTAINS_KEY))
                : DEFAULT_LOG_CONTAINS;
        List<String> logMustNotContain = root.containsKey(LOG_MUST_NOT_CONTAIN_KEY)
                ? readStringList(root.get(LOG_MUST_NOT_CONTAIN_KEY))
                : DEFAULT_LOG_MUST_NOT_CONTAIN;
        List<String> expectedFiles = root.containsKey(EXPECTED_FILES_KEY)
                ? readStringList(root.get(EXPECTED_FILES_KEY))
                : DEFAULT_EXPECTED_FILES;
        return new RegressionExpectations(logContains, logMustNotContain, expectedFiles);
    }

    private static List<String> readStringList(Object raw) {
        if (raw == null) {
            return List.of();
        }
        if (!(raw instanceof List<?> list)) {
            throw new IllegalArgumentException("Expected a YAML list for string entries");
        }
        List<String> out = new ArrayList<>();
        for (Object item : list) {
            if (item == null) {
                continue;
            }
            String text = item.toString();
            if (!text.isBlank()) {
                out.add(text);
            }
        }
        return out;
    }
}
