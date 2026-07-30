package redactedrice.ptcgr.configs.modules;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import redactedrice.ptcgr.configs.ParserHelpers;
import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.utils.WarningCollector;
import redactedrice.randomizer.lua.Module;
import redactedrice.randomizer.lua.arguments.ArgumentDefinition;

public final class ActionArgumentsConfig {
    private static final String SEED_OFFSET_KEY = "seedOffset";
    private final Integer seedOffset;

    private static final String ARGUMENTS_KEY = "arguments";
    private final Map<String, Object> arguments;

    public ActionArgumentsConfig(Integer seedOffset, Map<String, Object> arguments) {
        this.seedOffset = seedOffset;
        this.arguments =
                arguments != null ? Collections.unmodifiableMap(new LinkedHashMap<>(arguments))
                        : Map.of();
    }

    public static ActionArgumentsConfig empty() {
        return new ActionArgumentsConfig(null, Map.of());
    }

    public static ActionArgumentsConfig fromAction(Action action) {
        Integer seedOffset = action.getModule().isSeeded() ? action.getSeedOffset() : null;
        return new ActionArgumentsConfig(seedOffset, action.getArguments());
    }

    public Map<String, Object> convertToYamlMap() {
        Map<String, Object> node = new LinkedHashMap<>();
        if (seedOffset != null) {
            node.put(SEED_OFFSET_KEY, seedOffset);
        }
        if (!arguments.isEmpty()) {
            node.put(ARGUMENTS_KEY, new LinkedHashMap<>(arguments));
        }
        return node;
    }

    public static ActionArgumentsConfig readFromLoadedYamlMap(Map<String, Object> node,
            WarningCollector warnings, String entryLabel) {
        Integer seedOffset = null;
        Object seedOffsetValue = node.get(SEED_OFFSET_KEY);
        if (seedOffsetValue != null) {
            Integer parsed = ParserHelpers.parseInteger(seedOffsetValue);
            if (parsed == null) {
                warnings.addWarning(entryLabel + ": seedOffset must be a number.");
            } else {
                seedOffset = parsed;
            }
        }

        Map<String, Object> arguments = Map.of();
        Object argumentsValue = node.get(ARGUMENTS_KEY);
        if (argumentsValue instanceof Map<?, ?> argumentsMap) {
            if (!argumentsMap.isEmpty()) {
                Map<String, Object> parsed = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : argumentsMap.entrySet()) {
                    if (entry.getKey() == null) {
                        warnings.addWarning(entryLabel + ": arguments keys must be strings.");
                        continue;
                    }
                    parsed.put(entry.getKey().toString(), entry.getValue());
                }
                arguments = parsed;
            }
        } else if (argumentsValue != null) {
            warnings.addWarning(entryLabel + ": arguments must be a mapping.");
        }
        return new ActionArgumentsConfig(seedOffset, arguments);
    }

    public void applyToAction(Action action, Module module, WarningCollector warnings,
            String entryLabel) {
        for (Map.Entry<String, Object> entry : arguments.entrySet()) {
            String name = entry.getKey();
            if (!action.hasArgument(name)) {
                warnings.addWarning(entryLabel + ": unknown argument \"" + name + "\" (value: "
                        + entry.getValue() + "); ignoring.");
                continue;
            }

            ArgumentDefinition argDef = findArgument(module, name);
            try {
                action.setArgument(name, entry.getValue());
            } catch (IllegalArgumentException ex) {
                warnings.addWarning(entryLabel + ": argument \"" + name + "\" has invalid value ("
                        + entry.getValue() + "); using default value ("
                        + (argDef != null ? argDef.getDefaultValue() : "unknown") + "). "
                        + (ex.getMessage() != null ? ex.getMessage() : ""));
            }
        }

        for (ArgumentDefinition argDef : module.getArguments()) {
            String name = argDef.getName();
            if (!arguments.containsKey(name)) {
                warnings.addWarning(entryLabel + ": argument \"" + name
                        + "\" not specified; using default value (" + argDef.getDefaultValue()
                        + ").");
            }
        }
    }

    private static ArgumentDefinition findArgument(Module module, String name) {
        for (ArgumentDefinition argDef : module.getArguments()) {
            if (argDef.getName().equals(name)) {
                return argDef;
            }
        }
        return null;
    }

    public Integer getSeedOffset() {
        return seedOffset;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public boolean isEmpty() {
        return seedOffset == null && arguments.isEmpty();
    }
}
