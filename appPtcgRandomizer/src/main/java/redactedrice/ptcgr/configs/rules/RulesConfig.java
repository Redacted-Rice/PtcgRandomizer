package redactedrice.ptcgr.configs.rules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import redactedrice.ptcgr.configs.ParserHelpers;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.rules.MoveAssignment;
import redactedrice.ptcgr.rules.MoveExclusion;
import redactedrice.ptcgr.rules.Rules;
import redactedrice.randomizer.utils.IssueTracker;

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

    public static RulesConfig readFromLoadedYamlMap(Map<String, Object> node, String sourceLabel) {
        if (node == null || node.isEmpty()) {
            IssueTracker.addWarning("Rules must be a mapping; using empty rules.");
            return empty();
        }

        return new RulesConfig(sourceLabel,
                parseMoveExclusions(node.get(MOVE_EXCLUSIONS_KEY), sourceLabel),
                parseMoveAssignments(node.get(MOVE_ASSIGNMENTS_KEY), sourceLabel));
    }

    public Map<String, Object> convertToYamlMap() {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put(MOVE_EXCLUSIONS_KEY, convertToYamlMapExclusions(moveExclusionConfigs));
        node.put(MOVE_ASSIGNMENTS_KEY, convertToYamlMapAssignments(moveAssignmentConfigs));
        return node;
    }

    public void applyTo(Rules rules, CardGroup<MonsterCard> cards) {
        if (rules == null) {
            IssueTracker.addWarning("Cannot apply rules because no ROM is loaded.");
            return;
        }

        for (int i = 0; i < moveExclusionConfigs.size(); i++) {
            String entryContext = sourceLabel + ":" + MOVE_EXCLUSIONS_KEY + "[" + i + "]";
            MoveExclusion exclusion = moveExclusionConfigs.get(i).toMoveExclusion(cards,
                    sourceLabel, entryContext);
            if (exclusion != null) {
                rules.addMoveExclusion(exclusion, cards);
            }
        }
        for (int i = 0; i < moveAssignmentConfigs.size(); i++) {
            String entryContext = sourceLabel + ":" + MOVE_ASSIGNMENTS_KEY + "[" + i + "]";
            MoveAssignment assignment = moveAssignmentConfigs.get(i).toMoveAssignment(cards,
                    sourceLabel, entryContext);
            if (assignment != null) {
                rules.addMoveAssignment(assignment);
            }
        }
    }

    public void recreateRules(Rules rules, CardGroup<MonsterCard> cards) {
        if (rules == null) {
            IssueTracker.addWarning("Cannot apply rules because no ROM is loaded.");
            return;
        }
        rules.clear();
        applyTo(rules, cards);
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

    private static List<MoveExclusionConfig> parseMoveExclusions(Object value, String sourceLabel) {
        List<MoveExclusionConfig> exclusions = new ArrayList<>();
        ParserHelpers.forEachEntryInList(value, MOVE_EXCLUSIONS_KEY, sourceLabel,
                (fields, entryContext) -> {
                    MoveExclusionConfig parsed = MoveExclusionConfig.readFromLoadedYamlMap(fields, entryContext);
                    if (parsed != null) {
                        exclusions.add(parsed);
                    }
                });
        return exclusions;
    }

    private static List<MoveAssignmentConfig> parseMoveAssignments(Object value, String sourceLabel) {
        List<MoveAssignmentConfig> assignments = new ArrayList<>();
        ParserHelpers.forEachEntryInList(value, MOVE_ASSIGNMENTS_KEY, sourceLabel,
                (fields, entryContext) -> {
                    MoveAssignmentConfig parsed = MoveAssignmentConfig.readFromLoadedYamlMap(fields, entryContext);
                    if (parsed != null) {
                        assignments.add(parsed);
                    }
                });
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
