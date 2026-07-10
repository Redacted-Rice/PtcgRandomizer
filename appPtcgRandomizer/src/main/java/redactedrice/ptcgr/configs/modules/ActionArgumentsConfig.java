package redactedrice.ptcgr.configs.modules;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import redactedrice.ptcgr.configs.ParserHelpers;
import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.utils.WarningCollector;

public final class ActionArgumentsConfig {
    static final String SEED_OFFSET_KEY = "seedOffset";
    private final Integer seedOffset;

    static final String ARGUMENTS_KEY = "arguments";
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
        // For right now we only have seeds
        return new ActionArgumentsConfig(seedOffset, Map.of());
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
        if (argumentsValue instanceof Map<?, ?> argumentsMap && !argumentsMap.isEmpty()) {
            warnings.addWarning(
                    entryLabel + ": module arguments are not supported in the UI yet; ignoring.");
        } else if (argumentsValue != null) {
            warnings.addWarning(entryLabel + ": arguments must be a mapping.");
        }
        return new ActionArgumentsConfig(seedOffset, arguments);
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
