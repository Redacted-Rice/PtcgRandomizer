package redactedrice.ptcgr.rom;

import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.rules.Rules;

public class RomData {
    public final byte[] rawBytes;
    /**
     * Parsed ROM card/text data and never modified by rules. Used for validation
     * and as a copy source.
     */
    public final RandomizationData fromRom;
    public final Rules rules;
    /** Ruled baseline for Lua. Created only during a randomization pass. */
    public RandomizationData original;
    /** Working copy for Lua. Created only during a randomization pass. */
    public RandomizationData modified;

    public RomData(byte[] rawBytes, RandomizationData fromRom, Rules rules) {
        this.rawBytes = rawBytes;
        this.fromRom = fromRom;
        this.rules = rules;
        this.fromRom.bindRules(this.rules);
    }

    public CardGroup<MonsterCard> getReferenceMonsterCards() {
        return fromRom.allCards.cards().monsterCards();
    }

    /**
     * Builds ruled original and modified from fromRom
     * for one randomization pass.
     */
    public void prepareForModification() {
        original = fromRom.copy();
        original.bindRules(rules);
        rules.applyTo(original.allCards.cards().monsterCards());
        modified = original.copy();
        modified.bindRules(rules);
    }

    public void discardModificationWorkspace() {
        original = null;
        modified = null;
    }
}
