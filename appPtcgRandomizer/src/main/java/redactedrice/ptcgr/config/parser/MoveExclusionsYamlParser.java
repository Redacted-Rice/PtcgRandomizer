package redactedrice.ptcgr.config.parser;

import java.util.Map;

import redactedrice.ptcgr.config.MoveExclusions;
import redactedrice.ptcgr.config.support.ConfigWarningCollector;
import redactedrice.ptcgr.constants.CardConstants.CardId;
import redactedrice.ptcgr.data.MonsterCard;

public final class MoveExclusionsYamlParser {
    private MoveExclusionsYamlParser() {}

    public static void loadList(Object rawExclusions, String sourceFileName,
            MoveExclusions moveExclusions, YamlParser parser, ConfigWarningCollector warnings) {
        YamlParser.forEachEntryInList(rawExclusions, "exclusions", sourceFileName, warnings,
                (fields, entryContext) -> parseEntry(fields, sourceFileName, entryContext,
                        moveExclusions, parser, warnings));
    }

    private static void parseEntry(Map<String, Object> fields, String sourceFileName,
            String entryContext, MoveExclusions moveExclusions, YamlParser parser,
            ConfigWarningCollector warnings) {
        boolean removeFromPool = YamlParser.parseBoolean(fields.get("remove_from_pool"), false,
                "remove_from_pool", entryContext, warnings);
        boolean excludeFromRandomization =
                YamlParser.parseBoolean(fields.get("exclude_from_randomization"), false,
                        "exclude_from_randomization", entryContext, warnings);

        String move = YamlParser.parseRequiredString(fields.get("move"), "move", entryContext,
                warnings);
        if (move == null) {
            return;
        }

        String card = YamlParser.parseOptionalString(fields.get("card"));
        if (card.isEmpty()) {
            if (!parser.isKnownMoveName(move)) {
                warnings.appendWarningLine("Failed to find any card with the specified move for \""
                        + move + "\" so " + entryContext + " will be skipped:");
                warnings.appendWarning("\t" + entryContext);
                return;
            }
            moveExclusions.addMoveExclusion(CardId.NO_CARD, move, removeFromPool,
                    excludeFromRandomization, sourceFileName, warnings);
            return;
        }

        MonsterCard monsterCard = parser.resolveNumberedCard(card, entryContext);
        if (monsterCard == null) {
            return;
        }

        if (!parser.cardHasMove(monsterCard, move)) {
            warnings.appendWarningLine("Failed to find move with name \"" + move + "\" on card \""
                    + card + "\" so " + entryContext + " will be skipped:");
            warnings.appendWarning("\t" + entryContext);
            return;
        }

        moveExclusions.addMoveExclusion(monsterCard.id, move, removeFromPool,
                excludeFromRandomization, sourceFileName, warnings);
    }
}
