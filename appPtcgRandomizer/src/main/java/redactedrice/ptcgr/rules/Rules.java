package redactedrice.ptcgr.rules;

import java.awt.Component;

import redactedrice.ptcgr.rules.support.RulesWarningCollector;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.rom.RomData;

public class Rules {
    private final MoveExclusions moveExclusions;
    private final MoveAssignments moveAssignments;
    private final RulesIO io;

    public Rules(RomData romData, Component toCenterPopupsOn) {
        this(romData.original.allCards.cards().monsterCards(), toCenterPopupsOn, true);
    }

    Rules(CardGroup<MonsterCard> allCards, Component toCenterPopupsOn,
            boolean loadUnsupportedMovesOnCreate) {
        RulesWarningCollector warnings = new RulesWarningCollector(toCenterPopupsOn);
        moveExclusions = new MoveExclusions();
        moveAssignments = new MoveAssignments();
        io = new RulesIO(allCards, moveExclusions, moveAssignments, warnings);
        if (loadUnsupportedMovesOnCreate) {
            io.addUnsupportedMoves();
        }
    }

    public RulesIO getIo() {
        return io;
    }

    public MoveExclusions getMoveExclusions() {
        return moveExclusions;
    }

    public MoveAssignments getMoveAssignments() {
        return moveAssignments;
    }
}
