package redactedrice.ptcgr.rom;

import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.rules.Rules;

public class RomData {
    // Kept in memory so original can be rebuilt when rules change without reopening the file
    public final byte[] rawBytes;
    public final Rules rules;
    // Ruled baseline for Lua "original"; rebuilt from rawBytes on ROM load / rules change
    public RandomizationData original;
    // Deep copy of original for each randomization pass
    public RandomizationData modified;

    public RomData(byte[] rawBytes, RandomizationData original) {
        this.rawBytes = rawBytes;
        this.original = original;
        this.rules = new Rules();
        this.original.bindRules(this.rules);
    }

    public CardGroup<MonsterCard> getOriginalMonsterCards() {
        return original.allCards.cards().monsterCards();
    }

    /**
     * Re parses card/text data from rawBytes into original with no rules applied yet. Call before
     * recreating the rules config so assignments resolve against ROM card data.
     */
    public void reloadOriginalFromRom() {
        original = RomIO.readFromBytes(rawBytes);
        original.bindRules(rules);
    }

    /**
     * Applies current rules (assignments/locks) to original. Call after the rules config has been
     * loaded into rules, on ROM open or rules change
     */
    public void applyRulesToOriginal() {
        rules.applyTo(original.allCards.cards().monsterCards());
    }

    /**
     * Regenerates modified as a deep copy of the original with rules already applied. Does not re
     * parse the ROM or re apply rules
     */
    public void prepareForModification() {
        modified = original.copy();
        modified.bindRules(rules);
    }
}
