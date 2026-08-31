package redactedrice.ptcgr.randomizer.gui.moduleconfig.support;

import java.io.File;
import redactedrice.ptcgr.randomizer.RandomizerCore;
import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.ptcgr.resources.PtcgBundledResources;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.dialog.ModuleConfigDialog;
import redactedrice.randomizer.LuaRandomizerWrapper;
import redactedrice.randomizer.lua.Module;
import redactedrice.randomizer.utils.IssueTracker;

public final class ModuleConfigEndToEndSupport {
    private ModuleConfigEndToEndSupport() {}

    public static DevModuleEnvironment loadDevModules(String testName) {
        System.setProperty("ptcgr.devModules", "true");
        File workDir = new File("build/module-config-e2e/" + testName);
        workDir.mkdirs();
        PtcgBundledResources.main(new String[] {workDir.getAbsolutePath()});

        LuaRandomizerWrapper wrapper =
                RandomizerCore.createLuaRandomizer(new PtcgBundledResources(workDir));
        if (IssueTracker.hasErrors()) {
            throw new IllegalStateException(
                    "Module requirement validation failed: " + IssueTracker.getErrors());
        }

        return new DevModuleEnvironment(new ActionBank(wrapper));
    }

    public static void clearDevModulesProperty() {
        System.clearProperty("ptcgr.devModules");
    }

    public static Action newAction(ActionBank actionBank, String moduleId) {
        Module module = actionBank.getModule(moduleId);
        if (module == null) {
            throw new IllegalArgumentException("Missing module: " + moduleId);
        }
        return new Action(module, actionBank.getEnumRegistry());
    }

    public static ModuleConfigDialog openEditableDialog(ActionBank actionBank, Action action) {
        return new ModuleConfigDialog(null, action, true, actionBank);
    }
}
