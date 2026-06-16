package redactedrice.ptcgr.config.support;

import redactedrice.ptcgr.constants.CardConstants.CardId;
import redactedrice.ptcgr.data.Move;

public class MoveExclusionData {
    private final CardId cardId;
    private final String moveName;
    private final boolean removeFromPool;
    private final boolean excludeFromRandomization;
    private final String sourceFileName;

    public MoveExclusionData(CardId cardId, String moveName, boolean removeFromPool,
            boolean excludeFromRandomization, String sourceFileName) {
        this.cardId = cardId;
        this.moveName = moveName;
        this.removeFromPool = removeFromPool;
        this.excludeFromRandomization = excludeFromRandomization;
        this.sourceFileName = sourceFileName;
    }

    public boolean matchesMove(CardId id, Move move) {
        return (!isCardIdSet() || cardId == id)
                && (moveName.isEmpty() || moveName.equals(move.name.toString()));
    }

    public boolean isCardIdSet() {
        return cardId != CardId.NO_CARD;
    }

    public CardId getCardId() {
        return cardId;
    }

    public boolean isMoveNameSet() {
        return !moveName.isEmpty();
    }

    public String getMoveName() {
        return moveName;
    }

    public boolean isRemoveFromPool() {
        return removeFromPool;
    }

    public boolean isExcludeFromRandomization() {
        return excludeFromRandomization;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public boolean hasSameTarget(MoveExclusionData other) {
        return cardId == other.cardId && moveName.equals(other.moveName);
    }

    public boolean hasSameSettings(MoveExclusionData other) {
        return removeFromPool == other.removeFromPool
                && excludeFromRandomization == other.excludeFromRandomization;
    }
}
