package redactedrice.ptcgr.randomizer.preset;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.randomizer.lua.Module;

public final class ScriptPreset extends ModulePreset {
    public ScriptPreset(String module, String version) {
        super(module, version);
    }

    public static ScriptPreset fromModule(Module module) {
        return new ScriptPreset(module.getName(), module.getVersion());
    }

    public Map<String, Object> prepForSave() {
        return prepModuleForSave();
    }

    public static ScriptPreset readFromSave(Map<String, Object> node, List<String> warnings,
            String entryLabel) {
        String module = parseModule(node.get(MODULE_KEY), warnings, entryLabel);
        if (module == null) {
            return null;
        }
        String version = parseVersion(node.get(VERSION_KEY), warnings, entryLabel);
        return new ScriptPreset(module, version);
    }

    public static void checkAndWarnDifferences(String sectionLabel, List<ScriptPreset> saved,
            List<Module> currentModules, ActionBank actionBank, List<String> warnings) {
        Set<String> savedNames = new HashSet<>();
        for (ScriptPreset preset : saved) {
            savedNames.add(preset.getModule());
            String entryLabel = sectionLabel + " \"" + preset.getModule() + "\"";
            Module module = actionBank.getModule(preset.getModule());
            if (module == null) {
                warnings.add("Preset references " + entryLabel + " that is not loaded.");
                continue;
            }
            preset.checkAndWarnModuleVersion(entryLabel, module, warnings);
        }

        for (Module module : currentModules) {
            if (!savedNames.contains(module.getName())) {
                warnings.add("App has " + sectionLabel + " \"" + module.getName()
                        + "\" that was not in the preset.");
            }
        }
    }
}
