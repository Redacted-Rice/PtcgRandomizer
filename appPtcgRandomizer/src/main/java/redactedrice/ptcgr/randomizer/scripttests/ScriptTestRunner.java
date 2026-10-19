package redactedrice.ptcgr.randomizer.scripttests;

import java.io.File;

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
        return ScriptTestCli.run(args, resources.getScriptTestsDir().toPath(),
                () -> loadWrapper(new PtcgBundledResources(appDir)), new PtcgScriptTestFixtures());
    }

    private static PtcgBundledResources prepare(File appDir) {
        PtcgBundledResources resources = new PtcgBundledResources(appDir);
        resources.installRandomizerLibrary();
        resources.installAppResources();
        // After modules/rules/randomizer. Wipes script_tests/ so stale cases do not linger.
        resources.installScriptTests();
        return resources;
    }

    private static LuaRandomizerWrapper loadWrapper(PtcgBundledResources resources) {
        LuaRandomizerWrapper wrapper = RandomizerCore.createLuaRandomizer(resources);
        wrapper.requireModulesLoaded();
        return wrapper;
    }
}
