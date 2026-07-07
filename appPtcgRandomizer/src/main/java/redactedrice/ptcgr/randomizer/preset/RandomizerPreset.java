package redactedrice.ptcgr.randomizer.preset;

import java.util.ArrayList;
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

    public static RandomizerPreset fromDocumentMap(Map<String, Object> root, List<String> warnings)
            throws PresetLoadException {
        return PresetParsing.parseDocument(root, warnings);
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
}
