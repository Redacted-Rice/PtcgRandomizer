package redactedrice.ptcgr.rom;

import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.rules.Rules;
import redactedrice.ptcgr.utils.WarningCollector;

public class RomData {
    // These should never be modified
    public final byte[] rawBytes;
    public final Rules rules;
    public final RandomizationData original;
    // This will be modified as part of randomization and used to save
    public RandomizationData modified;

    public RomData(byte[] rawBytes, RandomizationData original) {
        this.rawBytes = rawBytes;
        this.original = original;
        this.rules = new Rules();
    }

    public CardGroup<MonsterCard> getOriginalMonsterCards() {
        return original.allCards.cards().monsterCards();
    }

    public void prepareForModification(WarningCollector warnings) {
        // Do a fresh read as this is easier and a better guarantee of isolation
        // than doing a deep copy
        modified = RomIO.readFromBytes(rawBytes);
        original.prepareForRandomization(rules, warnings);
        modified.prepareForRandomization(rules, warnings);
    }
}
