package redactedrice.ptcgr.randomizer.launch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import redactedrice.ptcgr.configs.Config;
import redactedrice.ptcgr.configs.YamlIO;
import redactedrice.ptcgr.randomizer.RandomizerCore;
import redactedrice.ptcgr.randomizer.Settings;
import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.utils.FileExtensionUtils;
import redactedrice.ptcgr.utils.IssuePresenter;
import redactedrice.randomizer.utils.IssueTracker;
import redactedrice.randomizer.utils.LogLevel;
import redactedrice.randomizer.utils.Logger;

/**
 * Headless batch randomize from CLI flags and built-in defaults
 */
public final class HeadlessRunner {
    public static final String OUTPUT_DIR_FLAG = "--output-dir";
    public static final String SAVE_CONFIG_FLAG = "--save-config";

    static final boolean DEFAULT_HEADLESS_LOG_DETAILS = true;

    // exit codes mirrored regression harness when running regression tests
    public static final int SUCCESS = 0;
    public static final int FAILURE = 1;
    public static final int ROM_NOT_FOUND = 2;
    public static final int USAGE_ERROR = 3;

    private HeadlessRunner() {
    }

    public static int run(CommandLineArgs launch) {
        System.setProperty("java.awt.headless", "true");

        if (LaunchUtils.hasUnknownArgs(launch)) {
            LaunchUtils.printUnknownArgs(launch);
            return USAGE_ERROR;
        }

        File configFile = resolveConfigFile(launch);
        if (!configFile.isFile()) {
            System.err.println("Config file not found: " + configFile);
            return FAILURE;
        }

        RandomizerCore randomizer = new RandomizerCore(null);

        File rom = resolveRomFile(launch);
        if (!rom.isFile()) {
            System.err.println("ROM not found: " + rom.getAbsolutePath());
            return ROM_NOT_FOUND;
        }
        if (!randomizer.openRom(rom, null)) {
            System.err.println("Failed to open ROM: " + rom.getAbsolutePath());
            return FAILURE;
        }

        try {
            IssueTracker.clear();
            Map<String, Object> yaml = YamlIO.load(configFile);
            Config loaded = Config.readFromLoadedYamlMap(yaml, configFile.getName());
            if (!loaded.isValid()) {
                IssuePresenter.finishPhase(null, "config load");
                return FAILURE;
            }

            if (loaded.hasRules()) {
                randomizer.mergeRulesFromConfig(loaded.getRulesConfig());
            }

            Settings settings = makeRandomizeSettings(launch, loaded.getSeed());
            if (!loaded.hasAddableActions()) {
                System.err.println("Config has no runnable actions.");
                return FAILURE;
            }
            loaded.checkRequiredScriptFingerprints(randomizer.getActionBank());
            List<Action> actions = loaded.getActions(randomizer.getActionBank());

            File outputDir = resolveOutputDir(launch);
            Files.createDirectories(outputDir.toPath());

            File patchFile = FileExtensionUtils.ensureExtension(resolvePatchOutputFile(launch),
                    RandomizerCore.PATCH_FILE_EXTENSION);

            if (!randomizer.randomizeAndSaveRom(patchFile, settings, actions)) {
                IssuePresenter.finishPhase(null, "randomize");
                return FAILURE;
            }
            if (launch.isSaveConfigRequested()) {
                File savedConfig = FileExtensionUtils.ensureExtension(
                        resolveSaveConfigOutputFile(launch), YamlIO.FILE_EXTENSION);
                Config configSnapshot = Config.fromAppState(settings.getSeedString(), actions,
                        randomizer.getActionBank(), randomizer.getRules(),
                        randomizer.getReferenceMonsterCards());
                YamlIO.save(savedConfig, configSnapshot.convertToYamlMap());
            }
            IssuePresenter.finishPhase(null, "randomize");
            System.out.println("Wrote patch: " + patchFile.getAbsolutePath());
            return SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
            IssuePresenter.finishPhase(null, "randomize");
            return FAILURE;
        }
    }

    static Settings makeRandomizeSettings(CommandLineArgs launch, String seed) {
        Settings settings = new Settings();
        settings.setLogDetails(DEFAULT_HEADLESS_LOG_DETAILS);
        LogLevel logLevel = launch.getLogLevel();
        settings.setLogLevel(logLevel);
        Logger.setMinLogLevel(logLevel);
        settings.setSeed(seed);
        return settings;
    }

    static File resolveConfigFile(CommandLineArgs launch) {
        if (launch.isLoadConfigRequested()) {
            return new File(launch.getLoadConfigCustomPath()).getAbsoluteFile();
        }
        return new File(YamlIO.DEFAULT_FILE_NAME).getAbsoluteFile();
    }

    static File resolveRomFile(CommandLineArgs launch) {
        String romPath = launch.getLoadRomCustomPath();
        if (romPath != null && !romPath.isBlank()) {
            return new File(romPath).getAbsoluteFile();
        }
        return new File(LaunchUtils.DEFAULT_ROM_NAME).getAbsoluteFile();
    }

    static File resolveOutputDir(CommandLineArgs launch) {
        String outputDirPath = launch.getOutputDirPath();
        if (outputDirPath != null && !outputDirPath.isBlank()) {
            return new File(outputDirPath).getAbsoluteFile();
        }
        return new File(System.getProperty("user.dir")).getAbsoluteFile();
    }

    static File resolvePatchOutputFile(CommandLineArgs launch) {
        return new File(resolveOutputDir(launch), RandomizerCore.DEFAULT_PATCH_BASE_NAME);
    }

    static File resolveSaveConfigOutputFile(CommandLineArgs launch) {
        return new File(resolveOutputDir(launch), YamlIO.DEFAULT_FILE_NAME);
    }
}
