package redactedrice.ptcgr.rules;

import java.awt.Component;
import java.io.File;

import redactedrice.ptcgr.rules.support.RulesWarningCollector;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.rom.RomData;

public class Rules {
    private final MoveExclusions moveExclusions;
    private final MoveAssignments moveAssignments;
    private final RulesIO io;

    public Rules(RomData romData, Component toCenterPopupsOn, File defaultRulesFile) {
        this(romData.original.allCards.cards().monsterCards(), toCenterPopupsOn, defaultRulesFile);
    }

    Rules(CardGroup<MonsterCard> allCards, Component toCenterPopupsOn, File defaultRulesFile) {
        RulesWarningCollector warnings = new RulesWarningCollector(toCenterPopupsOn);
        moveExclusions = new MoveExclusions();
        moveAssignments = new MoveAssignments();
        io = new RulesIO(allCards, moveExclusions, moveAssignments, warnings);
        if (defaultRulesFile != null) {
            io.addRulesFile(defaultRulesFile, RulesLoadOptions.exclusionsOnly());
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
