package redactedrice.ptcgr.randomizer.launch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import redactedrice.ptcgr.configs.AppPreferences;
import redactedrice.ptcgr.randomizer.RandomizerCore;
import redactedrice.ptcgr.randomizer.scripttests.PtcgScriptTestFixtures;
import redactedrice.ptcgr.resources.PtcgBundledResources;
import redactedrice.randomizer.LuaRandomizerWrapper;
import redactedrice.randomizer.scripttests.ScriptTestBatchRunner;
import redactedrice.randomizer.scripttests.ScriptTestCaseFiles;
import redactedrice.randomizer.scripttests.ScriptTestFailure;
import redactedrice.randomizer.scripttests.ScriptTestFixtures;
import redactedrice.randomizer.scripttests.ScriptTestRunResult;
import redactedrice.randomizer.scripttests.ScriptTestWrapperFactory;
import redactedrice.randomizer.utils.Logger;

// PTCG host for URJ script tests.
public final class ScriptTestRunner {
    private ScriptTestRunner() {
    }

    public static int run(CommandLineArgs launch) {
        return run(launch, AppPreferences.resolveAppDirectory());
    }

    public static int run(CommandLineArgs launch, File appDir) {
        System.setProperty("java.awt.headless", "true");
        if (launch == null) {
            throw new IllegalArgumentException("Launch options cannot be null");
        }
        if (appDir == null) {
            throw new IllegalArgumentException("App dir cannot be null");
        }
        PtcgBundledResources resources = prepare(appDir);
        return runCases(launch, resources.getScriptTestsDir().toPath(),
                () -> loadWrapper(new PtcgBundledResources(appDir)), new PtcgScriptTestFixtures());
    }

    private static int runCases(CommandLineArgs launch, Path testsDir,
            ScriptTestWrapperFactory wrapperFactory, ScriptTestFixtures fixtures) {
        if (!launch.isScriptTests()) {
            throw new IllegalArgumentException("Missing " + CommandLineArgs.SCRIPT_TESTS_FLAG);
        }
        if (wrapperFactory == null) {
            throw new IllegalArgumentException("Wrapper factory cannot be null");
        }
        if (fixtures == null) {
            throw new IllegalArgumentException("Fixtures cannot be null");
        }

        List<Path> cases;
        try {
            cases = ScriptTestCaseFiles.selectCases(testsDir, launch.getScriptTestCase());
        } catch (IOException e) {
            System.err.println("Failed to list script tests: " + e.getMessage());
            return 2;
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return 2;
        }

        if (cases.isEmpty()) {
            System.err.println("No lua tests found in " + testsDir.toAbsolutePath());
            return 2;
        }

        Logger.setEnabled(true);
        Logger.setMinLogLevel(launch.getLogLevel());
        ScriptTestRunResult result = ScriptTestBatchRunner.runCaseFiles(cases, wrapperFactory, fixtures);
        printResult(result);
        return result.exitCode();
    }

    private static void printResult(ScriptTestRunResult result) {
        for (ScriptTestFailure failure : result.failures()) {
            System.out.println(failure.displayName() + " FAIL");
            if (failure.message() != null && !failure.message().isBlank()) {
                System.out.println("  " + failure.message());
            }
        }
        System.out.println(result.passed() + " passed, " + result.failed() + " failed");
    }

    private static PtcgBundledResources prepare(File appDir) {
        PtcgBundledResources resources = new PtcgBundledResources(appDir);
        resources.installRandomizerLibrary();
        resources.installAppResources();
        // After modules/rules/randomizer. Wipes script_tests/ so stale cases do not
        // linger.
        resources.installScriptTests();
        return resources;
    }

    private static LuaRandomizerWrapper loadWrapper(PtcgBundledResources resources) {
        LuaRandomizerWrapper wrapper = RandomizerCore.createLuaRandomizer(resources);
        wrapper.requireModulesLoaded();
        return wrapper;
    }
}
