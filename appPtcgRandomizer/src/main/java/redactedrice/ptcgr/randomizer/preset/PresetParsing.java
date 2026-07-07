package redactedrice.ptcgr.randomizer.preset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class PresetParsing {
    private PresetParsing() {}

    static RandomizerPreset parseDocument(Map<String, Object> root, List<String> warnings)
            throws PresetLoadException {
        if (root == null) {
            throw new PresetLoadException("Preset file is empty.");
        }

        int version = parseVersion(root.get("version"), warnings);
        String seed = parseSeed(root.get("seed"));
        List<ActionPreset> actions = parseActions(root.get("actions"), warnings);
        return new RandomizerPreset(version, seed, actions);
    }

    static int parseVersion(Object value, List<String> warnings) {
        Integer version = parseInteger(value);
        if (version == null) {
            warnings.add("Missing or invalid version; assuming version "
                    + RandomizerPreset.CURRENT_VERSION + ".");
            return RandomizerPreset.CURRENT_VERSION;
        }
        if (version > RandomizerPreset.CURRENT_VERSION) {
            warnings.add("Preset version " + version + " is newer than supported version "
                    + RandomizerPreset.CURRENT_VERSION + ".");
        }
        return version;
    }

    static String parseSeed(Object value) throws PresetLoadException {
        if (value == null) {
            throw new PresetLoadException("Preset is missing seed.");
        }
        if (value instanceof String seedText) {
            if (seedText.isBlank()) {
                throw new PresetLoadException("Preset seed cannot be blank.");
            }
            return seedText;
        }
        if (value instanceof Number number) {
            return String.valueOf(number.longValue());
        }
        throw new PresetLoadException("Preset seed must be a string or number.");
    }

    static Integer parseInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text) {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
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
            String entryLabel = "actions[" + i + "]";
            if (!(actionNode instanceof Map<?, ?> actionMap)) {
                warnings.add(entryLabel + ": action entry must be a mapping.");
                continue;
            }
            @SuppressWarnings("unchecked")
            ActionPreset parsed = ActionPreset.fromDocumentMap((Map<String, Object>) actionMap,
                    warnings, entryLabel);
            if (parsed != null) {
                actions.add(parsed);
            }
        }
        return actions;
    }
}
