package redactedrice.ptcgr.randomizer.scripttests;

import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.File;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import redactedrice.randomizer.LuaRandomizerWrapper;
import redactedrice.randomizer.scripttests.ScriptTestFailure;
import redactedrice.randomizer.scripttests.ScriptTestRunResult;

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
        ScriptTestRunResult result = ScriptTestRunner.runCaseFile(caseFile, workDir, wrapper);
        if (result.isSuccess()) {
            return;
        }
        assertAll(result.failures().stream().map(ScriptTests::failureExecutable)
                .toArray(Executable[]::new));
    }

    private static Executable failureExecutable(ScriptTestFailure failure) {
        return () -> {
            throw new AssertionError(failure.displayName() + ": " + failure.message());
        };
    }

    Stream<String> caseFiles() {
        return ScriptTestRunner.bundledCaseFileNames().stream();
    }
}
