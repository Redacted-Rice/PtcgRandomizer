package redactedrice.ptcgr.randomizer.preset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import redactedrice.ptcgr.randomizer.actions.Action;

/**
 * In memory representation of a saved randomizer settings/configs. Used for both saving and
 * loading.
 */
public final class RandomizerPreset {
    public static final int CURRENT_VERSION = 1;

    private final int version;
    private final String seed;
    private final List<ActionPreset> actions;

    public RandomizerPreset(String seed, List<ActionPreset> actions) {
        this(CURRENT_VERSION, seed, actions);
    }

    public RandomizerPreset(int version, String seed, List<ActionPreset> actions) {
        this.version = version;
        this.seed = Objects.requireNonNull(seed, "seed");
        this.actions = List.copyOf(actions);
    }

    public static RandomizerPreset fromActions(String seed, List<Action> actions) {
        List<ActionPreset> presets = new ArrayList<>();
        for (Action action : actions) {
            presets.add(ActionPreset.fromAction(action));
        }
        return new RandomizerPreset(seed, presets);
    }

    public int getVersion() {
        return version;
    }

    public String getSeed() {
        return seed;
    }

    public List<ActionPreset> getActions() {
        return actions;
    }

    Map<String, Object> toDocumentMap() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("version", version);
        root.put("seed", seed);

        List<Map<String, Object>> actionNodes = new ArrayList<>();
        for (ActionPreset action : actions) {
            actionNodes.add(action.toDocumentMap());
        }
        root.put("actions", actionNodes);
        return root;
    }

    public static final class ActionPreset {
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
    }

    public static final class ActionConfig {
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
    }
}
