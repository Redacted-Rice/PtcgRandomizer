package redactedrice.ptcgr.randomizer.preset;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import redactedrice.ptcgr.constants.PtcgRandomizerVersion;
import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;

/**
 * In memory representation of a saved randomizer settings/configs. Used for both saving and
 * loading.
 */
public final class Preset {
    static final String FORMAT_VERSION_KEY = "version";
    static final String APP_VERSION_KEY = "appVersion";
    static final String SEED_KEY = "seed";
    static final String ACTIONS_KEY = "actions";

    public static final int CURRENT_FORMAT_VERSION = 1;

    private final int formatVersion;
    private final String appVersion;
    private final String seed;
    private final List<ActionPreset> actionPresets;

    public Preset(String seed, List<ActionPreset> actionPresets) {
        this(CURRENT_FORMAT_VERSION, PtcgRandomizerVersion.VERSION, seed, actionPresets);
    }

    public Preset(int formatVersion, String appVersion, String seed,
            List<ActionPreset> actionPresets) {
        this.formatVersion = formatVersion;
        this.appVersion = appVersion;
        this.seed = Objects.requireNonNull(seed, "seed");
        this.actionPresets = List.copyOf(actionPresets);
    }

    private static List<ActionPreset> convertActions(List<Action> actions) {
        List<ActionPreset> presets = new ArrayList<>();
        for (Action action : actions) {
            presets.add(ActionPreset.fromAction(action));
        }
        return presets;
    }

    public static Preset fromAppState(String seed, List<Action> actions) {
        return new Preset(CURRENT_FORMAT_VERSION, PtcgRandomizerVersion.VERSION, seed,
                convertActions(actions));
    }

    public static Preset readFromSave(Map<String, Object> root, List<String> warnings)
            throws PresetException {
        if (root == null) {
            throw new PresetException("Preset file is empty.");
        }

        int formatVersion = parseFormatVersion(root.get(FORMAT_VERSION_KEY), warnings);
        String appVersion = parseAppVersion(root.get(APP_VERSION_KEY), warnings);
        String seed = parseSeed(root.get(SEED_KEY));
        List<ActionPreset> actions = parseActions(root.get(ACTIONS_KEY), warnings);
        warnAppVersionMismatch(appVersion, warnings);
        return new Preset(formatVersion, appVersion, seed, actions);
    }

    Map<String, Object> prepForSave() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put(FORMAT_VERSION_KEY, formatVersion);
        if (appVersion != null) {
            root.put(APP_VERSION_KEY, appVersion);
        }
        root.put(SEED_KEY, seed);

        List<Map<String, Object>> actionNodes = new ArrayList<>();
        for (ActionPreset actionPreset : actionPresets) {
            actionNodes.add(actionPreset.prepForSave());
        }
        root.put(ACTIONS_KEY, actionNodes);
        return root;
    }

    public int getFormatVersion() {
        return formatVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getSeed() {
        return seed;
    }

    public List<ActionPreset> getActionPresets() {
        return actionPresets;
    }

    public List<Action> getActions(ActionBank actionBank, List<String> warnings) {
        List<Action> actions = new ArrayList<>();
        for (ActionPreset actionPreset : getActionPresets()) {
            Action action = actionPreset.toAction(actionBank, warnings);
            if (action != null) {
                actions.add(action);
            }
        }
        return actions;
    }

    private static int parseFormatVersion(Object value, List<String> warnings) {
        Integer version = ValueParser.parseInteger(value);
        if (version == null) {
            warnings.add(
                    "Missing or invalid version; assuming version " + CURRENT_FORMAT_VERSION + ".");
            return CURRENT_FORMAT_VERSION;
        }
        if (version > CURRENT_FORMAT_VERSION) {
            warnings.add("Preset version " + version + " is newer than supported version "
                    + CURRENT_FORMAT_VERSION + ".");
        }
        return version;
    }

    private static String parseAppVersion(Object value, List<String> warnings) {
        if (value == null) {
            return null;
        }
        if (value instanceof String appVersionText) {
            if (appVersionText.isBlank()) {
                warnings.add("appVersion cannot be blank.");
                return null;
            }
            return appVersionText;
        }
        if (value instanceof Number number) {
            return String.valueOf(number);
        }
        warnings.add("appVersion must be a string or number.");
        return null;
    }

    private static String parseSeed(Object value) throws PresetException {
        if (value == null) {
            throw new PresetException("Preset is missing seed.");
        }
        if (value instanceof String seedText) {
            if (seedText.isBlank()) {
                throw new PresetException("Preset seed cannot be blank.");
            }
            return seedText;
        }
        if (value instanceof Number number) {
            return String.valueOf(number.longValue());
        }
        throw new PresetException("Preset seed must be a string or number.");
    }

    private static void warnAppVersionMismatch(String savedAppVersion, List<String> warnings) {
        if (savedAppVersion == null || savedAppVersion.isBlank()) {
            warnings.add("Preset does not record an appVersion.");
            return;
        }
        if (!savedAppVersion.equals(PtcgRandomizerVersion.VERSION)) {
            warnings.add("Preset was saved with PtcgRandomizer " + savedAppVersion
                    + "; current version is " + PtcgRandomizerVersion.VERSION + ".");
        }
    }

    private static List<ActionPreset> parseActions(Object value, List<String> warnings) {
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?> actionNodes)) {
            warnings.add("Actions must be a list; ignoring actions.");
            return List.of();
        }

        List<ActionPreset> actions = new ArrayList<>();
        for (int i = 0; i < actionNodes.size(); i++) {
            Object actionNode = actionNodes.get(i);
            String entryLabel = ACTIONS_KEY + "[" + i + "]";
            if (!(actionNode instanceof Map<?, ?> actionMap)) {
                warnings.add(entryLabel + ": action entry must be a mapping.");
                continue;
            }
            @SuppressWarnings("unchecked")
            ActionPreset parsed = ActionPreset.readFromSave((Map<String, Object>) actionMap,
                    warnings, entryLabel);
            if (parsed != null) {
                actions.add(parsed);
            }
        }
        return actions;
    }
}
