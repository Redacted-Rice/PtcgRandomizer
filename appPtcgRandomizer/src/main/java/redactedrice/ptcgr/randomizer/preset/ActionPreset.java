package redactedrice.ptcgr.randomizer.preset;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import redactedrice.ptcgr.randomizer.actions.Action;

public final class ActionPreset {
    private final String module;
    private final ActionConfig config;

    public ActionPreset(String module, ActionConfig config) {
        this.module = Objects.requireNonNull(module, "module");
        this.config = config != null ? config : ActionConfig.empty();
    }

    public static ActionPreset fromAction(Action action) {
        return new ActionPreset(action.getName(), ActionConfig.fromAction(action));
    }

    public String getModule() {
        return module;
    }

    public ActionConfig getConfig() {
        return config;
    }

    Map<String, Object> toDocumentMap() {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("module", module);
        if (!config.isEmpty()) {
            node.put("config", config.toDocumentMap());
        }
        return node;
    }

    static ActionPreset fromDocumentMap(Map<String, Object> node, List<String> warnings,
            String entryLabel) {
        Object moduleValue = node.get("module");
        if (!(moduleValue instanceof String module) || module.isBlank()) {
            warnings.add(entryLabel + ": missing or invalid module name.");
            return null;
        }

        ActionConfig config = ActionConfig.empty();
        Object configValue = node.get("config");
        if (configValue != null) {
            if (configValue instanceof Map<?, ?> configMap) {
                @SuppressWarnings("unchecked")
                ActionConfig parsed = ActionConfig.fromDocumentMap((Map<String, Object>) configMap,
                        warnings, entryLabel);
                config = parsed;
            } else {
                warnings.add(entryLabel + ": config must be a mapping.");
            }
        }

        return new ActionPreset(module, config);
    }
}
