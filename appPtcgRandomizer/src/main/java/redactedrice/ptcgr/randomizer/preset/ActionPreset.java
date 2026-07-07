package redactedrice.ptcgr.randomizer.preset;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.randomizer.lua.Module;

public final class ActionPreset {
    static final String MODULE_KEY = "module";
    static final String VERSION_KEY = "version";
    static final String CONFIG_KEY = "config";
    static final String UNKNOWN_VERSION = "<unknown>";

    private final String module;
    private final String version;
    private final ModuleConfigPreset config;

    public ActionPreset(String module, String version, ModuleConfigPreset config) {
        this.module = Objects.requireNonNull(module, "module");
        this.version = version;
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

        checkAndWarnModuleVersion(module, warnings);

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

    public String getModule() {
        return module;
    }

    public String getVersion() {
        return version;
    }

    public ModuleConfigPreset getConfig() {
        return config;
    }

    Map<String, Object> prepForSave() {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put(MODULE_KEY, module);
        node.put(VERSION_KEY, version != null ? version : UNKNOWN_VERSION);
        if (!config.isEmpty()) {
            node.put(CONFIG_KEY, config.prepForSave());
        }
        return node;
    }

    static ActionPreset readFromSave(Map<String, Object> node, List<String> warnings,
            String entryLabel) {
        Object moduleValue = node.get(MODULE_KEY);
        if (!(moduleValue instanceof String module) || module.isBlank()) {
            warnings.add(entryLabel + ": missing or invalid module name.");
            return null;
        }

        String version = parseVersion(node.get(VERSION_KEY), warnings, entryLabel);

        ModuleConfigPreset config = ModuleConfigPreset.empty();
        Object configValue = node.get(CONFIG_KEY);
        if (configValue != null) {
            if (configValue instanceof Map<?, ?> configMap) {
                @SuppressWarnings("unchecked")
                ModuleConfigPreset parsed = ModuleConfigPreset
                        .readFromSave((Map<String, Object>) configMap, warnings, entryLabel);
                config = parsed;
            } else {
                warnings.add(entryLabel + ": config must be a mapping.");
            }
        }

        return new ActionPreset(module, version, config);
    }

    private static String parseVersion(Object value, List<String> warnings, String entryLabel) {
        if (value == null) {
            return null;
        }
        if (value instanceof String versionText && !versionText.isBlank()) {
            return versionText;
        }
        if (value instanceof Number number) {
            return String.valueOf(number);
        }
        warnings.add(entryLabel + ": version must be a string or number.");
        return null;
    }

    private void checkAndWarnModuleVersion(Module module, List<String> warnings) {
        String savedVersion = getVersion();
        if (savedVersion == null || savedVersion.isBlank()) {
            warnings.add("Module \"" + getModule() + "\" does not record a version.");
            return;
        }
        if (UNKNOWN_VERSION.equals(savedVersion)) {
            warnings.add("Module \"" + getModule() + "\" was saved with an unknown version.");
            return;
        }

        String currentVersion = module.getVersion();
        if (!savedVersion.equals(currentVersion)) {
            warnings.add("Module \"" + getModule() + "\" was saved as version " + savedVersion
                    + "; current version is " + currentVersion + ".");
        }
    }
}
