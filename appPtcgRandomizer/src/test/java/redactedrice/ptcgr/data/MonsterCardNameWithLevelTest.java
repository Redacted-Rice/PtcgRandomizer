package redactedrice.ptcgr.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import redactedrice.ptcgr.constants.CardConstants.CardId;

class MonsterCardNameWithLevelTest {
    private MonsterCard someMonster(int level, CardId id) {
        MonsterCard card = new MonsterCard();
        card.id = id;
        card.name.setText("SomeMonster");
        card.level = (byte) level;
        return card;
    }

    @Test
    void parsesNameWithLevel() {
        assertTrue(MonsterCard.isNameWithLevel("SomeMonster lvl65"));
        MonsterCard.NameWithLevel ref = MonsterCard.parseNameWithLevel("SomeMonster lvl65");
        assertEquals("SomeMonster", ref.name());
        assertEquals(65, ref.level());
    }

    @Test
    void rejectsPrintNumberInNameWithLevel() {
        assertNull(MonsterCard.parseNameWithLevel("SomeMonster_1 lvl76"));
    }

    @Test
    void findByNameWithLevelResolvesMatchingMonster() {
        CardGroup<MonsterCard> group = new CardGroup<>();
        group.add(someMonster(35, CardId.MONSTER_146_1));
        group.add(someMonster(37, CardId.MONSTER_146_2));

        MonsterCard found = MonsterCard.findByNameWithLevel(group, "SomeMonster lvl37");
        assertEquals(CardId.MONSTER_146_2, found.id);
        assertNull(MonsterCard.findByNameWithLevel(group, "SomeMonster lvl65"));
    }
}
