package redactedrice.ptcgr.resources;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import redactedrice.randomizer.utils.ManifestResourceExtractor;
import redactedrice.randomizer.utils.RandomizerBundledResources;

/**
 * Installs PTCG-specific bundled resources ({@code modules/}, {@code rules/}) and the URJ
 * randomizer Lua library (from {@code libUniversalRandomizerJava}) into the app working directory.
 */
public final class PtcgBundledResources {
    public static final String MODULES_RESOURCE = "modules";
    public static final String MODULES_DIR_NAME = "modules";
    public static final String RULES_RESOURCE = "rules";
    public static final String RULES_DIR_NAME = "rules";
    public static final String UNSUPPORTED_MOVES_FILE_NAME = "unsupported_moves.yaml";
    public static final String UNSUPPORTED_MOVES_CLASSPATH =
            "/" + RULES_DIR_NAME + "/" + UNSUPPORTED_MOVES_FILE_NAME;

    // Dev only test modules (e.g. exercising every argument constraint type in the config UI).
    // These live in a separate resource root so they can be excluded from release packages
    // (see appPtcgRandomizer's fatJar task) while still being installed for dev builds/runs.
    public static final String DEV_MODULES_RESOURCE = "devmodules";
    private static final String DEV_MODULES_SYSTEM_PROPERTY = "ptcgr.devModules";

    private final File workingDir;

    public PtcgBundledResources() {
        this(new File(System.getProperty("user.dir")));
    }

    PtcgBundledResources(File workingDir) {
        this.workingDir = workingDir;
    }

    public void installAll() {
        installRandomizerLibrary();
        installAppResources();
    }

    public void installRandomizerLibrary() {
        RandomizerBundledResources.install(workingDir, true);
    }

    public void installAppResources() {
        try {
            ManifestResourceExtractor.extract(MODULES_RESOURCE,
                    new File(workingDir, MODULES_DIR_NAME).getAbsolutePath(), true);
            ManifestResourceExtractor.extract(RULES_RESOURCE,
                    new File(workingDir, RULES_DIR_NAME).getAbsolutePath(), true);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to install PTCG bundled resources", e);
        }

        if (isDevModulesEnabled()) {
            installDevAppResources();
        }
    }

    /**
     * Installs the dev only test modules into the same modules directory as the regular ones (so
     * they show up alongside them in the UI). Only called when dev modules are enabled. These are
     * never present in a packaged release jar. Must run after installAppResources extracts (and
     * wipes) the regular modules directory.
     */
    public void installDevAppResources() {
        try {
            // overwriteExisting=false: merges into the modules dir instead of wiping it, since
            // installAppResources() already reset it to just the regular bundled modules
            ManifestResourceExtractor.extract(DEV_MODULES_RESOURCE,
                    getModulesDir().getAbsolutePath(), false);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to install PTCG dev test modules", e);
        }
    }

    public static boolean isDevModulesEnabled() {
        return Boolean.getBoolean(DEV_MODULES_SYSTEM_PROPERTY);
    }

    public File getRandomizerDir() {
        return RandomizerBundledResources.getInstalledDir(workingDir);
    }

    public File getModulesDir() {
        return new File(workingDir, MODULES_DIR_NAME);
    }

    public File getUnsupportedMovesFile() {
        return new File(new File(workingDir, RULES_DIR_NAME), UNSUPPORTED_MOVES_FILE_NAME);
    }

    /**
     * Install only entry point used by build verification and manual smoke tests. Optional first
     * argument is the working directory to extract into.
     */
    public static void main(String[] args) {
        File dir = args.length > 0 ? new File(args[0]) : new File(System.getProperty("user.dir"));
        PtcgBundledResources installer = new PtcgBundledResources(dir);
        installer.installAll();
        verifyInstalled(dir);
    }

    static void verifyInstalled(File dir) {
        requireFile(RandomizerBundledResources.getInstalledDir(dir).toPath().resolve("init.lua")
                .toFile());
        requireFile(new File(dir, "modules/actions/shuffle_hp.lua"));
        requireFile(new File(dir, "rules/unsupported_moves.yaml"));
    }

    private static void requireFile(File file) {
        if (!file.isFile()) {
            throw new IllegalStateException("Bundled resource was not extracted: " + file);
        }
    }
}
