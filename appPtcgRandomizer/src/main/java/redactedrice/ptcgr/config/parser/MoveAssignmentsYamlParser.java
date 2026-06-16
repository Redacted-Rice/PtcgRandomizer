package redactedrice.ptcgr.config.parser;

import java.util.Map;

import redactedrice.ptcgr.config.MoveAssignments;
import redactedrice.ptcgr.config.support.ConfigWarningCollector;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.data.Move;

public final class MoveAssignmentsYamlParser {
    private MoveAssignmentsYamlParser() {}

    public static void loadList(Object rawAssignments, String sourceFileName,
            MoveAssignments moveAssignments, YamlParser parser, ConfigWarningCollector warnings) {
        YamlParser.forEachEntryInList(rawAssignments, "assignments", sourceFileName, warnings,
                (fields, entryContext) -> parseEntry(fields, sourceFileName, entryContext,
                        moveAssignments, parser, warnings));
    }

    private static void parseEntry(Map<String, Object> fields, String sourceFileName,
            String entryContext, MoveAssignments moveAssignments, YamlParser parser,
            ConfigWarningCollector warnings) {
        String toCard = YamlParser.parseRequiredString(fields.get("to_card"), "to_card",
                entryContext, warnings);
        String toMoveSlot = YamlParser.parseRequiredString(fields.get("to_move_slot"),
                "to_move_slot", entryContext, warnings);
        String move =
                YamlParser.parseRequiredString(fields.get("move"), "move", entryContext, warnings);
        if (toCard == null || toMoveSlot == null || move == null) {
            return;
        }

        String fromCard = YamlParser.parseOptionalString(fields.get("from_card"));

        MonsterCard targetCard = parser.resolveNumberedCard(toCard, entryContext);
        if (targetCard == null) {
            return;
        }

        int moveSlot = parser.parseAssignmentMoveSlotId(toMoveSlot, entryContext);
        if (moveSlot < 0) {
            return;
        }

        MonsterCard hostCard =
                fromCard.isEmpty() ? null : parser.resolveNumberedCard(fromCard, entryContext);
        if (!fromCard.isEmpty() && hostCard == null) {
            return;
        }

        Move moveToAssign = parser.resolveAssignmentMoveByName(move, hostCard, entryContext);
        if (moveToAssign != null) {
            moveAssignments.addMoveAssignment(targetCard, moveSlot, moveToAssign, sourceFileName,
                    warnings);
        }
    }
}
