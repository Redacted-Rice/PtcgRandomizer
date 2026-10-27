package redactedrice.ptcgr.rom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import redactedrice.gbcframework.addressing.AddressRange;
import redactedrice.ptcgr.constants.PtcgRomConstants;
import redactedrice.ptcgr.constants.romenums.CardId;
import redactedrice.ptcgr.constants.romenums.DeckValues;
import redactedrice.ptcgr.data.Deck;
import redactedrice.rompacker.Blocks;

class RomReaderDeckTableTest {

    @Test
    void skipsUnusedDeckPointerRowsAndParsesTutorialDeck() {
        int tableLoc = PtcgRomConstants.DECK_POINTERS_LOC;
        int unusedBlob = tableLoc + 0x10;
        int tutorialBlob = unusedBlob + 4;
        byte[] rom = new byte[tutorialBlob + 8];
        writeDeckPointer(rom, tableLoc, unusedBlob);
        writeDeckPointer(rom, tableLoc + 2, unusedBlob + 2);
        writeDeckPointer(rom, tableLoc + 4, tutorialBlob);
        rom[tableLoc + 6] = 0;
        rom[tableLoc + 7] = 0;

        rom[unusedBlob] = 1;
        rom[unusedBlob + 1] = CardId.ENERGY_GRASS.getValue();
        rom[unusedBlob + 2] = 0;

        rom[tutorialBlob] = 2;
        rom[tutorialBlob + 1] = CardId.ENERGY_FIRE.getValue();
        rom[tutorialBlob + 2] = 0;
        rom[tutorialBlob + 3] = 0;
        rom[tutorialBlob + 4] = 0;

        RomSourceMap sourceMap = new RomSourceMap();
        Decks decks = RomReader.readDecksFromData(rom, new Texts(), new Blocks(), sourceMap);

        AddressRange deckPtrTable = sourceMap.getDeckPointerTable();
        assertEquals(tableLoc + 8, deckPtrTable.getStopExclusive());

        assertTrue(decks.getDeck(DeckValues.UNUSED_DECK_1).isPlaceholder());
        assertTrue(decks.getDeck(DeckValues.UNUSED_DECK_2).isPlaceholder());

        Deck deck = decks.getDeck(DeckValues.TUTORIAL_DECK);
        assertNotNull(deck);
        assertEquals(2, deck.getCountOfCard(CardId.ENERGY_FIRE));
    }

    private static void writeDeckPointer(byte[] rom, int tableIndex, int blobStart) {
        int rawPtr = blobStart - PtcgRomConstants.DECK_POINTER_OFFSET;
        rom[tableIndex] = (byte) (rawPtr & 0xFF);
        rom[tableIndex + 1] = (byte) ((rawPtr >> 8) & 0xFF);
    }
}
