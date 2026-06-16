package redactedrice.ptcgr.config;

import java.awt.Component;

import redactedrice.ptcgr.config.support.ConfigWarningCollector;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.rom.RomData;

public class Configs {
    private final MoveExclusions moveExclusions;
    private final MoveAssignments moveAssignments;
    private final RulesIO io;

    public Configs(RomData romData, Component toCenterPopupsOn) {
        this(romData.original.allCards.cards().monsterCards(), toCenterPopupsOn, true);
    }

    Configs(CardGroup<MonsterCard> allCards, Component toCenterPopupsOn,
            boolean loadUnsupportedMovesOnCreate) {
        ConfigWarningCollector warnings = new ConfigWarningCollector(toCenterPopupsOn);
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
