package redactedrice.ptcgr.configs.modules;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.randomizer.utils.IssueTracker;
import redactedrice.randomizer.lua.Module;

public final class ScriptConfig extends ModuleConfig {
    public ScriptConfig(String module, String version) {
        super(module, version);
    }

    public static ScriptConfig fromModule(Module module) {
        return new ScriptConfig(module.getId(), module.getVersion());
    }

    public static ScriptConfig readFromLoadedYamlMap(Map<String, Object> node, String entryLabel) {
        String module = parseModule(node.get(MODULE_KEY), entryLabel);
        if (module == null) {
            IssueTracker.addWarning(entryLabel + ": missing or invalid module name.");
            return null;
        }
        String version = parseVersion(node.get(VERSION_KEY), entryLabel);
        return new ScriptConfig(module, version);
    }

    public static void checkRequired(String sectionLabel, List<ScriptConfig> saved,
            ActionBank actionBank) {
        for (ScriptConfig config : saved) {
            String entryLabel = sectionLabel + " \"" + config.getModule() + "\"";
            Module module = actionBank.getScript(config.getModule());
            if (module == null) {
                IssueTracker.addWarning("Config references " + entryLabel + " that is not loaded.");
                continue;
            }
            config.checkAndWarnModuleVersion(entryLabel, module);
        }
    }

    public static void checkAndWarnDifferences(String sectionLabel, List<ScriptConfig> saved,
            List<Module> currentModules, ActionBank actionBank) {
        Set<String> savedNames = new HashSet<>();
        for (ScriptConfig config : saved) {
            savedNames.add(config.getModule());
        }
        checkRequired(sectionLabel, saved, actionBank);

        for (Module module : currentModules) {
            if (!savedNames.contains(module.getId())) {
                IssueTracker.addWarning("App has " + sectionLabel + " \"" + module.getId()
                        + "\" that was not in the config.");
            }
        }
    }
}
