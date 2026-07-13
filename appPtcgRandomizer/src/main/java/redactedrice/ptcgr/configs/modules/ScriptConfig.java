package redactedrice.ptcgr.configs.modules;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.ptcgr.utils.WarningCollector;
import redactedrice.randomizer.lua.Module;

public final class ScriptConfig extends ModuleConfig {
    public ScriptConfig(String module, String version) {
        super(module, version);
    }

    public static ScriptConfig fromModule(Module module) {
        return new ScriptConfig(module.getId(), module.getVersion());
    }

    public static ScriptConfig readFromLoadedYamlMap(Map<String, Object> node,
            WarningCollector warnings, String entryLabel) {
        String module = parseModule(node.get(MODULE_KEY), warnings, entryLabel);
        if (module == null) {
            warnings.addWarning(entryLabel + ": missing or invalid module name.");
            return null;
        }
        String version = parseVersion(node.get(VERSION_KEY), warnings, entryLabel);
        return new ScriptConfig(module, version);
    }

    public static void checkAndWarnDifferences(String sectionLabel, List<ScriptConfig> saved,
            List<Module> currentModules, ActionBank actionBank, WarningCollector warnings) {
        Set<String> savedNames = new HashSet<>();
        for (ScriptConfig config : saved) {
            savedNames.add(config.getModule());
            String entryLabel = sectionLabel + " \"" + config.getModule() + "\"";
            Module module = actionBank.getScript(config.getModule());
            if (module == null) {
                warnings.addWarning("Config references " + entryLabel + " that is not loaded.");
                continue;
            }
            config.checkAndWarnModuleVersion(entryLabel, module, warnings);
        }

        for (Module module : currentModules) {
            if (!savedNames.contains(module.getId())) {
                warnings.addWarning("App has " + sectionLabel + " \"" + module.getId()
                        + "\" that was not in the config.");
            }
        }
    }
}
