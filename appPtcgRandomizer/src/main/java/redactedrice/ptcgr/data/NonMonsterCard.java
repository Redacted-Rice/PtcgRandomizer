package redactedrice.ptcgr.data;


import redactedrice.compiler.CodeBlock;
import redactedrice.compiler.instructions.basic.RawBytes;
import redactedrice.gbcframework.utils.ByteUtils;
import redactedrice.ptcgr.constants.PtcgRomConstants;
import redactedrice.ptcgr.data.romtexts.CardName;
import redactedrice.ptcgr.data.romtexts.EffectDescription;
import redactedrice.ptcgr.rom.Texts;

public class NonMonsterCard extends Card {
    public static final int TOTAL_SIZE_IN_BYTES = 14;
    public static final int SIZE_OF_PAYLOAD_IN_BYTES = TOTAL_SIZE_IN_BYTES - CARD_COMMON_SIZE;

    private short effectPtr;
    private EffectDescription description;

    public NonMonsterCard() {
        super();

        description = new EffectDescription();
    }

    public NonMonsterCard copy() {
        NonMonsterCard copy = new NonMonsterCard();
        copy.copyNonMonsterCardFields(this);
        return copy;
    }

    protected void copyNonMonsterCardFields(NonMonsterCard toCopy) {
        copyCardFields(toCopy);

        effectPtr = toCopy.effectPtr;
        description = new EffectDescription(toCopy.description);
    }

    @Override
    protected CardName createCardName() {
        return new CardName(false); // not a poke name
    }

    @Override
    public int readAndConvertIds(byte[] cardBytes, int startIndex, Texts idToText) {
        commonReadAndConvertIds(cardBytes, startIndex, idToText);
        int index = startIndex + Card.CARD_COMMON_SIZE;

        // reading non monster specific data
        effectPtr = ByteUtils.readAsShort(cardBytes, index);
        index += 2;

        int[] descIndexes = {index, index + PtcgRomConstants.TEXT_ID_SIZE_IN_BYTES};
        description.readDataAndConvertIds(cardBytes, descIndexes, name, idToText);
        return TOTAL_SIZE_IN_BYTES;
    }

    @Override
    public void finalizeAndAddTexts(Texts texts) {
        commonFinalizeAndAddTexts(texts);

        description.finalizeAndAddTexts(texts, name.toString());
    }

    @Override
    public CodeBlock convertToCodeBlock() {
        CodeBlock block = convertCommonDataToCodeBlock();

        block.appendInstruction(new RawBytes(ByteUtils.shortToLittleEndianBytes(effectPtr),
                ByteUtils.shortListToLittleEndianBytes(
                        description.getTextIds(PtcgRomConstants.MAX_BLOCKS_EFFECT_DESC))));

        return block;
    }

    @Override
    public int getSize() {
        return TOTAL_SIZE_IN_BYTES;
    }

    public short getEffectPtr() {
        return effectPtr;
    }

    public EffectDescription getDescription() {
        return description;
    }
}
