package redactedrice.ptcgr.configs.rules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import redactedrice.ptcgr.configs.ParserHelpers;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.rules.MoveAssignment;
import redactedrice.ptcgr.rules.MoveAssignments;
import redactedrice.ptcgr.rules.MoveExclusion;
import redactedrice.ptcgr.rules.Rules;

public final class RulesConfig {
    private final String sourceLabel;

    static final String MOVE_EXCLUSIONS_KEY = "moveExclusions";
    private final List<MoveExclusionConfig> moveExclusionConfigs;

    static final String MOVE_ASSIGNMENTS_KEY = "moveAssignments";
    private final List<MoveAssignmentConfig> moveAssignmentConfigs;

    private final boolean moveExclusionsLoaded;
    private final boolean moveAssignmentsLoaded;

    public RulesConfig(String sourceLabel, List<MoveExclusionConfig> moveExclusionConfigs,
            List<MoveAssignmentConfig> moveAssignmentConfigs) {
        this(sourceLabel, moveExclusionConfigs, moveAssignmentConfigs, true, true);
    }

    private RulesConfig(String sourceLabel, List<MoveExclusionConfig> moveExclusionConfigs,
            List<MoveAssignmentConfig> moveAssignmentConfigs, boolean moveExclusionsLoaded,
            boolean moveAssignmentsLoaded) {
        this.sourceLabel = sourceLabel != null ? sourceLabel : "<unknown source>";
        this.moveExclusionConfigs = List.copyOf(moveExclusionConfigs);
        this.moveAssignmentConfigs = List.copyOf(moveAssignmentConfigs);
        this.moveExclusionsLoaded = moveExclusionsLoaded;
        this.moveAssignmentsLoaded = moveAssignmentsLoaded;
    }

    public static RulesConfig empty() {
        return new RulesConfig("", List.of(), List.of(), false, false);
    }

    public static RulesConfig readFromLoadedYamlMap(Map<String, Object> node, String sourceLabel) {
        if (node == null) {
            return empty();
        }

        boolean hasExclusions = node.containsKey(MOVE_EXCLUSIONS_KEY);
        boolean hasAssignments = node.containsKey(MOVE_ASSIGNMENTS_KEY);
        if (!hasExclusions && !hasAssignments) {
            return empty();
        }

        List<MoveExclusionConfig> exclusions = hasExclusions
                ? parseMoveExclusions(node.get(MOVE_EXCLUSIONS_KEY), sourceLabel)
                : List.of();
        List<MoveAssignmentConfig> assignments = hasAssignments
                ? parseMoveAssignments(node.get(MOVE_ASSIGNMENTS_KEY), sourceLabel)
                : List.of();
        return new RulesConfig(sourceLabel, exclusions, assignments, hasExclusions, hasAssignments);
    }

    public Map<String, Object> convertToYamlMap() {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put(MOVE_EXCLUSIONS_KEY, convertToYamlMapExclusions(moveExclusionConfigs));
        node.put(MOVE_ASSIGNMENTS_KEY, convertToYamlMapAssignments(moveAssignmentConfigs));
        return node;
    }

    public boolean hasMoveExclusions() {
        return moveExclusionsLoaded;
    }

    public boolean hasMoveAssignments() {
        return moveAssignmentsLoaded;
    }

    public boolean hasAnySection() {
        return moveExclusionsLoaded || moveAssignmentsLoaded;
    }

    public void applyTo(Rules rules, CardGroup<MonsterCard> cards) {
        if (rules == null) {
            return;
        }

        if (moveExclusionsLoaded) {
            for (int i = 0; i < moveExclusionConfigs.size(); i++) {
                String entryContext = sourceLabel + ":" + MOVE_EXCLUSIONS_KEY + "[" + i + "]";
                MoveExclusion exclusion = moveExclusionConfigs.get(i).toMoveExclusion(cards,
                        sourceLabel, entryContext);
                if (exclusion != null) {
                    rules.addMoveExclusion(exclusion, cards, false);
                }
            }
        }
        if (moveAssignmentsLoaded) {
            for (int i = 0; i < moveAssignmentConfigs.size(); i++) {
                String entryContext = sourceLabel + ":" + MOVE_ASSIGNMENTS_KEY + "[" + i + "]";
                MoveAssignment assignment = moveAssignmentConfigs.get(i).toMoveAssignment(cards,
                        sourceLabel, entryContext);
                if (assignment != null) {
                    rules.addMoveAssignment(assignment, cards, false);
                }
            }
        }
    }

    public static RulesConfig fromRules(Rules rules, CardGroup<MonsterCard> cards) {
        if (rules == null) {
            return empty();
        }

        List<MoveExclusionConfig> exclusions = new ArrayList<>();
        for (MoveExclusion exclusion : rules.getMoveExclusions().getAllExclusions()) {
            exclusions.add(MoveExclusionConfig.fromMoveExclusion(exclusion, cards));
        }

        List<MoveAssignmentConfig> assignments = new ArrayList<>();
        for (MoveAssignment assignment : rules.getMoveAssignments().getAllAssignments()) {
            if (!MoveAssignments.isAssignmentDerivedExclusionSource(assignment.getSourceFileName())) {
                assignments.add(MoveAssignmentConfig.fromMoveAssignment(assignment, cards));
            }
        }
        return new RulesConfig("", exclusions, assignments);
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
                    MoveExclusionConfig parsed = MoveExclusionConfig.readFromLoadedYamlMap(fields,
                            entryContext, sourceLabel);
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
                    MoveAssignmentConfig parsed = MoveAssignmentConfig.readFromLoadedYamlMap(fields,
                            entryContext);
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
