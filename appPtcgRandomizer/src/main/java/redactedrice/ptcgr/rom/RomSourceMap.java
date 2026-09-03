package redactedrice.ptcgr.rom;


import java.util.HashMap;
import java.util.Map;

import redactedrice.gbcframework.addressing.AddressRange;
import redactedrice.ptcgr.constants.romenums.CardId;

public class RomSourceMap {
    private AddressRange cardPointerTable;
    private AddressRange textPointerTable;
    private final Map<Short, AddressRange> textRanges;
    private final Map<CardId, AddressRange> cardRanges;

    public RomSourceMap() {
        textRanges = new HashMap<>();
        cardRanges = new HashMap<>();
    }

    public RomSourceMap copy() {
        RomSourceMap copy = new RomSourceMap();
        copy.cardPointerTable =
                cardPointerTable == null ? null : new AddressRange(cardPointerTable);
        copy.textPointerTable =
                textPointerTable == null ? null : new AddressRange(textPointerTable);
        for (Map.Entry<Short, AddressRange> entry : textRanges.entrySet()) {
            AddressRange range = entry.getValue();
            copy.textRanges.put(entry.getKey(), range == null ? null : new AddressRange(range));
        }
        for (Map.Entry<CardId, AddressRange> entry : cardRanges.entrySet()) {
            AddressRange range = entry.getValue();
            copy.cardRanges.put(entry.getKey(), range == null ? null : new AddressRange(range));
        }
        return copy;
    }

    public void setCardPointerTable(AddressRange range) {
        cardPointerTable = range;
    }

    public AddressRange getCardPointerTable() {
        return cardPointerTable;
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

    public void recordCardRange(CardId cardId, AddressRange range) {
        cardRanges.put(cardId, range);
    }

    public AddressRange getCardRange(CardId cardId) {
        return cardRanges.get(cardId);
    }
}
