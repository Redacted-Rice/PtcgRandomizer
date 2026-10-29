package redactedrice.ptcgr.randomizer.launch;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import redactedrice.ptcgr.configs.AppPreferences;
import redactedrice.ptcgr.configs.Config;
import redactedrice.ptcgr.configs.YamlIO;
import redactedrice.ptcgr.randomizer.RandomizerCore;
import redactedrice.ptcgr.randomizer.Settings;
import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.utils.FileExtensionUtils;
import redactedrice.ptcgr.utils.IssuePresenter;
import redactedrice.randomizer.utils.IssueTracker;

/**
 * Headless randomize using app prefs for ROM autoload, patch output, and
 * logging defaults.
 */
public final class HeadlessRunner {
    private HeadlessRunner() {
    }

    public static int run(CommandLineArgs launch) {
        System.setProperty("java.awt.headless", "true");

        AppPreferences appPreferences = LaunchUtils.loadAppPreferences(null);

        File configFile = launch.getStartupConfigFile(appPreferences);
        if (configFile == null || !configFile.isFile()) {
            System.err.println("Config file not found: " + configFile);
            return 1;
        }

        RandomizerCore randomizer = new RandomizerCore(null);

        File rom = launch.getStartupRomFile(appPreferences);
        if (!rom.isFile()) {
            System.err.println("ROM not found: " + rom.getAbsolutePath());
            return 1;
        }
        if (!randomizer.openRom(rom, null)) {
            System.err.println("Failed to open ROM: " + rom.getAbsolutePath());
            return 1;
        }

        try {
            IssueTracker.clear();
            Map<String, Object> yaml = YamlIO.load(configFile);
            Config loaded = Config.readFromLoadedYamlMap(yaml, configFile.getName());
            if (!loaded.isValid()) {
                IssuePresenter.finishPhase(null, "config load");
                return 1;
            }

            Settings settings = LaunchUtils.settingsFrom(appPreferences, loaded);
            List<Action> actions;
            if (!loaded.hasAddableActions()) {
                System.err.println("Config has no runnable actions.");
                return 1;
            }
            loaded.checkRequiredScriptFingerprints(randomizer.getActionBank());
            actions = loaded.getActions(randomizer.getActionBank());

            File patchFile = FileExtensionUtils.ensureExtension(appPreferences.resolvePatchFile(),
                    RandomizerCore.PATCH_FILE_EXTENSION);

            if (!randomizer.randomizeAndSaveRom(patchFile, settings, actions)) {
                IssuePresenter.finishPhase(null, "randomize");
                return 1;
            }
            IssuePresenter.finishPhase(null, "randomize");
            System.out.println("Wrote patch: " + patchFile.getAbsolutePath());
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            IssuePresenter.finishPhase(null, "randomize");
            return 1;
        }
    }
}
