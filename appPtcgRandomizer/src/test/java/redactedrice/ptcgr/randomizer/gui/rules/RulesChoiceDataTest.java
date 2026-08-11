package redactedrice.ptcgr.randomizer.gui.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import redactedrice.ptcgr.constants.romenums.CardId;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.data.Move;

class RulesChoiceDataTest {
    @Test
    void distinctMoveNamesCollectsNonEmptyMoves() {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(monster(35, CardId.MONSTER_146_1, "Ember"));
        cards.add(monster(37, CardId.MONSTER_146_2, "OtherMove"));

        assertEquals(List.of("Ember", "OtherMove"), RulesChoiceData.distinctMoveNames(cards));
    }

    @Test
    void moveExistsOnMultipleCardsDetectsSharedMoveName() {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(monster(35, CardId.MONSTER_146_1, "SharedMove"));
        cards.add(monster(37, CardId.MONSTER_146_2, "SharedMove"));

        assertTrue(RulesChoiceData.moveExistsOnMultipleCards(cards, "SharedMove"));
        assertFalse(RulesChoiceData.moveExistsOnMultipleCards(cards, "Ember"));
    }

    @Test
    void soleCardWithMoveReturnsCardOnlyWhenUnique() {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        MonsterCard only = monster(35, CardId.MONSTER_146_1, "UniqueMove");
        MonsterCard other = monster(37, CardId.MONSTER_146_2, "OtherMove");
        cards.add(only);
        cards.add(other);

        assertEquals(only, RulesChoiceData.soleCardWithMove(cards, "UniqueMove"));
        assertEquals(other, RulesChoiceData.soleCardWithMove(cards, "OtherMove"));
    }

    @Test
    void resolveCardTreatsAnyCardLabelAsAbsent() {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(monster(35, CardId.MONSTER_146_1, "Ember"));

        assertNull(RulesChoiceData.resolveCard(cards, RulesChoiceData.ANY_CARD_LABEL));
        assertEquals(cards.withId(CardId.MONSTER_146_1),
                RulesChoiceData.resolveCard(cards, "SomeMonster lvl35"));
    }

    private static MonsterCard monster(int level, CardId id, String moveName) {
        MonsterCard card = new MonsterCard();
        card.id = id;
        card.name.setText("SomeMonster");
        card.level = (byte) level;
        Move move = card.getMove(0);
        move.name.setText(moveName);
        card.setMoves(List.of(move, card.getMove(1)));
        return card;
    }
}
