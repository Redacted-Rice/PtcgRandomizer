package redactedrice.ptcgr.rom;


import redactedrice.gbcframework.addressing.AddressRange;
import redactedrice.gbcframework.utils.ByteUtils;
import redactedrice.ptcgr.constants.CharMapConstants;
import redactedrice.ptcgr.constants.PtcgRomConstants;
import redactedrice.ptcgr.constants.romenums.CharSetPrefix;
import redactedrice.ptcgr.data.Card;
import redactedrice.rompacker.Blocks;

public final class RomReader {
    private RomReader() {}

    // Note assumes that the first text in the pointer list is the first in the file as well. This
    // is required since there is no null between the text pointer map and the texts themselves
    public static Texts readTextsFromData(byte[] rawBytes, Blocks toBlankSpaceIn,
            RomSourceMap sourceMap) {
        // TODO: Optimize address range adding since they will mostly be in order

        Texts texts = new Texts();
        // Intentionally not clearing addressesReadToAddTo to support chaining calls/adding
        // to it via other functions

        // Read the text based on the pointer map in the rom
        // First pointer is a null pointer so we skip it
        int ptrIndex =
                PtcgRomConstants.TEXT_POINTERS_LOC + PtcgRomConstants.TEXT_POINTER_SIZE_IN_BYTES;
        int ptr = 0;
        int textIndex = 0;
        int firstPtr = Integer.MAX_VALUE;

        // Read each pointer one at a time until we reach the first actual text.
        // This is because they didn't end the pointer list with a null
        while (ptrIndex < firstPtr) {
            ptr = (int) ByteUtils.readLittleEndian(rawBytes, ptrIndex,
                    PtcgRomConstants.TEXT_POINTER_SIZE_IN_BYTES)
                    + PtcgRomConstants.TEXT_POINTER_OFFSET;
            if (ptr < firstPtr) {
                firstPtr = ptr;
            }

            // Find the ending null byte
            textIndex = ptr;

            // Ensure its either null or starts with the prefix char
            CharSetPrefix charSet = CharSetPrefix.readFromByte(rawBytes[textIndex]);
            if (charSet != CharSetPrefix.EMPTY) {
                // Loop until we find the ending character if its not an empty text
                while (rawBytes[++textIndex] != CharMapConstants.TEXT_END_CHAR);
            }

            // Read the string to the null char (but not including it) and store where
            // it was read from
            // +1 to include the null term since address range end is exclusive
            AddressRange textRange = new AddressRange(ptr, textIndex + 1);
            short textId = texts.insertTextAtNextId(new String(rawBytes, ptr, textIndex - ptr));
            sourceMap.recordTextRange(textId, textRange);

            // Add it to the list of spaces for the text itself
            toBlankSpaceIn.addBlankedBlock(textRange);

            // Move our text pointer to the next pointer
            ptrIndex += PtcgRomConstants.TEXT_POINTER_SIZE_IN_BYTES;
        }

        // Note that the texts for whatever reason doesn't end with a nullptr so
        // that's why we don't add the pointer size one last time like done for
        // reading in the cards

        // Add the space for the pointers. The ptrIndex will end at the first text
        // + 1 because end is exclusive
        AddressRange textPtrsRange =
                new AddressRange(PtcgRomConstants.TEXT_POINTERS_LOC, ptrIndex + 1);
        sourceMap.setTextPointerTable(textPtrsRange);
        toBlankSpaceIn.addBlankedBlock(textPtrsRange);
        return texts;
    }

    public static Cards readCardsFromData(byte[] rawBytes, Texts allText, Blocks toBlankSpaceIn,
            RomSourceMap sourceMap) {
        // TODO: Optimize address range adding since they will mostly be in order

        Cards cards = new Cards();
        // Intentionally not clearing addressesReadToAddTo to support chaining calls/adding
        // to it via other functions

        // Read the cards based on the pointer map in the rom
        // Skip the first null pointer
        int ptrIndex =
                PtcgRomConstants.CARD_POINTERS_LOC + PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES;
        int cardIndex = 0;

        // Read each pointer one at a time until we reach the ending null pointer
        while ((cardIndex = (short) ByteUtils.readLittleEndian(rawBytes, ptrIndex,
                PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES)) != 0) {
            cardIndex += PtcgRomConstants.CARD_POINTER_OFFSET;
            Card card = Card.addCardFromBytes(rawBytes, cardIndex, allText, cards.cards());
            AddressRange cardRange = new AddressRange(cardIndex, cardIndex + card.getSize());
            sourceMap.recordCardRange(card.id, cardRange);

            // Add the space for the card itself
            toBlankSpaceIn.addBlankedBlock(cardRange);

            // Move our text pointer to the next pointer
            ptrIndex += PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES;
        }

        // Move it one last time for the trailing nullptr that finishes the list
        ptrIndex += PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES;

        // Add the space for the pointers. The ptrIndex will end at the first text
        // + 1 because end is exclusive
        AddressRange cardPtrsRange =
                new AddressRange(PtcgRomConstants.CARD_POINTERS_LOC, ptrIndex + 1);
        sourceMap.setCardPointerTable(cardPtrsRange);
        toBlankSpaceIn.addBlankedBlock(cardPtrsRange);

        return cards;
    }
}
