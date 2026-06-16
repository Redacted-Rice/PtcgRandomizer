package redactedrice.ptcgr.rules.parser;

import java.util.List;
import java.util.Map;

import redactedrice.ptcgr.rules.support.RulesWarningCollector;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.data.Move;

public final class YamlParser {
    private final CardGroup<MonsterCard> monsterCards;
    private final RulesWarningCollector warnings;

    public YamlParser(CardGroup<MonsterCard> monsterCards, RulesWarningCollector warnings) {
        this.monsterCards = monsterCards;
        this.warnings = warnings;
    }

    @FunctionalInterface
    public interface EntryHandler {
        void handle(Map<String, Object> fields, String entryContext);
    }

    public static String entryContext(String sourceFileName, String entryPath) {
        return sourceFileName + ":" + entryPath;
    }

    public static void forEachEntryInList(Object rawList, String listFieldName,
            String sourceFileName, RulesWarningCollector warnings, EntryHandler handler) {
        if (rawList == null) {
            return;
        }
        if (!(rawList instanceof List)) {
            warnings.appendWarningLine(
                    sourceFileName + ": \"" + listFieldName + "\" must be a list.");
            return;
        }

        List<?> entries = (List<?>) rawList;
        for (int i = 0; i < entries.size(); i++) {
            String entryContext = entryContext(sourceFileName, listFieldName + "[" + i + "]");
            Object entry = entries.get(i);
            if (!(entry instanceof Map)) {
                warnings.appendWarningLine(entryContext + " is not a mapping and was skipped.");
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> fields = (Map<String, Object>) entry;
            handler.handle(fields, entryContext);
        }
    }

    public static boolean parseBoolean(Object value, boolean defaultValue, String fieldName,
            String entryContext, RulesWarningCollector warnings) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }

        warnings.appendWarningLine(entryContext + " field \"" + fieldName
                + "\" must be a boolean; false will be assumed.");
        return false;
    }

    public static String parseOptionalString(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString().trim();
    }

    public static String parseRequiredString(Object value, String fieldName, String entryContext,
            RulesWarningCollector warnings) {
        if (value == null) {
            warnings.appendWarningLine(entryContext + " is missing required field \"" + fieldName
                    + "\" and was skipped.");
            return null;
        }

        String trimmed = value.toString().trim();
        if (trimmed.isEmpty()) {
            warnings.appendWarningLine(entryContext + " required field \"" + fieldName
                    + "\" is empty and was skipped.");
            return null;
        }

        return trimmed;
    }

    public boolean isKnownMoveName(String moveName) {
        return monsterCards.allMoves().stream()
                .anyMatch(m -> m.name.toString().equalsIgnoreCase(moveName.trim()));
    }

    public MonsterCard resolveCard(String cardSpecifier, String entryContext) {
        String trimmed = cardSpecifier.trim();
        if (!MonsterCard.isNameWithLevel(trimmed)) {
            String exampleName = trimmed.isEmpty() ? "SomeMonster" : trimmed;
            warnings.appendWarningLine(
                    "Monster card \"" + cardSpecifier + "\" must use name and level (e.g. \""
                            + exampleName + " lvl65\") in " + entryContext + ".");
            warnings.appendWarning("\t" + entryContext);
            return null;
        }

        MonsterCard card = MonsterCard.findByNameWithLevel(monsterCards, trimmed);
        if (card == null) {
            warnings.appendWarningLine(
                    "Failed to resolve card \"" + cardSpecifier + "\" in " + entryContext + ":");
            warnings.appendWarning("\t" + entryContext);
        }
        return card;
    }

    public Move resolveMoveOnCard(MonsterCard hostCard, String moveName, String entryContext) {
        Move moveWithName = hostCard.getMoveWithName(moveName);
        if (moveWithName != null) {
            return moveWithName;
        }

        warnings.appendWarningLine("Failed to find move \"" + moveName + "\" on card \""
                + hostCard.name + "\" in " + entryContext + ":");
        warnings.appendWarning("\t" + entryContext);
        return null;
    }

    /**
     * Assignment target slots are 1 based in files. Internal storage is 0 based.
     */
    public int parseAssignmentMoveSlotId(String slotSpecifier, String entryContext) {
        try {
            int oneBasedSlot = Integer.parseInt(slotSpecifier);
            if (oneBasedSlot >= 1 && oneBasedSlot <= MonsterCard.MAX_NUM_MOVES) {
                return oneBasedSlot - 1;
            }

            warnings.appendWarningLine("to_move_slot \"" + slotSpecifier + "\" is out of range in "
                    + entryContext + "; use " + 1 + "-" + MonsterCard.MAX_NUM_MOVES + ":");
            warnings.appendWarning("\t" + entryContext);
            return -1;
        } catch (NumberFormatException ignored) {
            warnings.appendWarningLine("to_move_slot must be a 1-based slot number (1-"
                    + MonsterCard.MAX_NUM_MOVES + ") in " + entryContext + ":");
            warnings.appendWarning("\t" + entryContext);
            return -1;
        }
    }

    public boolean cardHasMove(MonsterCard card, String moveName) {
        return card.getMoveWithName(moveName) != null;
    }

    /**
     * Resolved by move name. if optionalFromCard is null the first card in ROM order with that move
     * is used and a warning is logged if more than one card matches.
     */
    public Move resolveAssignmentMoveByName(String moveName, MonsterCard optionalFromCard,
            String entryContext) {
        if (optionalFromCard != null) {
            return resolveMoveOnCard(optionalFromCard, moveName, entryContext);
        }

        MonsterCard firstHost = null;
        for (MonsterCard card : monsterCards.iterable()) {
            if (card.getMoveWithName(moveName) == null) {
                continue;
            }
            if (firstHost != null) {
                warnings.appendWarningLine(
                        "Move \"" + moveName + "\" was found on multiple cards in " + entryContext
                                + "; using the first match:");
                warnings.appendWarning("\t" + entryContext);
                return firstHost.getMoveWithName(moveName);
            }
            firstHost = card;
        }

        if (firstHost == null) {
            warnings.appendWarningLine("Failed to find move \"" + moveName + "\" on any card in "
                    + entryContext + ":");
            warnings.appendWarning("\t" + entryContext);
            return null;
        }

        return firstHost.getMoveWithName(moveName);
    }
}
