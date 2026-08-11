package redactedrice.ptcgr.rules;

import redactedrice.ptcgr.constants.romenums.CardId;
import redactedrice.ptcgr.data.Move;

public class MoveExclusion {
    private final CardId cardId;
    private final String cardSpecifier;
    private final String moveName;
    private final boolean removeFromPool;
    private final boolean excludeFromRandomization;
    private final String sourceFileName;

    public MoveExclusion(CardId cardId, String moveName, boolean removeFromPool,
            boolean excludeFromRandomization, String sourceFileName) {
        this(cardId, moveName, removeFromPool, excludeFromRandomization, sourceFileName, "");
    }

    public MoveExclusion(CardId cardId, String moveName, boolean removeFromPool,
            boolean excludeFromRandomization, String sourceFileName, String cardSpecifier) {
        this.cardId = cardId;
        this.cardSpecifier = cardSpecifier != null ? cardSpecifier : "";
        this.moveName = moveName;
        this.removeFromPool = removeFromPool;
        this.excludeFromRandomization = excludeFromRandomization;
        this.sourceFileName = sourceFileName;
    }

    public boolean matchesMove(CardId id, Move move) {
        // pending card scoped entries are not resolved yet so they must not match any card
        if (isPending()) {
            return false;
        }
        return (!isCardIdSet() || cardId == id)
                && (moveName.isEmpty() || moveName.equals(move.name.toString()));
    }

    public boolean isCardIdSet() {
        return cardId != CardId.NO_CARD;
    }

    /** Card named in yaml/ui but not resolved to a CardId yet (no ROM). */
    public boolean isPending() {
        return !isCardIdSet() && hasCardSpecifier();
    }

    public boolean hasCardSpecifier() {
        return !cardSpecifier.isEmpty();
    }

    public CardId getCardId() {
        return cardId;
    }

    public String getCardSpecifier() {
        return cardSpecifier;
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

    public MoveExclusion withRemoveFromPool(boolean removeFromPool) {
        return new MoveExclusion(cardId, moveName, removeFromPool, excludeFromRandomization,
                sourceFileName, cardSpecifier);
    }

    public MoveExclusion withExcludeFromRandomization(boolean excludeFromRandomization) {
        return new MoveExclusion(cardId, moveName, removeFromPool, excludeFromRandomization,
                sourceFileName, cardSpecifier);
    }

    public boolean hasSameTarget(MoveExclusion other) {
        return cardId == other.cardId && moveName.equals(other.moveName)
                && cardSpecifier.equals(other.cardSpecifier);
    }

    public boolean hasSameSettings(MoveExclusion other) {
        return removeFromPool == other.removeFromPool
                && excludeFromRandomization == other.excludeFromRandomization;
    }
}
