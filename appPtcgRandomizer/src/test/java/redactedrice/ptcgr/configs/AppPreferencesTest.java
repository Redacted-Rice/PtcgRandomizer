package redactedrice.ptcgr.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import redactedrice.ptcgr.randomizer.RandomizerCore;
import redactedrice.randomizer.utils.LogLevel;

class AppPreferencesTest {
    @TempDir
    Path tempDir;

    @Test
    void loadDefaultsUsesBuiltInValues() {
        AppPreferences prefs = AppPreferences.loadDefaults();

        assertEquals(LogLevel.INFO, prefs.getLogLevel());
        assertTrue(prefs.isLogDetails());
        assertTrue(prefs.isSaveSettings());
    }

    @Test
    void loadMissingFileReturnsDefaults() throws IOException {
        File missing = tempDir.resolve("missing.yaml").toFile();
        AppPreferences prefs = AppPreferences.load(missing);

        assertEquals(LogLevel.INFO, prefs.getLogLevel());
        assertTrue(prefs.isLogDetails());
    }

    @Test
    void saveAndLoadRoundTripsUiState() throws IOException {
        Path romDir = tempDir.resolve("roms");
        Path outDir = tempDir.resolve("out");
        Path cfgDir = tempDir.resolve("cfg");
        Path loadDir = tempDir.resolve("load");
        Files.createDirectories(romDir);
        Files.createDirectories(outDir);
        Files.createDirectories(cfgDir);
        Files.createDirectories(loadDir);

        File romFile = romDir.resolve("ptcg.gbc").toFile();
        File patchFile = outDir.resolve("ptcg_randomized.bps").toFile();
        File saveConfigFile = cfgDir.resolve("ptcg_randomize.yaml").toFile();
        File loadConfigFile = loadDir.resolve("last.yaml").toFile();

        AppPreferences original = AppPreferences.fromAppState(LogLevel.DEBUG, false, false, 10, 20,
                800, 600, romFile.getAbsolutePath(), romDir.toFile(), romFile, outDir.toFile(),
                patchFile, cfgDir.toFile(), saveConfigFile, loadDir.toFile(), loadConfigFile);

        File output = tempDir.resolve(AppPreferences.DEFAULT_FILE_NAME).toFile();
        original.save(output);

        String yamlText = Files.readString(output.toPath());
        assertTrue(yamlText.startsWith("# PTCG Randomizer app preferences\n"));
        assertFalse(yamlText.contains("seed:"));
        assertFalse(yamlText.contains("actions:"));

        AppPreferences reloaded = AppPreferences.load(output);
        assertEquals(LogLevel.DEBUG, reloaded.getLogLevel());
        assertFalse(reloaded.isLogDetails());
        assertFalse(reloaded.isSaveSettings());
        assertEquals(Integer.valueOf(10), reloaded.getWindowX());
        assertEquals(Integer.valueOf(20), reloaded.getWindowY());
        assertEquals(Integer.valueOf(800), reloaded.getWindowWidth());
        assertEquals(Integer.valueOf(600), reloaded.getWindowHeight());
        assertEquals(romFile.getAbsolutePath(), reloaded.getLastRomPath());
        assertEquals(romDir.toFile().getAbsolutePath(), reloaded.getOpenRomDirectory());
        assertEquals("ptcg.gbc", reloaded.getOpenRomFileName());
        assertEquals(outDir.toFile().getAbsolutePath(), reloaded.getPatchDirectory());
        assertEquals("ptcg_randomized.bps", reloaded.getPatchFileName());
        assertEquals(cfgDir.toFile().getAbsolutePath(), reloaded.getSaveConfigDirectory());
        assertEquals("ptcg_randomize.yaml", reloaded.getSaveConfigFileName());
        assertEquals(loadDir.toFile().getAbsolutePath(), reloaded.getLoadConfigDirectory());
        assertEquals("last.yaml", reloaded.getLoadConfigFileName());
    }

    @Test
    void readFromLoadedYamlMapParsesKnownFields() {
        Map<String, Object> root = Map.of("version", 1, "logLevel", "WARN", "logDetails", false,
                "saveSettings", true, "windowWidth", 640, "windowHeight", 480, "lastRomPath",
                "C:/roms/ptcg.gbc", "patchFileName", "custom.bps");

        AppPreferences prefs = AppPreferences.readFromLoadedYamlMap(root);

        assertEquals(LogLevel.WARN, prefs.getLogLevel());
        assertFalse(prefs.isLogDetails());
        assertTrue(prefs.isSaveSettings());
        assertEquals(Integer.valueOf(640), prefs.getWindowWidth());
        assertEquals(Integer.valueOf(480), prefs.getWindowHeight());
        assertEquals("C:/roms/ptcg.gbc", prefs.getLastRomPath());
        assertEquals("custom.bps", prefs.getPatchFileName());
    }

    @Test
    void resolveNamedFileFallsBackToDefaults() {
        AppPreferences prefs = AppPreferences.loadDefaults();

        assertEquals(RandomizerCore.DEFAULT_PATCH_BASE_NAME,
                prefs.resolvePatchFile().getName());
        assertEquals(YamlIO.DEFAULT_FILE_NAME, prefs.resolveSaveConfigFile().getName());
        assertEquals(YamlIO.DEFAULT_FILE_NAME, prefs.resolveLoadConfigFile().getName());
        assertEquals("ptcg.gbc", prefs.resolveOpenRomFile("ptcg.gbc").getName());
    }

    @Test
    void defaultFileLivesNextToAppDirectory() {
        File prefsFile = AppPreferences.defaultFile();
        assertEquals(AppPreferences.DEFAULT_FILE_NAME, prefsFile.getName());
        assertEquals(AppPreferences.resolveAppDirectory(), prefsFile.getParentFile());
    }

    @Test
    void saveCreatesParentDirectory() throws IOException {
        File nested = tempDir.resolve("prefs/nested/" + AppPreferences.DEFAULT_FILE_NAME).toFile();
        AppPreferences prefs = AppPreferences.loadDefaults();
        prefs.setLogLevel(LogLevel.ERROR);

        prefs.save(nested);

        assertTrue(nested.isFile());
        assertEquals(LogLevel.ERROR, AppPreferences.load(nested).getLogLevel());
    }
}
