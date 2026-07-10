package redactedrice.ptcgr.rules;

import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.rom.RomData;

/** Runtime move exclusion and assignment state for an opened ROM. */
public class Rules {
    private final CardGroup<MonsterCard> allCards;
    private final MoveExclusions moveExclusions;
    private final MoveAssignments moveAssignments;

    public Rules(RomData romData) {
        this(romData.original.allCards.cards().monsterCards());
    }

    public Rules(CardGroup<MonsterCard> allCards) {
        this.allCards = allCards;
        moveExclusions = new MoveExclusions();
        moveAssignments = new MoveAssignments();
    }

    public void clear() {
        moveExclusions.clear();
        moveAssignments.clear();
    }

    public MoveExclusions getMoveExclusions() {
        return moveExclusions;
    }

    public MoveAssignments getMoveAssignments() {
        return moveAssignments;
    }

    public CardGroup<MonsterCard> getAllCards() {
        return allCards;
    }
}
