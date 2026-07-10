package redactedrice.ptcgr.configs.rules;

import java.util.LinkedHashMap;
import java.util.Map;
import redactedrice.ptcgr.configs.ParserHelpers;
import redactedrice.ptcgr.constants.CardConstants.CardId;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.rules.MoveExclusion;
import redactedrice.ptcgr.utils.WarningCollector;

public final class MoveExclusionConfig {
    static final String REMOVE_FROM_POOL_KEY = "remove_from_pool";
    private final boolean removeFromPool;

    static final String EXCLUDE_FROM_RANDOMIZATION_KEY = "exclude_from_randomization";
    private final boolean excludeFromRandomization;

    static final String MOVE_KEY = "move";
    private final String move;

    static final String CARD_KEY = "card";
    private final String card;

    public MoveExclusionConfig(boolean removeFromPool, boolean excludeFromRandomization,
            String move, String card) {
        this.removeFromPool = removeFromPool;
        this.excludeFromRandomization = excludeFromRandomization;
        this.move = move;
        this.card = card != null ? card : "";
    }

    public static MoveExclusionConfig fromMoveExclusion(MoveExclusion exclusion,
            CardGroup<MonsterCard> cards) {
        String cardSpecifier = "";
        if (exclusion.isCardIdSet()) {
            MonsterCard monster = cards.withId(exclusion.getCardId());
            if (monster != null) {
                cardSpecifier = monster.toNameWithLevelSpecifier();
            }
        }
        return new MoveExclusionConfig(exclusion.isRemoveFromPool(),
                exclusion.isExcludeFromRandomization(), exclusion.getMoveName(), cardSpecifier);
    }

    public static MoveExclusionConfig readFromLoadedYamlMap(Map<String, Object> node,
            WarningCollector warnings, String entryLabel) {
        boolean removeFromPool = ParserHelpers.parseBoolean(node.get(REMOVE_FROM_POOL_KEY), false,
                REMOVE_FROM_POOL_KEY, entryLabel, warnings);
        boolean excludeFromRandomization =
                ParserHelpers.parseBoolean(node.get(EXCLUDE_FROM_RANDOMIZATION_KEY), false,
                        EXCLUDE_FROM_RANDOMIZATION_KEY, entryLabel, warnings);

        String move = ParserHelpers.parseRequiredString(node.get(MOVE_KEY), MOVE_KEY, entryLabel,
                warnings);
        if (move == null) {
            return null;
        }

        String card = ParserHelpers.parseOptionalString(node.get(CARD_KEY));
        return new MoveExclusionConfig(removeFromPool, excludeFromRandomization, move, card);
    }

    public Map<String, Object> convertToYamlMap() {
        Map<String, Object> entry = new LinkedHashMap<>();
        if (removeFromPool) {
            entry.put(REMOVE_FROM_POOL_KEY, true);
        }
        if (excludeFromRandomization) {
            entry.put(EXCLUDE_FROM_RANDOMIZATION_KEY, true);
        }
        entry.put(MOVE_KEY, move);
        if (!card.isEmpty()) {
            entry.put(CARD_KEY, card);
        }
        return entry;
    }

    public MoveExclusion toMoveExclusion(CardGroup<MonsterCard> cards, String sourceLabel,
            String entryContext, WarningCollector warnings) {
        if (card.isEmpty()) {
            if (!cards.isKnownMoveName(move)) {
                warnings.addWarning(entryContext + ": failed to find any card with move \"" + move
                        + "\"; entry skipped.");
                return null;
            }
            return new MoveExclusion(CardId.NO_CARD, move, removeFromPool, excludeFromRandomization,
                    sourceLabel);
        }

        MonsterCard monsterCard = cards.resolveCard(card, entryContext, warnings);
        if (monsterCard == null) {
            return null;
        }

        if (!cards.cardHasMove(monsterCard, move)) {
            warnings.addWarning(entryContext + ": failed to find move \"" + move + "\" on card \""
                    + card + "\"; entry skipped.");
            return null;
        }

        return new MoveExclusion(monsterCard.id, move, removeFromPool, excludeFromRandomization,
                sourceLabel);
    }

    public boolean isRemoveFromPool() {
        return removeFromPool;
    }

    public boolean isExcludeFromRandomization() {
        return excludeFromRandomization;
    }

    public String getMove() {
        return move;
    }

    public String getCard() {
        return card;
    }
}
