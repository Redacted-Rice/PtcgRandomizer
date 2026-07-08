package redactedrice.ptcgr.randomizer.preset;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import redactedrice.ptcgr.randomizer.actions.Action;

public final class ModuleConfigPreset {
    static final String SEED_OFFSET_KEY = "seedOffset";
    static final String ARGUMENTS_KEY = "arguments";

    private final Integer seedOffset;
    private final Map<String, Object> arguments;

    public ModuleConfigPreset(Integer seedOffset, Map<String, Object> arguments) {
        this.seedOffset = seedOffset;
        this.arguments =
                arguments != null ? Collections.unmodifiableMap(new LinkedHashMap<>(arguments))
                        : Map.of();
    }

    public static ModuleConfigPreset empty() {
        return new ModuleConfigPreset(null, Map.of());
    }

    public static ModuleConfigPreset fromAction(Action action) {
        Integer seedOffset = action.getModule().isSeeded() ? action.getSeedOffset() : null;
        return new ModuleConfigPreset(seedOffset, Map.of());
    }

    public Map<String, Object> prepForSave() {
        Map<String, Object> node = new LinkedHashMap<>();
        if (seedOffset != null) {
            node.put(SEED_OFFSET_KEY, seedOffset);
        }
        if (!arguments.isEmpty()) {
            node.put(ARGUMENTS_KEY, new LinkedHashMap<>(arguments));
        }
        return node;
    }

    public static ModuleConfigPreset readFromSave(Map<String, Object> node, List<String> warnings,
            String entryLabel) {
        Integer seedOffset = null;
        Object seedOffsetValue = node.get(SEED_OFFSET_KEY);
        if (seedOffsetValue != null) {
            Integer parsed = ValueParser.parseInteger(seedOffsetValue);
            if (parsed == null) {
                warnings.add(entryLabel + ": seedOffset must be a number.");
            } else {
                seedOffset = parsed;
            }
        }

        Map<String, Object> arguments = Map.of();
        Object argumentsValue = node.get(ARGUMENTS_KEY);
        if (argumentsValue instanceof Map<?, ?> argumentsMap && !argumentsMap.isEmpty()) {
            warnings.add(
                    entryLabel + ": module arguments are not supported in the UI yet; ignoring.");
        } else if (argumentsValue != null) {
            warnings.add(entryLabel + ": arguments must be a mapping.");
        }

        return new ModuleConfigPreset(seedOffset, arguments);
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
