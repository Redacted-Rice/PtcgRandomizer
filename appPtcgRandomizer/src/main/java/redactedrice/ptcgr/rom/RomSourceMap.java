package redactedrice.ptcgr.rom;

import java.util.HashMap;
import java.util.Map;

import redactedrice.gbcframework.addressing.AddressRange;
import redactedrice.ptcgr.constants.romenums.CardId;
import redactedrice.ptcgr.constants.romenums.DeckValues;

public class RomSourceMap {
    private AddressRange cardPointerTable;
    private AddressRange deckPointerTable;
    private AddressRange textPointerTable;
    private final Map<Short, AddressRange> textRanges;
    private final Map<CardId, AddressRange> cardRanges;
    private final Map<DeckValues, AddressRange> deckRanges;

    public RomSourceMap() {
        textRanges = new HashMap<>();
        cardRanges = new HashMap<>();
        deckRanges = new HashMap<>();
    }

    public RomSourceMap copy() {
        RomSourceMap copy = new RomSourceMap();
        copy.deckPointerTable = deckPointerTable == null ? null : new AddressRange(deckPointerTable);
        copy.cardPointerTable = cardPointerTable == null ? null : new AddressRange(cardPointerTable);
        copy.textPointerTable = textPointerTable == null ? null : new AddressRange(textPointerTable);
        for (Map.Entry<DeckValues, AddressRange> entry : deckRanges.entrySet()) {
            AddressRange range = entry.getValue();
            copy.deckRanges.put(entry.getKey(), range == null ? null : new AddressRange(range));
        }
        for (Map.Entry<CardId, AddressRange> entry : cardRanges.entrySet()) {
            AddressRange range = entry.getValue();
            copy.cardRanges.put(entry.getKey(), range == null ? null : new AddressRange(range));
        }
        for (Map.Entry<Short, AddressRange> entry : textRanges.entrySet()) {
            AddressRange range = entry.getValue();
            copy.textRanges.put(entry.getKey(), range == null ? null : new AddressRange(range));
        }
        return copy;
    }

    public void setDeckPointerTable(AddressRange range) {
        deckPointerTable = range;
    }

    public AddressRange getDeckPointerTable() {
        return deckPointerTable;
    }

    public void recordDeckRange(DeckValues deckValue, AddressRange range) {
        deckRanges.put(deckValue, range);
    }

    public AddressRange getDeckRange(DeckValues deckValue) {
        return deckRanges.get(deckValue);
    }

    public void setCardPointerTable(AddressRange range) {
        cardPointerTable = range;
    }

    public AddressRange getCardPointerTable() {
        return cardPointerTable;
    }

    public void recordCardRange(CardId cardId, AddressRange range) {
        cardRanges.put(cardId, range);
    }

    public AddressRange getCardRange(CardId cardId) {
        return cardRanges.get(cardId);
    }

    public void setTextPointerTable(AddressRange range) {
        textPointerTable = range;
    }

    public AddressRange getTextPointerTable() {
        return textPointerTable;
    }

    public void recordTextRange(short textId, AddressRange range) {
        textRanges.put(textId, range);
    }

    public AddressRange getTextRange(short textId) {
        return textRanges.get(textId);
    }
}
