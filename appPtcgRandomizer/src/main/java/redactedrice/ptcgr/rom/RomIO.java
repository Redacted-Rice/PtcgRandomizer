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
import redactedrice.gbcframework.addressing.AssignedAddresses;
import redactedrice.gbcframework.utils.ByteUtils;
import redactedrice.ptcgr.compiler.PtcgInstructionSetParser;
import redactedrice.ptcgr.constants.PtcgRomConstants;
import redactedrice.ptcgr.rules.Rules;
import redactedrice.rompacker.Blocks;
import redactedrice.rompacker.DataManager;

public class RomIO {
    private RomIO() {}

    public static RomData readFromFile(File romFile, Rules rules) throws IOException {
        byte[] rawBytes = Files.readAllBytes(romFile.toPath());
        RomIO.verifyRom(rawBytes);

        RomSourceMap sourceMap = new RomSourceMap();
        Blocks romBlanks = new Blocks();
        Texts texts = RomReader.readTextsFromData(rawBytes, romBlanks, sourceMap);
        Cards cards = RomReader.readCardsFromData(rawBytes, texts, romBlanks, sourceMap);

        return new RomData(rawBytes, cards, texts, sourceMap, romBlanks, rules);
    }

    private static void verifyRom(byte[] rawBytes) {
        int expectedSize = PtcgRomConstants.NUMBER_OF_BANKS * PtcgRomConstants.BANK_SIZE;
        if (rawBytes.length != expectedSize) {
            throw new IllegalArgumentException("Failed to verify the rom: Expected size "
                    + expectedSize + " but found " + rawBytes.length);
        }

        int index = PtcgRomConstants.HEADER_LOCATION;
        for (byte headerByte : PtcgRomConstants.HEADER) {
            if (headerByte != rawBytes[index++]) {
                throw new IllegalArgumentException(
                        "Failed to verify the rom: Header is incorrect!");
            }
        }

        verifyGbChecksum(rawBytes);
    }

    // The standard GB approach for the header checksum. This is not the whole rom but
    // just the header stuff.
    private static void verifyGbChecksum(byte[] rawBytes) {
        int checksum = 0;
        for (int i = 0; i <= 0x14D; i++) {
            checksum = (checksum + ByteUtils.unsignedByteAsShort(rawBytes[i])) & 0xFF;
        }
        if (checksum != 0) {
            throw new IllegalArgumentException(
                    "Failed to verify the rom: GB checksum is incorrect!");
        }
    }

    // !!! For BPS Writing Testing only !!!
    static void writeRaw(byte[] rawBytes, File romFile) {
        try (FileOutputStream fos = new FileOutputStream(romFile)) {
            fos.write(rawBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writePatch(RomData romData, File patchFile) {
        PtcgInstructionSetParser ptcgParser = new PtcgInstructionSetParser();
        InstructionParser parser = new InstructionParser(List.of(ptcgParser,
                new BpsInstructionSetParser(), new GbZ80InstructionSetParser()));

        Blocks writeBlocks = romData.romBlanks.copy();
        Texts writeTexts = romData.texts.copy();
        RomBlockAssembler.assemble(romData.modified, writeTexts, writeBlocks, romData.sourceMap,
                parser, ptcgParser);

        DataManager manager = new DataManager();
        AssignedAddresses assignedAddresses =
                manager.allocateBlocks(romData.rawBytes, writeBlocks);

        RomIO.writeBpsPatch(patchFile, romData.rawBytes, writeBlocks, assignedAddresses);
    }

    public static void writeBpsPatch(File patchFile, byte[] rawBytes, Blocks blocks,
            AssignedAddresses assignedAddresses) {
        BpsWriter writer = new BpsWriter(rawBytes);
        try {
            blocks.writeBlocks(writer, assignedAddresses);
            writer.writeBps(patchFile, blocks.getAllBlankedBlocks());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write BPS patch to " + patchFile.getPath(),
                    e);
        }
    }
}
