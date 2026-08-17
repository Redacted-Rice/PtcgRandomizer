package redactedrice.ptcgr.randomizer.scripttests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import redactedrice.ptcgr.configs.AppPreferences;
import redactedrice.ptcgr.constants.PtcgRandomizerVersion;
import redactedrice.ptcgr.randomizer.RandomizerCore;
import redactedrice.ptcgr.resources.PtcgBundledResources;
import redactedrice.randomizer.LuaRandomizerWrapper;
import redactedrice.randomizer.lua.requirements.CoreRequirements;
import redactedrice.randomizer.scripttests.ScriptTestCli;
import redactedrice.randomizer.utils.IssueTracker;

// PTCG host for URJ script tests. Installs this app's modules then hands off to ScriptTestCli.
public final class ScriptTestRunner {
    private ScriptTestRunner() {}

    public static boolean handles(String[] args) {
        return ScriptTestCli.handles(args);
    }

    public static int run(String[] args) {
        File appDir = AppPreferences.resolveAppDirectory();
        PtcgBundledResources resources = new PtcgBundledResources(appDir);
        resources.installAll();
        resources.installScriptTests();
        return ScriptTestCli.run(args, resources.getScriptTestsDir().toPath(), loadWrapper(resources),
                new PtcgScriptTestFixtures());
    }

    private static LuaRandomizerWrapper loadWrapper(PtcgBundledResources resources) {
        File randomizerDir = resources.getRandomizerDir();
        File modulesDir = resources.getModulesDir();
        if (!modulesDir.isDirectory()) {
            throw new IllegalStateException(
                    "Action modules dir is missing: " + modulesDir.getAbsolutePath());
        }

        List<String> allowedDirectories = new ArrayList<>();
        allowedDirectories.add(randomizerDir.getAbsolutePath());
        allowedDirectories.add(modulesDir.getAbsolutePath());

        CoreRequirements requirements = new CoreRequirements();
        requirements.addCoreRequirement(PtcgRandomizerVersion.PLATFORM_KEY,
                PtcgRandomizerVersion.VERSION, true);

        LuaRandomizerWrapper wrapper = new LuaRandomizerWrapper(allowedDirectories,
                List.of(modulesDir.getAbsolutePath()), null, requirements);
        RandomizerCore.registerSharedEnums(wrapper.getSharedContext());

        IssueTracker.clear();
        int loaded = wrapper.loadModules();
        if (loaded <= 0) {
            throw new IllegalStateException(
                    "No action modules loaded from " + modulesDir.getAbsolutePath());
        }
        if (IssueTracker.hasErrors()) {
            throw new IllegalStateException("Module load failed: " + IssueTracker.getErrors());
        }
        return wrapper;
    }
}
