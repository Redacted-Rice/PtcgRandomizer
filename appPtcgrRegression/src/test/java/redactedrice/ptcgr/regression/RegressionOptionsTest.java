package redactedrice.ptcgr.regression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;

import org.junit.jupiter.api.Test;

class RegressionOptionsTest {

    @Test
    void parsesCaseFilterAndCleanReinstall() {
        RegressionOptions options = RegressionOptions.fromArgs(new String[] {"/app",
                "--case", "hp_randomize", "--clean-reinstall", "never"});

        assertEquals(new File("/app").getAbsoluteFile(), options.appDir);
        assertEquals("hp_randomize", options.caseFilter);
        assertEquals(RegressionCleanReinstall.NEVER, options.cleanReinstall);
    }

    @Test
    void defaultsCleanReinstallToFirstCase() {
        RegressionOptions options = RegressionOptions.fromArgs(new String[] {"/app"});

        assertNull(options.caseFilter);
        assertEquals(RegressionCleanReinstall.FIRST_CASE, options.cleanReinstall);
    }
}
