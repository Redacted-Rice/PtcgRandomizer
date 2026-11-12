package redactedrice.ptcgr.regression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RegressionCleanReinstallTest {

    @Test
    void parseAcceptsAliases() {
        assertEquals(RegressionCleanReinstall.FIRST_CASE,
                RegressionCleanReinstall.parse("first-case"));
        assertEquals(RegressionCleanReinstall.EVERY_CASE, RegressionCleanReinstall.parse("all"));
        assertEquals(RegressionCleanReinstall.NEVER, RegressionCleanReinstall.parse("none"));
    }

    @Test
    void parseRejectsUnknownMode() {
        assertThrows(IllegalArgumentException.class,
                () -> RegressionCleanReinstall.parse("sometimes"));
    }
}
