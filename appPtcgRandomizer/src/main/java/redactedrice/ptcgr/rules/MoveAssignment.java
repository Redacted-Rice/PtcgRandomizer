package redactedrice.ptcgr.rules;

import redactedrice.ptcgr.constants.CardConstants.CardId;
import redactedrice.ptcgr.data.Move;

public class MoveAssignment {
    private final CardId cardId;
    private final int moveSlot;
    private final Move move;
    private final String sourceFileName;

    public MoveAssignment(CardId cardId, int moveSlot, Move move, String sourceFileName) {
        this.cardId = cardId;
        this.moveSlot = moveSlot;
        this.move = move.copy();
        this.sourceFileName = sourceFileName;
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

    public boolean hasSameTarget(MoveAssignment other) {
        return cardId == other.cardId && moveSlot == other.moveSlot;
    }

    public boolean hasSameSettings(MoveAssignment other) {
        return move.name.toString().equals(other.move.name.toString());
    }
}
