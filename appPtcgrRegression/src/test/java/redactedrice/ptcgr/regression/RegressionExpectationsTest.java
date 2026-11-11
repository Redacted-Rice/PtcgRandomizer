package redactedrice.ptcgr.regression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

class RegressionExpectationsTest {

    @Test
    void appliesDefaultsWhenKeysOmitted() {
        RegressionExpectations expectations = RegressionExpectations.fromYamlMap(Map.of());

        assertEquals(RegressionExpectations.DEFAULT_LOG_CONTAINS, expectations.getLogContains());
        assertEquals(RegressionExpectations.DEFAULT_LOG_MUST_NOT_CONTAIN,
                expectations.getLogMustNotContain());
        assertEquals(RegressionExpectations.DEFAULT_EXPECTED_FILES, expectations.getExpectedFiles());
    }

    @Test
    void emptyListOptsOutOfThatCheck() {
        RegressionExpectations expectations = RegressionExpectations.fromYamlMap(Map.of(
                RegressionExpectations.LOG_CONTAINS_KEY, java.util.List.of(),
                RegressionExpectations.LOG_MUST_NOT_CONTAIN_KEY, java.util.List.of(),
                RegressionExpectations.EXPECTED_FILES_KEY, java.util.List.of()));

        assertTrue(expectations.getLogContains().isEmpty());
        assertTrue(expectations.getLogMustNotContain().isEmpty());
        assertTrue(expectations.getExpectedFiles().isEmpty());
    }
}
