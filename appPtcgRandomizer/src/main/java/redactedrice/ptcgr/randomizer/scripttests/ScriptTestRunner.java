package redactedrice.ptcgr.randomizer.scripttests;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import redactedrice.ptcgr.configs.AppPreferences;
import redactedrice.ptcgr.randomizer.RandomizerCore;
import redactedrice.ptcgr.resources.PtcgBundledResources;
import redactedrice.randomizer.LuaRandomizerWrapper;
import redactedrice.randomizer.scripttests.ScriptTestCli;
import redactedrice.randomizer.utils.ManifestResourceExtractor;

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
        PtcgBundledResources resources = new PtcgBundledResources(appDir);
        resources.installAll();
        installBundledScriptTests(resources);
        return ScriptTestCli.run(args, resources.getScriptTestsDir().toPath(),
                loadWrapper(resources), new PtcgScriptTestFixtures());
    }

    private static LuaRandomizerWrapper loadWrapper(PtcgBundledResources resources) {
        LuaRandomizerWrapper wrapper = RandomizerCore.createLuaRandomizer(resources);
        wrapper.requireModulesLoaded();
        return wrapper;
    }

    private static void installBundledScriptTests(PtcgBundledResources resources) {
        try {
            ManifestResourceExtractor.extract(PtcgBundledResources.SCRIPT_TESTS_RESOURCE,
                    resources.getScriptTestsDir().getAbsolutePath(), true);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to install script tests", e);
        }
    }
}
