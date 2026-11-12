package redactedrice.ptcgr.randomizer.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import redactedrice.ptcgr.configs.AppPreferences;
import redactedrice.ptcgr.resources.PtcgBundledResources;
import redactedrice.randomizer.utils.LogLevel;

class CommandLineArgsTest {

    @Test
    void parsesHeadlessAndLoadConfigPath() {
        CommandLineArgs launch = CommandLineArgs.parse(new String[] {CommandLineArgs.HEADLESS_FLAG,
                CommandLineArgs.CONFIG_FLAG, "my.yaml"});

        assertTrue(launch.isHeadless());
        assertTrue(launch.isLoadConfigRequested());
        assertEquals("my.yaml", launch.getLoadConfigCustomPath());
    }

    @Test
    void loadConfigWithoutPathIsUnknownArg() {
        CommandLineArgs launch =
                CommandLineArgs.parse(new String[] {CommandLineArgs.CONFIG_FLAG});

        assertFalse(launch.isLoadConfigRequested());
        assertNull(launch.getLoadConfigCustomPath());
        assertEquals(CommandLineArgs.CONFIG_FLAG, launch.getUnknownArgs().get(0));
    }

    @Test
    void parsesLoadRomPath() {
        CommandLineArgs launch = CommandLineArgs.parse(
                new String[] {CommandLineArgs.ROM_FLAG, "custom.gbc"});

        assertEquals("custom.gbc", launch.getLoadRomCustomPath());
    }

    @Test
    void parsesLogLevelForAnyMode() {
        CommandLineArgs launch = CommandLineArgs.parse(new String[] {CommandLineArgs.HEADLESS_FLAG,
                CommandLineArgs.LOG_LEVEL_FLAG, "debug", CommandLineArgs.CONFIG_FLAG,
                "batch.yaml"});

        assertEquals(LogLevel.DEBUG, launch.getLogLevel());
    }

    @Test
    void collectsUnknownArgs() {
        CommandLineArgs launch = CommandLineArgs.parse(new String[] {"--extra"});
        assertFalse(launch.getUnknownArgs().isEmpty());
        assertEquals("--extra", launch.getUnknownArgs().get(0));
    }

    @Test
    void parsesScriptTestsWithLogLevelAndCase() {
        CommandLineArgs launch = CommandLineArgs.parse(new String[] {CommandLineArgs.SCRIPT_TESTS_FLAG,
                CommandLineArgs.LOG_LEVEL_FLAG, "info", "test_one"});

        assertTrue(launch.isScriptTests());
        assertEquals(LogLevel.INFO, launch.getLogLevel());
        assertEquals("test_one", launch.getScriptTestCase());
    }

    @Test
    void romFlagWithoutPathIsUnknownArg() {
        CommandLineArgs launch = CommandLineArgs.parse(new String[] {CommandLineArgs.ROM_FLAG});
        assertEquals(CommandLineArgs.ROM_FLAG, launch.getUnknownArgs().get(0));
        assertNull(launch.getLoadRomCustomPath());
    }

    @Test
    void resourceReinstallFlagsAreNotUnknownArgs() {
        CommandLineArgs launch = CommandLineArgs.parse(new String[] {
                CommandLineArgs.REINSTALL_RESOURCES_FLAG, CommandLineArgs.CLEAN_REINSTALL_RESOURCES_FLAG,
                CommandLineArgs.SCRIPT_TESTS_FLAG});

        assertTrue(launch.getUnknownArgs().isEmpty());
        assertTrue(launch.isScriptTests());
        assertTrue(launch.isReinstallResources());
        assertTrue(launch.isCleanReinstallResources());
    }

    @Test
    void parseDoesNotSetReinstallPropertiesUntilApply() {
        System.clearProperty(PtcgBundledResources.REINSTALL_RESOURCES_SYSTEM_PROPERTY);
        System.clearProperty(PtcgBundledResources.CLEAN_REINSTALL_RESOURCES_SYSTEM_PROPERTY);

        CommandLineArgs launch = CommandLineArgs.parse(
                new String[] {CommandLineArgs.CLEAN_REINSTALL_RESOURCES_FLAG});
        assertFalse(Boolean.getBoolean(PtcgBundledResources.CLEAN_REINSTALL_RESOURCES_SYSTEM_PROPERTY));

        LaunchUtils.applyBundledResourceFlags(launch);
        assertTrue(Boolean.getBoolean(PtcgBundledResources.CLEAN_REINSTALL_RESOURCES_SYSTEM_PROPERTY));

        System.clearProperty(PtcgBundledResources.CLEAN_REINSTALL_RESOURCES_SYSTEM_PROPERTY);
    }

    @Test
    void startupRomUsesCustomPathWhenProvided() {
        AppPreferences prefs = AppPreferences.loadDefaults();
        CommandLineArgs launch =
                CommandLineArgs.parse(new String[] {CommandLineArgs.ROM_FLAG, "roms/test.gbc"});

        File rom = launch.getStartupRomFile(prefs);
        assertEquals(new File("roms/test.gbc").getAbsoluteFile(), rom);
    }

    @Test
    void startupRomFallsBackToPrefsWhenFlagHasNoPath() {
        AppPreferences prefs = AppPreferences.loadDefaults();
        CommandLineArgs launch = CommandLineArgs.parse(new String[] {CommandLineArgs.ROM_FLAG});

        assertEquals(LaunchUtils.resolveStartupRomFile(prefs), launch.getStartupRomFile(prefs));
    }

    @Test
    void startupConfigUsesCustomPathWhenProvided() {
        AppPreferences prefs = AppPreferences.loadDefaults();
        CommandLineArgs launch = CommandLineArgs.parse(
                new String[] {CommandLineArgs.CONFIG_FLAG, "configs/batch.yaml"});

        File config = launch.getStartupConfigFile(prefs);
        assertEquals(new File("configs/batch.yaml").getAbsoluteFile(), config);
    }

    @Test
    void startupConfigAbsentWhenFlagNotPassed() {
        AppPreferences prefs = AppPreferences.loadDefaults();
        CommandLineArgs launch = CommandLineArgs.parse(new String[] {CommandLineArgs.HEADLESS_FLAG});

        assertNull(launch.getStartupConfigFile(prefs));
    }
}
