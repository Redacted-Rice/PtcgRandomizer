package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import redactedrice.ptcgr.constants.CardDataConstants.CardType;
import redactedrice.ptcgr.constants.PtcgRandomizerVersion;
import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.ptcgr.resources.PtcgBundledResources;
import redactedrice.randomizer.LuaRandomizerWrapper;
import redactedrice.randomizer.lua.Module;
import redactedrice.randomizer.lua.requirements.CoreRequirements;
import redactedrice.randomizer.utils.ErrorTracker;
import redactedrice.randomizer.utils.RandomizerBundledResources;

final class ModuleConfigEndToEndSupport {
    record DevModuleEnvironment(ActionBank actionBank) {
    }

    private ModuleConfigEndToEndSupport() {
    }

    static DevModuleEnvironment loadDevModules(String testName) {
        System.setProperty("ptcgr.devModules", "true");
        File workDir = new File("build/module-config-e2e/" + testName);
        workDir.mkdirs();
        PtcgBundledResources.main(new String[] {workDir.getAbsolutePath()});

        File modulesDir = new File(workDir, PtcgBundledResources.MODULES_DIR_NAME);
        File randomizerDir = RandomizerBundledResources.getInstalledDir(workDir);
        List<String> allowedDirectories = new ArrayList<>();
        allowedDirectories.add(randomizerDir.getAbsolutePath());
        allowedDirectories.add(modulesDir.getAbsolutePath());
        List<String> searchPaths = List.of(modulesDir.getAbsolutePath());

        CoreRequirements requirements = new CoreRequirements();
        requirements.addCoreRequirement(PtcgRandomizerVersion.PLATFORM_KEY,
                PtcgRandomizerVersion.VERSION, true);

        LuaRandomizerWrapper wrapper = new LuaRandomizerWrapper(allowedDirectories, searchPaths,
                null, null, requirements);
        wrapper.getSharedContext().registerEnum(CardType.class);

        ErrorTracker.clearErrors();
        wrapper.loadModules();
        if (ErrorTracker.hasErrors()) {
            throw new IllegalStateException(
                    "Module requirement validation failed: " + ErrorTracker.getErrors());
        }

        return new DevModuleEnvironment(new ActionBank(wrapper));
    }

    static void clearDevModulesProperty() {
        System.clearProperty("ptcgr.devModules");
    }

    static Action newAction(ActionBank actionBank, String moduleId) {
        Module module = actionBank.getModule(moduleId);
        if (module == null) {
            throw new IllegalArgumentException("Missing module: " + moduleId);
        }
        return new Action(module, actionBank.getEnumRegistry());
    }

    static ModuleConfigDialog openEditableDialog(ActionBank actionBank, Action action) {
        return new ModuleConfigDialog(null, action, true, actionBank::getEnumValues);
    }
}
