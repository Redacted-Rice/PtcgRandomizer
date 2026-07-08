package redactedrice.ptcgr.randomizer.preset;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.randomizer.lua.Module;

public final class ActionPreset extends ModulePreset {
    static final String CONFIG_KEY = "config";

    private final ModuleConfigPreset config;

    public ActionPreset(String module, String version, ModuleConfigPreset config) {
        super(module, version);
        this.config = config != null ? config : ModuleConfigPreset.empty();
    }

    public static ActionPreset fromAction(Action action) {
        return new ActionPreset(action.getName(), action.getModule().getVersion(),
                ModuleConfigPreset.fromAction(action));
    }

    public Action toAction(ActionBank actionBank, List<String> warnings) {
        Module module = actionBank.getModule(getModule());
        if (module == null) {
            warnings.add("Missing module \"" + getModule() + "\"; it will be skipped.");
            return null;
        }

        checkAndWarnModuleVersion("Module \"" + getModule() + "\"", module, warnings);

        Action action = new Action(module);
        ModuleConfigPreset config = getConfig();
        if (config.getSeedOffset() != null) {
            if (module.isSeeded()) {
                action.setSeedOffset(config.getSeedOffset());
            } else {
                warnings.add("Module " + getModule()
                        + " does not use seed offsets; ignoring seedOffset.");
            }
        }
        return action;
    }

    public ModuleConfigPreset getConfig() {
        return config;
    }

    public Map<String, Object> prepForSave() {
        Map<String, Object> node = new LinkedHashMap<>(prepModuleForSave());
        if (!config.isEmpty()) {
            node.put(CONFIG_KEY, config.prepForSave());
        }
        return node;
    }

    public static ActionPreset readFromSave(Map<String, Object> node, List<String> warnings,
            String entryLabel) {
        String module = parseModule(node.get(MODULE_KEY), warnings, entryLabel);
        if (module == null) {
            return null;
        }
        String version = parseVersion(node.get(VERSION_KEY), warnings, entryLabel);

        ModuleConfigPreset readConfig = ModuleConfigPreset.empty();
        Object configValue = node.get(CONFIG_KEY);
        if (configValue != null) {
            if (configValue instanceof Map<?, ?> configMap) {
                // Need to cast separately for supress warning to work
                @SuppressWarnings("unchecked")
                Map<String, Object> castMap = (Map<String, Object>) configMap;
                readConfig = ModuleConfigPreset.readFromSave(castMap, warnings, entryLabel);
                return new ActionPreset(module, version, readConfig);
            } else {
                warnings.add(entryLabel + ": config must be a mapping.");
            }
        }
        return new ActionPreset(module, version, readConfig);
    }
}
