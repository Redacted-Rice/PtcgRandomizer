package redactedrice.ptcgr.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import redactedrice.ptcgr.constants.romenums.CardId;
import redactedrice.ptcgr.constants.romenums.DeckValues;
import redactedrice.ptcgr.rom.Decks;
import redactedrice.ptcgr.rom.Texts;

class DeckTest {

    @Test
    void readMergesDuplicateCardIdsIntoMap() {
        byte[] romBlock = {
                2, CardId.ENERGY_FIRE.getValue(),
                3, CardId.ENERGY_FIRE.getValue(),
                0,
                0, 0,
        };

        Decks decks = new Decks();
        Deck.addDeckFromBytes(romBlock, 0, DeckValues.FIRE_STARTER_DECK, new Texts(), decks);
        Deck deck = decks.getDeck(DeckValues.FIRE_STARTER_DECK);
        assertEquals(5, deck.getCountOfCard(CardId.ENERGY_FIRE));
    }

    @Test
    void roundTripsListAndName() {
        Texts texts = new Texts();
        short nameId = texts.insertTextOrGetId("Test Deck");

        byte[] romBlock = {
                24, CardId.ENERGY_FIRE.getValue(),
                4, CardId.MONSTER_004.getValue(),
                0,
                (byte) (nameId & 0xFF), (byte) ((nameId >> 8) & 0xFF),
        };

        Decks decks = new Decks();
        Deck.addDeckFromBytes(romBlock, 0, DeckValues.FIRE_STARTER_DECK, texts, decks);
        Deck deck = decks.getDeck(DeckValues.FIRE_STARTER_DECK);
        assertEquals(24, deck.getCountOfCard(CardId.ENERGY_FIRE));
        assertEquals(4, deck.getCountOfCard(CardId.MONSTER_004));
        assertEquals("Test Deck", deck.name.toString());
        assertArrayEquals(romBlock, deck.toRomBytes(new CardGroup<>()));
    }

    @Test
    void setCountOfCardUpdatesMap() {
        Deck deck = new Deck(DeckValues.WATER_1);
        deck.setCountOfCard(CardId.ENERGY_WATER, 2);
        deck.setCountOfCard(CardId.MONSTER_007, 2);

        deck.setCountOfCard(CardId.ENERGY_WATER, 3);
        assertEquals(5, deck.getTotalCardCount());
        assertEquals(3, deck.getCountOfCard(CardId.ENERGY_WATER));
        deck.setCountOfCard(CardId.MONSTER_007, 0);
        assertEquals(3, deck.getTotalCardCount());
    }
}
