package redactedrice.ptcgr.config.support;


import redactedrice.ptcgr.constants.CardConstants.CardId;
import redactedrice.ptcgr.data.Move;

public class MoveAssignmentData {
    private CardId cardId;
    private int moveSlot;
    private Move move;

    public MoveAssignmentData(CardId cardId, int moveSlot, Move move) {
        this.cardId = cardId;
        this.moveSlot = moveSlot;
        this.move = new Move(move);
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
}
