package redactedrice.ptcgr.configs.modules;

import java.util.Map;

import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.ptcgr.utils.WarningCollector;
import redactedrice.randomizer.lua.Module;

public final class ActionConfig extends ModuleConfig {
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

        String entryLabel = moduleLabel(module);
        checkAndWarnModuleVersion(entryLabel, module, warnings);

        Action action = new Action(module, actionBank.getEnumRegistry());
        ActionArgumentsConfig config = getConfig();
        if (config.getSeedOffset() != null) {
            if (module.isSeeded()) {
                action.setSeedOffset(config.getSeedOffset());
            } else {
                warnings.addWarning(
                        entryLabel + " does not use seed offsets; ignoring seedOffset.");
            }
        }
        config.applyToAction(action, module, warnings, entryLabel);
        return action;
    }

    public Map<String, Object> convertToYamlMap() {
        Map<String, Object> node = super.convertToYamlMap();
        node.putAll(config.convertToYamlMap());
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

        ActionArgumentsConfig readConfig =
                ActionArgumentsConfig.readFromLoadedYamlMap(node, warnings, entryLabel);
        return new ActionConfig(module, version, readConfig);
    }

    public ActionArgumentsConfig getConfig() {
        return config;
    }

    // Builds a warning label using the module's display name with its id in parenthesis,
    // e.g. Module "Set X Moves Per Card" (set_num_moves)
    private static String moduleLabel(Module module) {
        String name = module.getName();
        if (name == null || name.isBlank()) {
            return "Module \"" + module.getId() + "\"";
        }
        return "Module \"" + name + "\" (" + module.getId() + ")";
    }
}
