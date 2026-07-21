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
        Move move = scratch.getMove(0);
        move.name.setText(moveName);
        scratch.setMove(move, 0);
        return scratch.getMove(0);
    }

    @Test
    void setMoveCopiesDataWithoutChangingSlotBinding() {
        MonsterCard sourceCard = someMonster(35, CardId.MONSTER_146_1);
        MonsterCard targetCard = someMonster(37, CardId.MONSTER_146_2);
        sourceCard.setMove(namedMove("SharedMove"), 0);

        Move poolMove = sourceCard.getMove(0);
        targetCard.setMove(poolMove, 1);

        assertEquals("SharedMove", targetCard.getMove(1).name.toString());
        assertEquals(targetCard, targetCard.getMove(1).getSourceCard());
        assertEquals(1, targetCard.getMove(1).getSourceMoveIndex());
        assertEquals("SharedMove", sourceCard.getMove(0).name.toString());
    }

    @Test
    void assignSpecifiedMovesMarksSlotsAsLocked() {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        MonsterCard card = someMonster(35, CardId.MONSTER_146_1);
        cards.add(card);

        MoveAssignments assignments = new MoveAssignments();
        assignments.addMoveAssignment(card, 1, namedMove("AssignedMove"), "test.yaml");
        assignments.assignSpecifiedMoves(cards, null);

        assertTrue(card.getMove(1).isLockedViaAssignment());
        assertEquals(List.of(1), card.getLockedMoveIndexes());
    }

    @Test
    void setMoveRefusesToOverrideLockedSlotWithoutForce() {
        MonsterCard card = someMonster(35, CardId.MONSTER_146_1);
        card.setMove(namedMove("AssignedMove"), 0);
        card.setMoveLockedViaAssignment(0, true);

        WarningCollector warnings = new WarningCollector(null);
        assertFalse(card.setMove(namedMove("RandomizedMove"), 0, false, warnings));
        assertTrue(card.getMove(0).isLockedViaAssignment());
        assertEquals("AssignedMove", card.getMove(0).name.toString());
        assertTrue(warnings.getWarnings().stream()
                .anyMatch(w -> w.contains("Refusing to overwrite locked assignment")));
    }

    @Test
    void setMoveWithForceOverrideKeepsLockedFlag() {
        MonsterCard card = someMonster(35, CardId.MONSTER_146_1);
        card.setMove(namedMove("AssignedMove"), 0);
        card.setMoveLockedViaAssignment(0, true);

        WarningCollector warnings = new WarningCollector(null);
        assertTrue(card.setMove(namedMove("RandomizedMove"), 0, true, warnings));
        assertTrue(card.getMove(0).isLockedViaAssignment());
        assertEquals("RandomizedMove", card.getMove(0).name.toString());
        assertEquals(List.of(0), card.getLockedMoveIndexes());
        assertTrue(warnings.getWarnings().isEmpty());
    }

    @Test
    void setNumMovesRefusesToClearLockedSlotWithoutForce() {
        MonsterCard card = someMonster(35, CardId.MONSTER_146_1);
        card.setMove(namedMove("MoveOne"), 0);
        card.setMove(namedMove("LockedMove"), 1);
        card.setMoveLockedViaAssignment(1, true);

        WarningCollector warnings = new WarningCollector(null);
        assertFalse(card.setNumMoves(1, false, warnings));
        assertEquals(2, card.getNumMoves());
        assertTrue(card.getMove(1).isLockedViaAssignment());
        assertEquals("LockedMove", card.getMove(1).name.toString());
        assertTrue(warnings.getWarnings().stream()
                .anyMatch(w -> w.contains("Refusing to reduce move count")));
    }

    @Test
    void getMaxLockedMoveIndexUsesHighestLockedSlot() {
        MonsterCard card = someMonster(35, CardId.MONSTER_146_1);
        assertEquals(-1, card.getMaxLockedMoveIndex());

        card.setMove(namedMove("MoveOne"), 0);
        card.setMoveLockedViaAssignment(0, true);
        assertEquals(0, card.getMaxLockedMoveIndex());

        card.setMove(namedMove("LockedMove"), 1);
        card.setMoveLockedViaAssignment(1, true);
        assertEquals(1, card.getMaxLockedMoveIndex());
    }

    @Test
    void setMovesRefusesToClearLockedSlotWithoutForce() {
        MonsterCard card = someMonster(35, CardId.MONSTER_146_1);
        card.setMove(namedMove("MoveOne"), 0);
        card.setMove(namedMove("LockedMove"), 1);
        card.setMoveLockedViaAssignment(1, true);

        WarningCollector warnings = new WarningCollector(null);
        assertEquals(List.of(0), card.setMoves(List.of(card.getMove(0)), false, warnings));
        assertEquals(2, card.getNumMoves());
        assertTrue(card.getMove(1).isLockedViaAssignment());
        assertTrue(warnings.getWarnings().stream()
                .anyMatch(w -> w.contains("Refusing to overwrite locked assignment")));
    }

    @Test
    void setNumMovesWithForceOverrideClearsLockedSlot() {
        MonsterCard card = someMonster(35, CardId.MONSTER_146_1);
        card.setMove(namedMove("MoveOne"), 0);
        card.setMove(namedMove("LockedMove"), 1);
        card.setMoveLockedViaAssignment(1, true);

        WarningCollector warnings = new WarningCollector(null);
        assertTrue(card.setNumMoves(1, true, warnings));
        assertEquals(1, card.getNumMoves());
        assertTrue(card.getLockedMoveIndexes().isEmpty());
        assertTrue(card.getMove(1).isEmpty());
        assertTrue(warnings.getWarnings().isEmpty());
    }
}
