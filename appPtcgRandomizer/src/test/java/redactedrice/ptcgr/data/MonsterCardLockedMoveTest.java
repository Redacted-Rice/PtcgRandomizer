package redactedrice.ptcgr.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import redactedrice.ptcgr.constants.CardConstants.CardId;
import redactedrice.ptcgr.rules.MoveAssignments;
import redactedrice.ptcgr.utils.WarningCollector;

class MonsterCardLockedMoveTest {
    private MonsterCard someMonster(int level, CardId id) {
        MonsterCard card = new MonsterCard();
        card.id = id;
        card.name.setText("SomeMonster");
        card.level = (byte) level;
        return card;
    }

    private Move namedMove(String moveName) {
        MonsterCard scratch = new MonsterCard();
        scratch.peekMove(0).name.setText(moveName);
        return scratch.peekMove(0);
    }

    @Test
    void setMoveCopiesDataWithoutChangingSlotBinding() {
        MonsterCard sourceCard = someMonster(35, CardId.MONSTER_146_1);
        MonsterCard targetCard = someMonster(37, CardId.MONSTER_146_2);
        sourceCard.setMove(namedMove("SharedMove"), 0);

        Move poolMove = sourceCard.peekMove(0);
        Move targetSlot = targetCard.peekMove(1);

        targetCard.setMove(poolMove, 1);

        assertEquals("SharedMove", targetCard.peekMove(1).name.toString());
        assertEquals(targetCard, targetSlot.getSourceCard());
        assertEquals(1, targetSlot.getSourceMoveIndex());
        assertEquals(poolMove, sourceCard.peekMove(0));
    }

    @Test
    void assignSpecifiedMovesMarksSlotsAsLocked() {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        MonsterCard card = someMonster(35, CardId.MONSTER_146_1);
        cards.add(card);

        MoveAssignments assignments = new MoveAssignments();
        assignments.addMoveAssignment(card, 1, namedMove("AssignedMove"), "test.yaml");
        assignments.assignSpecifiedMoves(cards);

        assertTrue(card.peekMove(1).isLockedViaAssignment());
        assertEquals(List.of(1), card.getLockedMoveIndexes());
    }

    @Test
    void setMoveFromPoolClearsLockedFlag() {
        MonsterCard card = someMonster(35, CardId.MONSTER_146_1);
        card.setMove(namedMove("AssignedMove"), 0);
        card.setMoveLockedViaAssignment(0, true);

        card.setMove(namedMove("RandomizedMove"), 0);

        assertFalse(card.peekMove(0).isLockedViaAssignment());
        assertTrue(card.getLockedMoveIndexes().isEmpty());
    }

    @Test
    void setNumMovesWarnsWhenClearingLockedSlotButStillAllowsChange() {
        MonsterCard card = someMonster(35, CardId.MONSTER_146_1);
        card.setMove(namedMove("MoveOne"), 0);
        card.setMove(namedMove("LockedMove"), 1);
        card.setMoveLockedViaAssignment(1, true);

        WarningCollector warnings = new WarningCollector(null);
        assertTrue(card.setNumMoves(1, warnings));
        assertEquals(1, card.getNumMoves());
        assertTrue(card.getLockedMoveIndexes().isEmpty());
        assertTrue(warnings.getWarnings().stream()
                .anyMatch(w -> w.contains("cleared locked assignment in slot 2")));
    }
}
