package redactedrice.ptcgr.rom;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import redactedrice.ptcgr.constants.PtcgRomConstants;

class RomIOTest {

    @Test
    void verifyGbHeaderChecksumAcceptsKnownPtcgHeader() {
        byte[] rom = newMinimalVerifiedRomBytes();

        assertDoesNotThrow(() -> RomIO.verifyGbHeaderChecksum(rom));
    }

    @Test
    void verifyRomAcceptsKnownPtcgHeaderAndSize() {
        byte[] rom = newMinimalVerifiedRomBytes();

        assertDoesNotThrow(() -> RomIO.verifyRom(rom));
    }

    @Test
    void verifyRomRejectsWrongSize() {
        byte[] rom = newMinimalVerifiedRomBytes();
        byte[] tooSmall = new byte[rom.length - 1];
        System.arraycopy(rom, 0, tooSmall, 0, tooSmall.length);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> RomIO.verifyRom(tooSmall));
        assertEquals("Failed to verify the rom: Expected size 1048576 but found 1048575",
                ex.getMessage());
    }

    @Test
    void verifyRomRejectsWrongHeaderFingerprint() {
        byte[] rom = newMinimalVerifiedRomBytes();
        rom[PtcgRomConstants.HEADER_LOCATION] = (byte) 0x00;

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> RomIO.verifyRom(rom));
        assertEquals("Failed to verify the rom: Header is incorrect!", ex.getMessage());
    }

    @Test
    void verifyGbHeaderChecksumRejectsWrongValue() {
        byte[] rom = newMinimalVerifiedRomBytes();
        rom[0x14D] = (byte) (rom[0x14D] + 1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> RomIO.verifyGbHeaderChecksum(rom));
        assertEquals("Failed to verify the rom: GB header checksum is incorrect!", ex.getMessage());
    }

    private static byte[] newMinimalVerifiedRomBytes() {
        int size = PtcgRomConstants.NUMBER_OF_BANKS * PtcgRomConstants.BANK_SIZE;
        byte[] rom = new byte[size];
        System.arraycopy(PtcgRomConstants.HEADER, 0, rom, PtcgRomConstants.HEADER_LOCATION,
                PtcgRomConstants.HEADER.length);
        return rom;
    }
}
