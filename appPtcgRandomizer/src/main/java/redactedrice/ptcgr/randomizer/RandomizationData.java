package redactedrice.ptcgr.randomizer;

import redactedrice.ptcgr.rom.Cards;
import redactedrice.ptcgr.rom.Decks;

/**
 * Data passed to lua for randomization in a single object for convinience
 */
public class RandomizationData {
    public final Cards cards;
    public final Decks decks;

    public RandomizationData(Cards cards, Decks decks) {
        this.cards = cards;
        this.decks = decks;
    }

    public RandomizationData copy() {
        return new RandomizationData(cards.copy(), decks.copy());
    }
}
