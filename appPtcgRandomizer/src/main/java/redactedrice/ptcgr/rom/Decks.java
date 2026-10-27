package redactedrice.ptcgr.rom;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import redactedrice.ptcgr.constants.romenums.DeckValues;
import redactedrice.ptcgr.data.Deck;

public class Decks {
    private final Map<DeckValues, Deck> decksByValue;

    public Decks() {
        decksByValue = new LinkedHashMap<>();
    }

    public Decks copy() {
        Decks copy = new Decks();
        for (Deck deck : decksByValue.values()) {
            copy.put(deck.copy());
        }
        return copy;
    }

    public void put(Deck deck) {
        decksByValue.put(deck.getDeckValue(), deck);
    }

    public Deck getDeck(DeckValues deckValue) {
        return decksByValue.get(deckValue);
    }

    public List<Deck> getDecks() {
        return new ArrayList<>(decksByValue.values());
    }
}
