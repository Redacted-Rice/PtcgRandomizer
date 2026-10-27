package redactedrice.ptcgr.rom;

import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.rules.Rules;
import redactedrice.rompacker.Blocks;

public class RomData {
    public final byte[] rawBytes;
    /** Parsed cards from the ROM. Not modified by rules or randomization. */
    public final Cards fromRom;
    /** Parsed decks from the ROM. Not modified by rules or randomization. */
    public final Decks fromRomDecks;
    /**
     * Text id registry from the ROM. Not mutated by randomization or patch writes.
     */
    public final Texts texts;
    /** Original ROM addresses for cards and texts. Immutable after read. */
    public final RomSourceMap sourceMap;
    /** Blanked ROM ranges from read. Shared template for patch writes. */
    public final Blocks romBlanks;
    public final Rules rules;
    /** Ruled baseline for Lua. Created only during a randomization pass. */
    public Cards original;
    /** Working copy for Lua. Created only during a randomization pass. */
    public Cards modified;
    /** Ruled baseline decks for Lua. Created only during a randomization pass. */
    public Decks originalDecks;
    /** Working deck copy for Lua. Created only during a randomization pass. */
    public Decks modifiedDecks;

    public RomData(byte[] rawBytes, Cards fromRom, Decks fromRomDecks, Texts texts,
            RomSourceMap sourceMap, Blocks romBlanks, Rules rules) {
        this.rawBytes = rawBytes;
        this.fromRom = fromRom;
        this.fromRomDecks = fromRomDecks;
        this.texts = texts;
        this.sourceMap = sourceMap;
        this.romBlanks = romBlanks;
        this.rules = rules;
        this.fromRom.bindRules(this.rules);
    }

    public CardGroup<MonsterCard> getReferenceMonsterCards() {
        return fromRom.cards().monsterCards();
    }

    /**
     * Builds ruled original and modified from fromRom
     * for one randomization pass.
     */
    public void prepareForModification() {
        original = fromRom.copy();
        original.bindRules(rules);
        rules.applyTo(original.cards().monsterCards());
        modified = original.copy();
        modified.bindRules(rules);
        originalDecks = fromRomDecks.copy();
        modifiedDecks = originalDecks.copy();
    }

    public void discardModificationWorkspace() {
        original = null;
        modified = null;
        originalDecks = null;
        modifiedDecks = null;
    }
}
