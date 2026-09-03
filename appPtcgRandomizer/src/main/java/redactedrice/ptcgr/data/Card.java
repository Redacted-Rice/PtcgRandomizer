package redactedrice.ptcgr.data;



import redactedrice.gbcframework.utils.ByteUtils;
import redactedrice.ptcgr.constants.romenums.BoosterPack;
import redactedrice.ptcgr.constants.romenums.CardId;
import redactedrice.ptcgr.constants.romenums.CardRarity;
import redactedrice.ptcgr.constants.romenums.CardSet;
import redactedrice.ptcgr.constants.romenums.CardType;
import redactedrice.ptcgr.data.romtexts.CardName;
import redactedrice.ptcgr.data.support.CardIdSorter;
import redactedrice.ptcgr.data.support.CardRomSorter;
import redactedrice.ptcgr.rom.Texts;

import java.security.InvalidParameterException;
import java.util.Comparator;
import redactedrice.compiler.CodeBlock;
import redactedrice.compiler.RawBytePacker;

public abstract class Card {
    public static final int CARD_COMMON_SIZE = 8;
    public static final Comparator<Card> ID_SORTER = new CardIdSorter();
    public static final Comparator<Card> ROM_SORTER = new CardRomSorter();

    public CardType type;
    public CardName name;
    public short gfx; // Card art
    public CardRarity rarity;

    // IMPORTANT! in the data the set and pack are stored in one byte:
    // bits 0-3 are the set, bits 4-7 are the booster pack they can be found in
    public CardSet set;
    public BoosterPack pack;
    public CardId id;

    public Card() {
        name = createCardName();
    }

    protected abstract CardName createCardName();

    public abstract Card copy();

    // Common copying used by subclasses
    protected void copyCardFields(Card toCopy) {
        type = toCopy.type;
        name = new CardName(toCopy.name);
        gfx = toCopy.gfx;
        rarity = toCopy.rarity;
        set = toCopy.set;
        pack = toCopy.pack;
        id = toCopy.id;
    }

    public static Card addCardFromBytes(byte[] cardBytes, int startIndex, Texts idToText,
            CardGroup<Card> toAddTo) {
        CardType type = CardType.readFromByte(cardBytes[startIndex]);

        Card card;
        if (type.isMonsterCard()) {
            card = new MonsterCard();
        } else if (type.isEnergyCard()) {
            card = new NonMonsterCard();
        } else if (type.isTrainerCard()) {
            card = new NonMonsterCard();
        } else {
            throw new InvalidParameterException("Failed to determine type of card at index "
                    + startIndex + " that is of type " + type);
        }

        card.readAndConvertIds(cardBytes, startIndex, idToText);
        toAddTo.add(card);
        return card;
    }

    public abstract int readAndConvertIds(byte[] cardBytes, int startIndex, Texts idsToText);

    public abstract void finalizeAndAddTexts(Texts texts);

    public abstract CodeBlock convertToCodeBlock();

    public abstract int getSize();

    /** Numeric card id for Lua change-detector sorting and display. */
    public int getIdValue() {
        return id.getValue() & 0xFF;
    }

    public String toString() {
        return "Name = " + name.toString() + "\nID = " + id + "\nType = " + type + "\nRarity = "
                + rarity + "\nSet = " + set + "\nPack = " + pack;
    }

    protected int commonReadAndConvertIds(byte[] cardBytes, int startIndex, Texts idsToText) {
        int index = startIndex;

        type = CardType.readFromByte(cardBytes[index++]);
        gfx = ByteUtils.readAsShort(cardBytes, index);
        index += 2;

        index = name.readDataAndConvertIds(cardBytes, index, idsToText);

        rarity = CardRarity.readFromByte(cardBytes[index++]);

        pack = BoosterPack.readFromHexChar(ByteUtils.readUpperHexChar(cardBytes[index])); // no ++ -
                                                                                          // this
                                                                                          // reads
                                                                                          // only
                                                                                          // half
                                                                                          // the
                                                                                          // byte
        set = CardSet.readFromHexChar(ByteUtils.readLowerHexChar(cardBytes[index++]));

        id = CardId.readFromByte(cardBytes[index++]);

        return index;
    }

    protected void commonFinalizeAndAddTexts(Texts texts) {
        name.finalizeAndAddTexts(texts);
    }

    protected CodeBlock convertCommonDataToCodeBlock() {
        RawBytePacker bytes = new RawBytePacker();
        bytes.append(type.getValue());
        bytes.append(ByteUtils.shortToLittleEndianBytes(gfx));
        bytes.append(ByteUtils.shortToLittleEndianBytes(name.getTextId()));
        bytes.append(rarity.getValue(),
                ByteUtils.packHexCharsToByte(pack.getValue(), set.getValue()), id.getValue());

        CodeBlock block = new CodeBlock("internal_card_" + name.toString() + "_"
                + ByteUtils.unsignedByteAsShort(id.getValue()));
        block.appendInstruction(bytes.createRawByteInsruct());
        return block;
    }
}
