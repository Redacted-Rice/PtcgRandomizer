package redactedrice.ptcgr.randomizer.preset;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import redactedrice.ptcgr.randomizer.actions.Action;

public final class ActionConfig {
    private final Integer seedOffset;
    private final Map<String, Object> arguments;

    public ActionConfig(Integer seedOffset, Map<String, Object> arguments) {
        this.seedOffset = seedOffset;
        this.arguments =
                arguments != null ? Collections.unmodifiableMap(new LinkedHashMap<>(arguments))
                        : Map.of();
    }

    public static ActionConfig empty() {
        return new ActionConfig(null, Map.of());
    }

    public static ActionConfig fromAction(Action action) {
        Integer seedOffset = action.getModule().isSeeded() ? action.getSeedOffset() : null;
        return new ActionConfig(seedOffset, Map.of());
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

    Map<String, Object> toDocumentMap() {
        Map<String, Object> node = new LinkedHashMap<>();
        if (seedOffset != null) {
            node.put("seedOffset", seedOffset);
        }
        if (!arguments.isEmpty()) {
            node.put("arguments", new LinkedHashMap<>(arguments));
        }
        return node;
    }

    static ActionConfig fromDocumentMap(Map<String, Object> node, List<String> warnings,
            String entryLabel) {
        Integer seedOffset = null;
        Object seedOffsetValue = node.get("seedOffset");
        if (seedOffsetValue != null) {
            Integer parsed = PresetParsing.parseInteger(seedOffsetValue);
            if (parsed == null) {
                warnings.add(entryLabel + ": seedOffset must be a number.");
            } else {
                seedOffset = parsed;
            }
        }

        Map<String, Object> arguments = Map.of();
        Object argumentsValue = node.get("arguments");
        if (argumentsValue instanceof Map<?, ?> argumentsMap && !argumentsMap.isEmpty()) {
            warnings.add(entryLabel
                    + ": module arguments are not supported in the UI yet; ignoring.");
        } else if (argumentsValue != null) {
            warnings.add(entryLabel + ": arguments must be a mapping.");
        }

        return new ActionConfig(seedOffset, arguments);
    }
}
