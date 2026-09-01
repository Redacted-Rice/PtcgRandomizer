package redactedrice.ptcgr.randomizer.scripttests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import redactedrice.ptcgr.configs.AppPreferences;
import redactedrice.ptcgr.randomizer.RandomizerCore;
import redactedrice.ptcgr.resources.PtcgBundledResources;
import redactedrice.randomizer.LuaRandomizerWrapper;
import redactedrice.randomizer.scripttests.ScriptTestCli;

// PTCG host for URJ script tests. Installs this app's modules then hands off to ScriptTestCli.
public final class ScriptTestRunner {
    private ScriptTestRunner() {}

    public static boolean handles(String[] args) {
        return ScriptTestCli.handles(args);
    }

    public static int run(String[] args) {
        return run(args, AppPreferences.resolveAppDirectory());
    }

    public static int run(String[] args, File appDir) {
        if (appDir == null) {
            throw new IllegalArgumentException("App dir cannot be null");
        }
        PtcgBundledResources resources = prepare(appDir);
        return ScriptTestCli.run(args, resources.getScriptTestsDir().toPath(), loadWrapper(resources),
                new PtcgScriptTestFixtures());
    }

    // One-time setup for JUnit. Installs app resources and bundled script tests.
    public static LuaRandomizerWrapper prepareForScriptTests(File appDir) {
        if (appDir == null) {
            throw new IllegalArgumentException("App dir cannot be null");
        }
        return loadWrapper(prepare(appDir));
    }

    // Run a single bundled case file against an app dir that prepareForScriptTests already set up.
    public static int runCase(String caseFileName, File appDir, LuaRandomizerWrapper wrapper) {
        if (appDir == null) {
            throw new IllegalArgumentException("App dir cannot be null");
        }
        if (wrapper == null) {
            throw new IllegalArgumentException("Wrapper cannot be null");
        }
        Path testsDir = new PtcgBundledResources(appDir).getScriptTestsDir().toPath();
        return ScriptTestCli.run(new String[] { ScriptTestCli.FLAG, caseFileName }, testsDir, wrapper,
                new PtcgScriptTestFixtures());
    }

    public static List<String> bundledCaseFileNames() {
        String resourcePath = PtcgBundledResources.SCRIPT_TESTS_RESOURCE + "/.manifest";
        try (InputStream in =
                PtcgBundledResources.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("Missing " + resourcePath + " on classpath");
            }
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return text.lines().map(String::trim).filter(line -> !line.isEmpty())
                    .filter(line -> !line.contains("/") && !line.contains("\\"))
                    .filter(ScriptTestCli::isLuaCaseFileName).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + resourcePath, e);
        }
    }

    private static PtcgBundledResources prepare(File appDir) {
        PtcgBundledResources resources = new PtcgBundledResources(appDir);
        resources.installRandomizerLibrary();
        resources.installAppResources();
        resources.installScriptTests();
        return resources;
    }

    private static LuaRandomizerWrapper loadWrapper(PtcgBundledResources resources) {
        LuaRandomizerWrapper wrapper = RandomizerCore.createLuaRandomizer(resources);
        wrapper.requireModulesLoaded();
        return wrapper;
    }
}
