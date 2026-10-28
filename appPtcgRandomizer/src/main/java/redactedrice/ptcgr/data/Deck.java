package redactedrice.ptcgr.data;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import redactedrice.compiler.CodeBlock;
import redactedrice.compiler.instructions.basic.RawBytes;
import redactedrice.gbcframework.utils.ByteUtils;
import redactedrice.ptcgr.constants.PtcgRomConstants;
import redactedrice.ptcgr.constants.romenums.CardId;
import redactedrice.ptcgr.constants.romenums.DeckId;
import redactedrice.ptcgr.data.romtexts.DeckName;
import redactedrice.ptcgr.rom.Decks;
import redactedrice.ptcgr.rom.Texts;

public class Deck {
    private final DeckId deckId;
    private final EnumMap<CardId, Integer> cardCounts;
    public DeckName name;

    public Deck(DeckId deckId) {
        this.deckId = deckId;
        this.cardCounts = new EnumMap<>(CardId.class);
        this.name = new DeckName();
    }

    public Deck copy() {
        Deck copy = new Deck(deckId);
        copy.cardCounts.putAll(cardCounts);
        copy.name = new DeckName(name);
        return copy;
    }

    public DeckId getDeckId() {
        return deckId;
    }

    public Map<CardId, Integer> getCardCounts() {
        return cardCounts;
    }

    // Should always be 60 for valid decks but some decks seem to be larger
    public int getTotalCardCount() {
        int total = 0;
        for (int count : cardCounts.values()) {
            total += count;
        }
        return total;
    }

    public int getCountOfCard(CardId cardId) {
        return cardCounts.getOrDefault(cardId, 0);
    }

    public void setCountOfCard(CardId cardId, int count) {
        if (count < 0 || count > 255) {
            throw new IllegalArgumentException("Deck entry count out of range: " + count);
        }
        if (count == 0) {
            cardCounts.remove(cardId);
        } else {
            cardCounts.put(cardId, count);
        }
    }

    public boolean isPlaceholder() {
        return cardCounts.isEmpty() && name.isEmpty();
    }

    public void finalizeAndAddTexts(Texts texts) {
        name.finalizeAndAddTexts(texts);
    }

    public static void addPlaceholderDeck(DeckId deckId, Decks toAddTo) {
        toAddTo.put(new Deck(deckId));
    }

    public static int addDeckFromBytes(byte[] rawBytes, int start, DeckId deckId,
            Texts texts, Decks toAddTo) {
        Deck deck = new Deck(deckId);
        int index = start;
        while (index < rawBytes.length) {
            int count = rawBytes[index] & 0xFF;
            // Check if we reached the ending null of the card list and break when we do
            if (count == 0) {
                index++;
                break;
            }
            // Otherwise read the card and add it
            CardId cardId = CardId.readFromByte(rawBytes[index + 1]);
            deck.cardCounts.merge(cardId, count, Integer::sum);
            index += 2;
        }

        // Read the deck name
        deck.name.readDataAndConvertIds(rawBytes, index, texts);
        index += PtcgRomConstants.TEXT_ID_SIZE_IN_BYTES;

        toAddTo.put(deck);
        return index - start;
    }

    public CodeBlock convertToCodeBlock(CardGroup<Card> cards) {
        CodeBlock block = new CodeBlock("internal_deck_" + deckId.name());
        block.appendInstruction(new RawBytes(toRomBytes(cards)));
        return block;
    }

    public byte[] toRomBytes(CardGroup<Card> cards) {
        List<CardId> ordered = new ArrayList<>(cardCounts.keySet());
        ordered.sort(romWriteOrder(cards));

        byte[] bytes = new byte[ordered.size() * 2 + 1 + PtcgRomConstants.TEXT_ID_SIZE_IN_BYTES];
        int index = 0;
        for (CardId cardId : ordered) {
            bytes[index++] = (byte) (int) cardCounts.get(cardId);
            bytes[index++] = cardId.getValue();
        }
        bytes[index++] = 0;
        name.writeTextId(bytes, index);
        return bytes;
    }

    private static java.util.Comparator<CardId> romWriteOrder(CardGroup<Card> cards) {
        return (id1, id2) -> {
            Card c1 = cards.withId(id1);
            Card c2 = cards.withId(id2);

            // Handle null just in case
            if (c1 == null) {
                if (c2 == null) {
                    return 0;
                }
                return 1;
            }
            if (c2 == null) {
                return -1;
            }

            // Energies come first sorted by card id
            if (c1.type.isEnergyCard()) {
                if (c2.type.isEnergyCard()) {
                    return ByteUtils.unsignedCompareBytes(id1.getValue(), id2.getValue());
                }
                return -1;
            }
            if (c2.type.isEnergyCard()) {
                return 1;
            }

            // Then monsters sorted by dex number
            if (c1.type.isMonsterCard()) {
                if (c2.type.isMonsterCard()) {
                    MonsterCard m1 = (MonsterCard) c1;
                    MonsterCard m2 = (MonsterCard) c2;
                    return ByteUtils.unsignedCompareBytes(m1.dexNumber, m2.dexNumber);
                }
                return -1;
            }
            if (c2.type.isMonsterCard()) {
                return 1;
            }

            // If its not a monster or energy, then it must be a trianer and we just compare
            // by card id
            return ByteUtils.unsignedCompareBytes(id1.getValue(), id2.getValue());
        };
    }
}
