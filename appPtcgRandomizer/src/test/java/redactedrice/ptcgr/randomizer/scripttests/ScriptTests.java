package redactedrice.ptcgr.randomizer.scripttests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import redactedrice.randomizer.scripttests.ScriptTestCli;

class ScriptTests {
    @Test
    void luaScriptTestsPass(TestInfo testInfo) {
        // Install under build so clean removes it. Avoid @TempDir because Lua can leave
        // file handles open on Windows.
        File workDir =
                new File("build/script-tests/" + testInfo.getTestMethod().get().getName());
        workDir.mkdirs();

        assertEquals(0, ScriptTestRunner.run(new String[] { ScriptTestCli.FLAG }, workDir),
                "Lua script tests failed");
    }
}
