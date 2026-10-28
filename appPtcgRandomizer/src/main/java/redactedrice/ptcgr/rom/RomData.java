package redactedrice.ptcgr.rom;

import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.randomizer.RandomizationData;
import redactedrice.ptcgr.rules.Rules;
import redactedrice.rompacker.Blocks;

public class RomData {
    /** ROM file bytes at read time. Not modified by randomization. */
    public final byte[] baselineRawBytes;
    /** Parsed cards from the ROM. Not modified by rules or randomization. */
    public final Cards baselineCards;
    /** Parsed decks from the ROM. Not modified by rules or randomization. */
    public final Decks baselineDecks;
    /**
     * Text id registry from the ROM. Not mutated by randomization or patch writes.
     */
    public final Texts baselineTexts;
    /** Original ROM addresses for cards and texts. Immutable after read. */
    public final RomSourceMap baselineSourceMap;
    /** Blanked ROM ranges from read. Shared template for patch writes. */
    public final Blocks baselineRomBlanks;
    public final Rules baselineRules;
    /**
     * Data exposed to lua for randomization. Created only during a randomization
     * pass.
     */
    public RandomizationData original;
    public RandomizationData modified;

    public RomData(byte[] baselineRawBytes, Cards baselineCards, Decks baselineDecks,
            Texts baselineTexts, RomSourceMap baselineSourceMap, Blocks baselineRomBlanks,
            Rules baselineRules) {
        this.baselineRawBytes = baselineRawBytes;
        this.baselineCards = baselineCards;
        this.baselineDecks = baselineDecks;
        this.baselineTexts = baselineTexts;
        this.baselineSourceMap = baselineSourceMap;
        this.baselineRomBlanks = baselineRomBlanks;
        this.baselineRules = baselineRules;
        this.baselineCards.bindRules(this.baselineRules);
    }

    public CardGroup<MonsterCard> getReferenceMonsterCards() {
        return baselineCards.cards().monsterCards();
    }

    /**
     * Builds ruled original and modified from fromRom
     * for one randomization pass.
     */
    public void prepareForModification() {
        Cards cards = baselineCards.copy();
        cards.bindRules(baselineRules);
        baselineRules.applyTo(cards.cards().monsterCards());
        Decks decks = baselineDecks.copy();
        original = new RandomizationData(cards, decks);
        modified = original.copy();
        modified.cards.bindRules(baselineRules);
    }

    public void discardModificationWorkspace() {
        original = null;
        modified = null;
    }
}
