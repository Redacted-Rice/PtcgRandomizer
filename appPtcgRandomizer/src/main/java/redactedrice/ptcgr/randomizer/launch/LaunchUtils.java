package redactedrice.ptcgr.randomizer.launch;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import redactedrice.ptcgr.configs.AppPreferences;
import redactedrice.ptcgr.utils.IssuePresenter;
import redactedrice.randomizer.utils.IssueTracker;

/** Shared ROM, config, and settings setup for GUI and headless runs. */
public final class LaunchUtils {
    public static final String DEFAULT_ROM_NAME = "ptcg.gbc";

    private LaunchUtils() {
    }

    public static AppPreferences loadAppPreferences(Component parent) {
        try {
            IssueTracker.clear();
            AppPreferences prefs = AppPreferences.load();
            IssuePresenter.finishPhase(parent, "app preferences load");
            return prefs;
        } catch (IOException e) {
            e.printStackTrace();
            IssuePresenter.finishPhase(parent, "app preferences load");
            return AppPreferences.loadDefaults();
        }
    }

    public static File resolveStartupRomFile(AppPreferences appPreferences) {
        File rom = appPreferences.resolveLastRomFile();
        if (rom == null) {
            rom = new File(DEFAULT_ROM_NAME);
        }
        return rom;
    }

}
