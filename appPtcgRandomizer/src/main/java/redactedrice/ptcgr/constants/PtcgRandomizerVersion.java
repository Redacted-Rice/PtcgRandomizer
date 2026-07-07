package redactedrice.ptcgr.constants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Application version recorded in saved presets. Loaded from {@code app-version.properties}, which
 * is generated from {@code build.gradle.kts} at build time.
 */
public final class PtcgRandomizerVersion {
    public static final String VERSION = loadVersion();

    private PtcgRandomizerVersion() {}

    private static String loadVersion() {
        try (InputStream input =
                PtcgRandomizerVersion.class.getResourceAsStream("app-version.properties")) {
            if (input == null) {
                throw new IllegalStateException(
                        "Missing app-version.properties; run a Gradle build first.");
            }
            Properties properties = new Properties();
            properties.load(input);
            String version = properties.getProperty("version");
            if (version == null || version.isBlank()) {
                throw new IllegalStateException("app-version.properties is missing version.");
            }
            return version;
        } catch (IOException error) {
            throw new ExceptionInInitializerError(error);
        }
    }
}
