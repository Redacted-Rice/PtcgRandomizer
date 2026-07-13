package redactedrice.ptcgr.constants;

import redactedrice.randomizer.utils.VersionProperties;

/**
 * Application version recorded in saved presets. Loaded from {@code app-version.properties}, which
 * is generated from {@code build.gradle.kts} at build time.
 */
public final class PtcgRandomizerVersion {
    public static final String PLATFORM_KEY = "PtcgRandomizer";
    public static final String VERSION = loadVersion();

    private PtcgRandomizerVersion() {}

    private static String loadVersion() {
        return VersionProperties.loadVersion(PtcgRandomizerVersion.class,
                "app-version.properties");
    }
}
