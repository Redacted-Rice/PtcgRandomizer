package redactedrice.ptcgr.rules.support;

import redactedrice.ptcgr.constants.CardConstants.CardId;
import redactedrice.ptcgr.data.Move;

public class MoveAssignmentData {
    private final CardId cardId;
    private final int moveSlot;
    private final Move move;
    private final String sourceFileName;

    public MoveAssignmentData(CardId cardId, int moveSlot, Move move, String sourceFileName) {
        this.cardId = cardId;
        this.moveSlot = moveSlot;
        this.move = new Move(move);
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

    public boolean hasSameTarget(MoveAssignmentData other) {
        return cardId == other.cardId && moveSlot == other.moveSlot;
    }

    public boolean hasSameSettings(MoveAssignmentData other) {
        return move.name.toString().equals(other.move.name.toString());
    }
}
