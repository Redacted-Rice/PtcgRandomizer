package redactedrice.ptcgr.configs.modules;

import java.util.Map;
import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.ptcgr.utils.WarningCollector;
import redactedrice.randomizer.lua.Module;

public final class ActionConfig extends ModuleConfig {
    static final String CONFIG_KEY = "config";
    private final ActionArgumentsConfig config;

    public ActionConfig(String module, String version, ActionArgumentsConfig config) {
        super(module, version);
        this.config = config != null ? config : ActionArgumentsConfig.empty();
    }

    public static ActionConfig fromAction(Action action) {
        return new ActionConfig(action.getModuleId(), action.getModule().getVersion(),
                ActionArgumentsConfig.fromAction(action));
    }

    public Action toAction(ActionBank actionBank, WarningCollector warnings) {
        Module module = actionBank.getModule(getModule());
        if (module == null) {
            warnings.addWarning("Missing module \"" + getModule() + "\"; it will be skipped.");
            return null;
        }

        checkAndWarnModuleVersion("Module \"" + getModule() + "\"", module, warnings);

        Action action = new Action(module);
        ActionArgumentsConfig config = getConfig();
        if (config.getSeedOffset() != null) {
            if (module.isSeeded()) {
                action.setSeedOffset(config.getSeedOffset());
            } else {
                warnings.addWarning("Module " + getModule()
                        + " does not use seed offsets; ignoring seedOffset.");
            }
        }
        return action;
    }

    public Map<String, Object> convertToYamlMap() {
        Map<String, Object> node = super.convertToYamlMap();
        if (!config.isEmpty()) {
            node.put(CONFIG_KEY, config.convertToYamlMap());
        }
        return node;
    }

    public static ActionConfig readFromLoadedYamlMap(Map<String, Object> node,
            WarningCollector warnings, String entryLabel) {
        String module = parseModule(node.get(MODULE_KEY), warnings, entryLabel);
        if (module == null) {
            warnings.addWarning(entryLabel + ": missing or invalid module name.");
            return null;
        }
        String version = parseVersion(node.get(VERSION_KEY), warnings, entryLabel);

        ActionArgumentsConfig readConfig = ActionArgumentsConfig.empty();
        Object configValue = node.get(CONFIG_KEY);
        if (configValue != null) {
            if (configValue instanceof Map<?, ?> configMap) {
                @SuppressWarnings("unchecked")
                Map<String, Object> castMap = (Map<String, Object>) configMap;
                readConfig =
                        ActionArgumentsConfig.readFromLoadedYamlMap(castMap, warnings, entryLabel);
            } else {
                warnings.addWarning(entryLabel + ": config must be a mapping.");
            }
        }
        return new ActionConfig(module, version, readConfig);
    }

    public ActionArgumentsConfig getConfig() {
        return config;
    }
}
