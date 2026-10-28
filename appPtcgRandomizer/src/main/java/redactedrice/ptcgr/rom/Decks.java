package redactedrice.ptcgr.rom;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import redactedrice.ptcgr.constants.romenums.DeckId;
import redactedrice.ptcgr.data.Deck;

public class Decks {
    private final Map<DeckId, Deck> decksById;

    public Decks() {
        decksById = new LinkedHashMap<>();
    }

    public Decks copy() {
        Decks copy = new Decks();
        for (Deck deck : decksById.values()) {
            copy.put(deck.copy());
        }
        return copy;
    }

    public void put(Deck deck) {
        decksById.put(deck.getDeckId(), deck);
    }

    public Deck getDeck(DeckId deckId) {
        return decksById.get(deckId);
    }

    public List<Deck> getDecks() {
        return new ArrayList<>(decksById.values());
    }
}
