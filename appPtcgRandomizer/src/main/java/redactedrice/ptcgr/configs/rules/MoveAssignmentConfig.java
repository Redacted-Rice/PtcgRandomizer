package redactedrice.ptcgr.configs.rules;

import java.util.LinkedHashMap;
import java.util.Map;
import redactedrice.ptcgr.configs.ParserHelpers;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.data.Move;
import redactedrice.ptcgr.rules.MoveAssignment;
import redactedrice.randomizer.utils.IssueTracker;

public final class MoveAssignmentConfig {
    static final String TO_CARD_KEY = "to_card";
    private final String toCard;

    static final String TO_MOVE_SLOT_KEY = "to_move_slot";
    private final String toMoveSlot;

    static final String MOVE_KEY = "move";
    private final String move;

    static final String FROM_CARD_KEY = "from_card";
    private final String fromCard;

    public MoveAssignmentConfig(String toCard, String toMoveSlot, String move, String fromCard) {
        this.toCard = toCard;
        this.toMoveSlot = toMoveSlot;
        this.move = move;
        this.fromCard = fromCard != null ? fromCard : "";
    }

    public static MoveAssignmentConfig fromMoveAssignment(MoveAssignment assignment,
            CardGroup<MonsterCard> cards) {
        if (assignment.isPending()) {
            return new MoveAssignmentConfig(assignment.getToCardSpecifier(),
                    String.valueOf(assignment.getMoveSlot() + 1), assignment.getMoveName(),
                    assignment.getFromCardSpecifier());
        }

        String toCardSpecifier = "";
        if (cards != null) {
            MonsterCard targetCard = cards.withId(assignment.getCardId());
            if (targetCard != null) {
                toCardSpecifier = targetCard.toNameWithLevelSpecifier();
            }
        }
        if (toCardSpecifier.isEmpty()) {
            toCardSpecifier = assignment.getCardId().toString();
        }
        MonsterCard sourceCard = assignment.getMove().getSourceCard();
        String fromCardSpecifier =
                sourceCard != null ? sourceCard.toNameWithLevelSpecifier() : "";
        return new MoveAssignmentConfig(toCardSpecifier,
                String.valueOf(assignment.getMoveSlot() + 1), assignment.getMove().name.toString(),
                fromCardSpecifier);
    }

    public static MoveAssignmentConfig readFromLoadedYamlMap(Map<String, Object> node, String entryLabel) {
        String toCard = ParserHelpers.parseRequiredString(node.get(TO_CARD_KEY), TO_CARD_KEY,
                entryLabel);
        String toMoveSlot = parseMoveSlot(node.get(TO_MOVE_SLOT_KEY), entryLabel);
        String move = ParserHelpers.parseRequiredString(node.get(MOVE_KEY), MOVE_KEY, entryLabel);
        if (toCard == null || toMoveSlot == null || move == null) {
            return null;
        }

        String fromCard = ParserHelpers.parseOptionalString(node.get(FROM_CARD_KEY));
        return new MoveAssignmentConfig(toCard, toMoveSlot, move, fromCard);
    }

    public Map<String, Object> convertToYamlMap() {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put(TO_CARD_KEY, toCard);
        entry.put(TO_MOVE_SLOT_KEY, Integer.parseInt(toMoveSlot));
        entry.put(MOVE_KEY, move);
        if (!fromCard.isEmpty()) {
            entry.put(FROM_CARD_KEY, fromCard);
        }
        return entry;
    }

    public MoveAssignment toMoveAssignment(CardGroup<MonsterCard> cards, String sourceLabel,
            String entryContext) {
        if (cards == null) {
            int moveSlot = parseMoveSlot0Based(toMoveSlot, entryContext);
            if (moveSlot < 0) {
                return null;
            }
            return MoveAssignment.pending(toCard, moveSlot, move, fromCard, sourceLabel);
        }

        MonsterCard targetCard = cards.resolveCard(toCard, entryContext);
        if (targetCard == null) {
            return null;
        }

        int moveSlot = cards.parseMoveSlotId(toMoveSlot, entryContext);
        if (moveSlot < 0) {
            return null;
        }

        MonsterCard hostCard =
                fromCard.isEmpty() ? null : cards.resolveCard(fromCard, entryContext);
        if (!fromCard.isEmpty() && hostCard == null) {
            return null;
        }

        Move moveToAssign = cards.resolveMoveByName(move, hostCard, entryContext);
        if (moveToAssign == null) {
            return null;
        }
        return new MoveAssignment(targetCard.id, moveSlot, moveToAssign, sourceLabel);
    }

    private static String parseMoveSlot(Object value, String entryLabel) {
        if (value == null) {
            IssueTracker.addWarning(
                    entryLabel + ": missing required field \"" + TO_MOVE_SLOT_KEY + "\".");
            return null;
        }
        if (value instanceof Number number) {
            return String.valueOf(number.intValue());
        }
        String trimmed = value.toString().trim();
        if (trimmed.isEmpty()) {
            IssueTracker.addWarning(
                    entryLabel + ": required field \"" + TO_MOVE_SLOT_KEY + "\" is empty.");
            return null;
        }
        return trimmed;
    }

    private static int parseMoveSlot0Based(String toMoveSlot, String entryContext) {
        Integer slot = ParserHelpers.parseInteger(toMoveSlot);
        if (slot == null || slot < 1) {
            IssueTracker.addWarning(entryContext + ": invalid move slot \"" + toMoveSlot + "\".");
            return -1;
        }
        return slot - 1;
    }

    public String getToCard() {
        return toCard;
    }

    public String getToMoveSlot() {
        return toMoveSlot;
    }

    public String getMove() {
        return move;
    }

    public String getFromCard() {
        return fromCard;
    }
}
