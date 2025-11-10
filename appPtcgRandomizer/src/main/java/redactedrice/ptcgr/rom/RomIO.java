package redactedrice.ptcgr.rom;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import redactedrice.bpsqueuedwriter.BpsWriter;
import redactedrice.bpsqueuedwriter.compiler.BpsInstructionSetParser;
import redactedrice.compiler.GbZ80InstructionSetParser;
import redactedrice.compiler.InstructionParser;
import redactedrice.gbcframework.addressing.AddressRange;
import redactedrice.gbcframework.addressing.AssignedAddresses;
import redactedrice.gbcframework.utils.ByteUtils;
import redactedrice.ptcgr.compiler.PtcgInstructionSetParser;
import redactedrice.ptcgr.constants.CharMapConstants;
import redactedrice.ptcgr.constants.PtcgRomConstants;
import redactedrice.ptcgr.constants.CharMapConstants.CharSetPrefix;
import redactedrice.ptcgr.data.Card;
import redactedrice.ptcgr.data.customcardeffects.HardcodedEffects;
import redactedrice.rompacker.Blocks;
import redactedrice.rompacker.DataManager;

public class RomIO {
    private RomIO() {}

    public static RomData readFromFile(File romFile) throws IOException {
        byte[] rawBytes = Files.readAllBytes(romFile.toPath());
        RomIO.verifyRom(rawBytes);
        return new RomData(rawBytes, readFromBytes(rawBytes));
    }
    
    private static void verifyRom(byte[] rawBytes) {
        // TODO later: Do a CRC instead/in addition to? Maybe if we go with the BPS patch format
        int index = PtcgRomConstants.HEADER_LOCATION;
        for (byte headerByte : PtcgRomConstants.HEADER) {
            if (headerByte != rawBytes[index++]) {
                throw new IllegalArgumentException(
                        "Failed to verify the rom: Header is incorrect!");
            }
        }
    }
    
    public static RandomizationData readFromBytes(byte[] bytes) {
    	RandomizationData data = new RandomizationData();
    	data.idsToText = readTextsFromData(bytes, data.blocks);
    	data.allCards = readCardsFromData(bytes, data.idsToText, data.blocks);
    	return data;
    }

    // TODO: Testing only. Should remove once BPS is good and ready
    static void writeRaw(byte[] rawBytes, File romFile) {
        try (FileOutputStream fos = new FileOutputStream(romFile)) {
            fos.write(rawBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Note assumes that the first text in the pointer list is the first in the file as well. This
    // is required
    // since there is no null between the text pointer map and the texts themselves
    static Texts readTextsFromData(byte[] rawBytes, Blocks toBlankSpaceIn) {
        // TODO: Optimize address range adding since they will mostly be in order

        Texts texts = new Texts();
        // Intentionally not clearing addressesReadToAddTo to support chaining calls/adding
        // to it via other functions

        // Read the text based on the pointer map in the rom
        // First pointer is a null pointer so we skip it
        int ptrIndex = PtcgRomConstants.TEXT_POINTERS_LOC
                + PtcgRomConstants.TEXT_POINTER_SIZE_IN_BYTES;
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
                while (rawBytes[++textIndex] != CharMapConstants.TEXT_END_CHAR)
                    ;
            }

            // Read the string to the null char (but not including it) and store where
            // it was read from
            // +1 to include the null term since address range end is exclusive
            texts.insertTextAtNextId(new String(rawBytes, ptr, textIndex - ptr),
                    new AddressRange(ptr, textIndex + 1));

            // Add it to the list of spaces for the text itself
            toBlankSpaceIn.addBlankedBlock(new AddressRange(ptr, textIndex + 1));

            // Move our text pointer to the next pointer
            ptrIndex += PtcgRomConstants.TEXT_POINTER_SIZE_IN_BYTES;
        }

        // Note that the texts for whatever reason doesn't end with a nullptr so
        // that's why we don't add the pointer size one last time like done for
        // reading in the cards

        // Add the space for the pointers. The ptrIndex will end at the first text
        // + 1 because end is exclusive
        AddressRange textPtrsRange = new AddressRange(PtcgRomConstants.TEXT_POINTERS_LOC,
                ptrIndex + 1);
        texts.setOrigPtrsRange(textPtrsRange);
        toBlankSpaceIn.addBlankedBlock(textPtrsRange);
        return texts;
    }

    static Cards readCardsFromData(byte[] rawBytes, Texts allText, Blocks toBlankSpaceIn) {
        // TODO: Optimize address range adding since they will mostly be in order

        Cards cards = new Cards();
        // Intentionally not clearing addressesReadToAddTo to support chaining calls/adding
        // to it via other functions

        // Read the cards based on the pointer map in the rom
        // Skip the first null pointer
        int ptrIndex = PtcgRomConstants.CARD_POINTERS_LOC
                + PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES;
        int cardIndex = 0;

        // Read each pointer one at a time until we reach the ending null pointer
        while ((cardIndex = (short) ByteUtils.readLittleEndian(rawBytes, ptrIndex,
                PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES)) != 0) {
            cardIndex += PtcgRomConstants.CARD_POINTER_OFFSET;
            int size = Card.addCardFromBytes(rawBytes, cardIndex, allText, cards.cards());

            // Add the space for the card itself
            toBlankSpaceIn.addBlankedBlock(new AddressRange(cardIndex, cardIndex + size));

            // Move our text pointer to the next pointer
            ptrIndex += PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES;
        }

        // Move it one last time for the trailing nullptr that finishes the list
        ptrIndex += PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES;

        // Add the space for the pointers. The ptrIndex will end at the first text
        // + 1 because end is exclusive
        AddressRange cardPtrsRange = new AddressRange(PtcgRomConstants.CARD_POINTERS_LOC,
                ptrIndex + 1);
        cards.setOrigPtrsRange(cardPtrsRange);
        toBlankSpaceIn.addBlankedBlock(cardPtrsRange);

        return cards;
    }

    public static void writePatch(RomData romData, File patchFile) {
        // Create the custom parser and set the data blocks to use it
        PtcgInstructionSetParser ptcgParser = new PtcgInstructionSetParser();
        InstructionParser parser = new InstructionParser(List.of(ptcgParser,
                new BpsInstructionSetParser(), new GbZ80InstructionSetParser()));

        // TODO later: Need to handle tweak blocks somehow. Should these all be
        // file defined and selected via a menu? could also include if they default
        // to on or not. Also for now we can handle these after the other blocks
        // are generated but we arbitrarily do it before. Is there any reason to
        // do one or the other?
        // CustomCardEffect.addTweakToAllowEffectsInMoreBanks(blocks, parser);

        // Finalize all the data to prepare for writing
        finalizeDataAndGenerateBlocks(romData.modified, parser, ptcgParser);

        // Now assign locations for the data
        DataManager manager = new DataManager();
        AssignedAddresses assignedAddresses = manager.allocateBlocks(romData.rawBytes, romData.modified.blocks);

        // Finally write the patch file
        RomIO.writeBpsPatch(patchFile, romData.rawBytes, romData.modified.blocks, assignedAddresses);
    }

    private static void finalizeDataAndGenerateBlocks(RandomizationData patchedData, InstructionParser parser,
            PtcgInstructionSetParser ptcgParser) {
        // Reset the singleton -- TODO later: Needed?
        HardcodedEffects.reset();

        // Finalize the card data, texts and blocks
        patchedData.allCards.finalizeConvertAndAddData(patchedData.idsToText, patchedData.blocks, parser);

        // Now add all the text from the custom parser instructions
        ptcgParser.finalizeAndAddTexts(patchedData.idsToText);

        // Convert the text to blocks
        patchedData.idsToText.convertAndAddBlocks(patchedData.blocks);

        // Sort them and combine values to make things easier elsewhere in the code
        // TODO later: if adding custom blanking, we should call this afterwards
        // TODO: sorted twice?
        AddressRange.sortAndCombine(patchedData.blocks.getAllBlankedBlocks());
    }
    
    public static void writeBpsPatch(File patchFile, byte[] rawBytes, Blocks blocks,
            AssignedAddresses assignedAddresses) {
        // Now actually write to the bytes
        BpsWriter writer = new BpsWriter(rawBytes);
        try {
            blocks.writeBlocks(writer, assignedAddresses);
            writer.writeBps(patchFile, blocks.getAllBlankedBlocks());
        } catch (IOException e) {
            // TODO later: Auto-generated catch block
            e.printStackTrace();
        }
    }
}
