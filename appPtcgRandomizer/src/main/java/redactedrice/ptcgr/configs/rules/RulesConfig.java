package redactedrice.ptcgr.configs.rules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.rules.MoveAssignment;
import redactedrice.ptcgr.rules.MoveAssignments;
import redactedrice.ptcgr.rules.MoveExclusion;
import redactedrice.ptcgr.rules.Rules;
import redactedrice.ptcgr.rom.RomData;
import redactedrice.ptcgr.utils.WarningCollector;

public final class RulesConfig {
    private final String sourceLabel;

    static final String MOVE_EXCLUSIONS_KEY = "moveExclusions";
    private final List<MoveExclusionConfig> moveExclusionConfigs;

    static final String MOVE_ASSIGNMENTS_KEY = "moveAssignments";
    private final List<MoveAssignmentConfig> moveAssignmentConfigs;

    public RulesConfig(String sourceLabel, List<MoveExclusionConfig> moveExclusionConfigs,
            List<MoveAssignmentConfig> moveAssignmentConfigs) {
        this.sourceLabel = sourceLabel != null ? sourceLabel : "<unknown source>";
        this.moveExclusionConfigs = List.copyOf(moveExclusionConfigs);
        this.moveAssignmentConfigs = List.copyOf(moveAssignmentConfigs);
    }

    public static RulesConfig empty() {
        return new RulesConfig("", List.of(), List.of());
    }

    public static RulesConfig readFromLoadedYamlMap(Map<String, Object> node, String sourceLabel,
            WarningCollector warnings) {
        if (node == null || node.isEmpty()) {
            warnings.addWarning("Rules must be a mapping; using empty rules.");
            return empty();
        }

        return new RulesConfig(sourceLabel,
                parseMoveExclusions(node.get(MOVE_EXCLUSIONS_KEY), sourceLabel, warnings),
                parseMoveAssignments(node.get(MOVE_ASSIGNMENTS_KEY), sourceLabel, warnings));
    }

    public Map<String, Object> convertToYamlMap() {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put(MOVE_EXCLUSIONS_KEY, convertToYamlMapExclusions(moveExclusionConfigs));
        node.put(MOVE_ASSIGNMENTS_KEY, convertToYamlMapAssignments(moveAssignmentConfigs));
        return node;
    }

    public void applyTo(Rules rules, CardGroup<MonsterCard> cards, WarningCollector warnings) {
        if (rules == null) {
            warnings.addWarning("Cannot apply rules because no ROM is loaded.");
            return;
        }

        for (int i = 0; i < moveExclusionConfigs.size(); i++) {
            String entryContext = sourceLabel + ":" + MOVE_EXCLUSIONS_KEY + "[" + i + "]";
            MoveExclusion exclusion = moveExclusionConfigs.get(i).toMoveExclusion(cards,
                    sourceLabel, entryContext, warnings);
            if (exclusion != null) {
                rules.addMoveExclusion(exclusion, cards);
            }
        }
        for (int i = 0; i < moveAssignmentConfigs.size(); i++) {
            String entryContext = sourceLabel + ":" + MOVE_ASSIGNMENTS_KEY + "[" + i + "]";
            MoveAssignment assignment = moveAssignmentConfigs.get(i).toMoveAssignment(cards,
                    sourceLabel, entryContext, warnings);
            if (assignment != null) {
                rules.addMoveAssignment(assignment);
            }
        }
    }

    public void recreateRules(Rules rules, CardGroup<MonsterCard> cards,
            WarningCollector warnings) {
        if (rules == null) {
            warnings.addWarning("Cannot apply rules because no ROM is loaded.");
            return;
        }
        rules.clear();
        applyTo(rules, cards, warnings);
    }

    public RulesConfig mergedWith(RulesConfig other) {
        String mergedSource = sourceLabel.isEmpty() ? other.sourceLabel : sourceLabel;
        List<MoveExclusionConfig> exclusions = new ArrayList<>(moveExclusionConfigs);
        exclusions.addAll(other.moveExclusionConfigs);
        List<MoveAssignmentConfig> assignments = new ArrayList<>(moveAssignmentConfigs);
        assignments.addAll(other.moveAssignmentConfigs);
        return new RulesConfig(mergedSource, exclusions, assignments);
    }

    public String getSourceLabel() {
        return sourceLabel;
    }

    public List<MoveExclusionConfig> getMoveExclusionConfigs() {
        return moveExclusionConfigs;
    }

    public List<MoveAssignmentConfig> getMoveAssignmentConfigs() {
        return moveAssignmentConfigs;
    }

    private static List<MoveExclusionConfig> parseMoveExclusions(Object value, String sourceLabel,
            WarningCollector warnings) {
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?> entries)) {
            warnings.addWarning(sourceLabel + ": \"" + MOVE_EXCLUSIONS_KEY + "\" must be a list.");
            return List.of();
        }

        List<MoveExclusionConfig> exclusions = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            Object entry = entries.get(i);
            String entryLabel = MOVE_EXCLUSIONS_KEY + "[" + i + "]";
            if (!(entry instanceof Map<?, ?> entryMap)) {
                warnings.addWarning(sourceLabel + ":" + entryLabel + ": entry must be a mapping.");
                continue;
            }
            @SuppressWarnings("unchecked")
            MoveExclusionConfig parsed = MoveExclusionConfig
                    .readFromLoadedYamlMap((Map<String, Object>) entryMap, warnings, entryLabel);
            if (parsed != null) {
                exclusions.add(parsed);
            }
        }
        return exclusions;
    }

    private static List<MoveAssignmentConfig> parseMoveAssignments(Object value, String sourceLabel,
            WarningCollector warnings) {
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?> entries)) {
            warnings.addWarning(sourceLabel + ": \"" + MOVE_ASSIGNMENTS_KEY + "\" must be a list.");
            return List.of();
        }

        List<MoveAssignmentConfig> assignments = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            Object entry = entries.get(i);
            String entryLabel = MOVE_ASSIGNMENTS_KEY + "[" + i + "]";
            if (!(entry instanceof Map<?, ?> entryMap)) {
                warnings.addWarning(sourceLabel + ":" + entryLabel + ": entry must be a mapping.");
                continue;
            }
            @SuppressWarnings("unchecked")
            MoveAssignmentConfig parsed = MoveAssignmentConfig
                    .readFromLoadedYamlMap((Map<String, Object>) entryMap, warnings, entryLabel);
            if (parsed != null) {
                assignments.add(parsed);
            }
        }
        return assignments;
    }

    private static List<Map<String, Object>> convertToYamlMapExclusions(
            List<MoveExclusionConfig> exclusions) {
        List<Map<String, Object>> entries = new ArrayList<>();
        for (MoveExclusionConfig exclusion : exclusions) {
            entries.add(exclusion.convertToYamlMap());
        }
        return entries;
    }

    private static List<Map<String, Object>> convertToYamlMapAssignments(
            List<MoveAssignmentConfig> assignments) {
        List<Map<String, Object>> entries = new ArrayList<>();
        for (MoveAssignmentConfig assignment : assignments) {
            entries.add(assignment.convertToYamlMap());
        }
        return entries;
    }
}
