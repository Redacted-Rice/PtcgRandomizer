package redactedrice.ptcgr.randomizer.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import redactedrice.ptcgr.configs.AppPreferences;

class LaunchUtilsTest {

    @Test
    void resolveStartupRomUsesLastRomFromPrefsWhenSet(@TempDir File tempDir) throws IOException {
        File romFile = new File(tempDir, LaunchUtils.DEFAULT_ROM_NAME);
        Files.writeString(romFile.toPath(), "placeholder");

        AppPreferences prefs = AppPreferences.loadDefaults();
        prefs.setLastRomPath(romFile.getAbsolutePath());

        File rom = LaunchUtils.resolveStartupRomFile(prefs);
        assertEquals(romFile.getAbsoluteFile(), rom);
    }

    @Test
    void resolveStartupRomUsesDefaultNameWhenPrefsHaveNoRom() {
        AppPreferences prefs = AppPreferences.loadDefaults();
        prefs.setLastRomPath(null);

        File rom = LaunchUtils.resolveStartupRomFile(prefs);
        assertNotNull(rom);
        assertEquals(LaunchUtils.DEFAULT_ROM_NAME, rom.getName());
    }
}
