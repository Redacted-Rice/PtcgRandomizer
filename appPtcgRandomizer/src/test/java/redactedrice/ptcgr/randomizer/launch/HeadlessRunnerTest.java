package redactedrice.ptcgr.randomizer.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import redactedrice.ptcgr.configs.YamlIO;
import redactedrice.ptcgr.randomizer.RandomizerCore;
import redactedrice.ptcgr.randomizer.Settings;
import redactedrice.randomizer.utils.LogLevel;

class HeadlessRunnerTest {

    @Test
    void headlessConfigWithoutPathReturnsUsageError() {
        CommandLineArgs launch = CommandLineArgs.parse(new String[] {CommandLineArgs.HEADLESS_FLAG,
                CommandLineArgs.CONFIG_FLAG});
        assertEquals(HeadlessRunner.USAGE_ERROR, HeadlessRunner.run(launch));
    }

    @Test
    void makeRandomizeSettingsUsesCliLogLevelAndSeed() {
        CommandLineArgs launch = CommandLineArgs.parse(new String[] {CommandLineArgs.LOG_LEVEL_FLAG,
                "debug"});
        Settings settings = HeadlessRunner.makeRandomizeSettings(launch, "42");
        assertEquals(LogLevel.DEBUG, settings.getLogLevel());
        assertEquals(HeadlessRunner.DEFAULT_HEADLESS_LOG_DETAILS, settings.isLogDetails());
        assertEquals("42", settings.getSeedString());
    }

    @Test
    void outputDirPlacesStandardArtifacts() {
        CommandLineArgs launch = CommandLineArgs.parse(new String[] {HeadlessRunner.OUTPUT_DIR_FLAG,
                "out/dir", HeadlessRunner.SAVE_CONFIG_FLAG});

        File outDir = HeadlessRunner.resolveOutputDir(launch);
        assertEquals(new File("out/dir").getAbsoluteFile(), outDir);
        assertEquals(new File(outDir, RandomizerCore.DEFAULT_PATCH_BASE_NAME).getAbsoluteFile(),
                HeadlessRunner.resolvePatchOutputFile(launch));
        assertEquals(new File(outDir, YamlIO.DEFAULT_FILE_NAME).getAbsoluteFile(),
                HeadlessRunner.resolveSaveConfigOutputFile(launch));
        assertTrue(launch.isSaveConfigRequested());
    }

    @Test
    void outputDirDefaultsToUserDir() {
        String cwd = System.getProperty("user.dir");
        CommandLineArgs launch = CommandLineArgs.parse(new String[] {CommandLineArgs.CONFIG_FLAG,
                "batch.yaml"});

        assertEquals(new File(cwd).getAbsoluteFile(), HeadlessRunner.resolveOutputDir(launch));
    }

    @Test
    void configUsesPathDefaultFileOrFailsWhenFlagHasNoPath() {
        CommandLineArgs withPath = CommandLineArgs.parse(
                new String[] {CommandLineArgs.CONFIG_FLAG, "batch.yaml"});
        CommandLineArgs noFlag = CommandLineArgs.parse(new String[] {CommandLineArgs.HEADLESS_FLAG});
        CommandLineArgs pathOnly = CommandLineArgs.parse(new String[] {CommandLineArgs.CONFIG_FLAG});

        assertEquals(new File("batch.yaml").getAbsoluteFile(),
                HeadlessRunner.resolveConfigFile(withPath));
        assertEquals(new File(YamlIO.DEFAULT_FILE_NAME).getAbsoluteFile(),
                HeadlessRunner.resolveConfigFile(noFlag));
        assertNull(HeadlessRunner.resolveConfigFile(pathOnly));
    }
}
