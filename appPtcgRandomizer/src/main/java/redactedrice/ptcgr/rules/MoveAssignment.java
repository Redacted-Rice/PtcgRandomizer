package redactedrice.ptcgr.rules;

import redactedrice.ptcgr.constants.romenums.CardId;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.data.Move;

public class MoveAssignment {
    private final CardId cardId;
    private final int moveSlot;
    private final Move move;
    private final String sourceFileName;
    private final String toCardSpecifier;
    private final String fromCardSpecifier;

    public MoveAssignment(CardId cardId, int moveSlot, Move move, String sourceFileName) {
        this(cardId, moveSlot, move, sourceFileName, "", "");
    }

    private MoveAssignment(CardId cardId, int moveSlot, Move move, String sourceFileName,
            String toCardSpecifier, String fromCardSpecifier) {
        this.cardId = cardId;
        this.moveSlot = moveSlot;
        this.move = move.copy();
        this.sourceFileName = sourceFileName;
        this.toCardSpecifier = toCardSpecifier != null ? toCardSpecifier : "";
        this.fromCardSpecifier = fromCardSpecifier != null ? fromCardSpecifier : "";
    }

    public static MoveAssignment pending(String toCardSpecifier, int moveSlot0Based,
            String moveName, String fromCardSpecifier, String sourceFileName) {
        return new MoveAssignment(CardId.NO_CARD, moveSlot0Based, stubMoveNamed(moveName),
                sourceFileName, toCardSpecifier, fromCardSpecifier);
    }

    private static Move stubMoveNamed(String moveName) {
        MonsterCard card = new MonsterCard();
        Move move = card.getMove(0);
        move.name.setText(moveName);
        return move;
    }

    public CardId getCardId() {
        return cardId;
    }

    public int getMoveSlot() {
        return moveSlot;
    }

    public Move getMove() {
        return move;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public boolean isCardIdSet() {
        return cardId != CardId.NO_CARD;
    }

    public boolean isPending() {
        return !isCardIdSet() && hasToCardSpecifier();
    }

    public boolean hasToCardSpecifier() {
        return !toCardSpecifier.isEmpty();
    }

    public String getToCardSpecifier() {
        return toCardSpecifier;
    }

    public String getFromCardSpecifier() {
        return fromCardSpecifier;
    }

    public String getMoveName() {
        return move.name.toString();
    }

    public boolean hasSameTarget(MoveAssignment other) {
        if (isPending() || other.isPending()) {
            return toCardSpecifier.equals(other.toCardSpecifier) && moveSlot == other.moveSlot;
        }
        return cardId == other.cardId && moveSlot == other.moveSlot;
    }

    public boolean hasSameSettings(MoveAssignment other) {
        if (!getMoveName().equals(other.getMoveName())) {
            return false;
        }
        if (isPending() || other.isPending()) {
            return fromCardSpecifier.equals(other.fromCardSpecifier);
        }
        return true;
    }
}
