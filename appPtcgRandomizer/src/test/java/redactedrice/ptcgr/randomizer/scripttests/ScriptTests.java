package redactedrice.ptcgr.randomizer.scripttests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import redactedrice.randomizer.LuaRandomizerWrapper;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
class ScriptTests {
    private File workDir;
    private LuaRandomizerWrapper wrapper;

    @BeforeAll
    void setUp() {
        // Install under build so clean removes it. Avoid @TempDir because Lua can leave
        // file handles open on Windows.
        workDir = new File("build/script-tests");
        workDir.mkdirs();
        wrapper = ScriptTestRunner.prepareForScriptTests(workDir);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("caseFiles")
    void luaScriptTest(String caseFile) {
        assertEquals(0, ScriptTestRunner.runCase(caseFile, workDir, wrapper),
                () -> "Lua script tests failed in " + caseFile);
    }

    Stream<String> caseFiles() {
        return ScriptTestRunner.bundledCaseFileNames().stream();
    }
}
