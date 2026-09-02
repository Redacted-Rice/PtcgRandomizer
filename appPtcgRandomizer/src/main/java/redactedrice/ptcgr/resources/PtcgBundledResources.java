package redactedrice.ptcgr.resources;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import redactedrice.ptcgr.constants.PtcgRandomizerVersion;
import redactedrice.randomizer.utils.ManifestResourceExtractor;
import redactedrice.randomizer.utils.RandomizerBundledResources;
import redactedrice.randomizer.utils.VersionedResourceInstaller;

/**
 * Installs PTCG specific bundled resources and the URJ randomizer Lua library into the app working
 * directory.
 *
 * modules/rules/run-scripts/script_tests are meant to be user editable (custom modules, tweaked
 * rules, hand edited wrapper scripts, extra test cases) but should still pick up new bundled
 * content on upgrade (see VersionedResourceInstaller). modules/rules/run-scripts always move
 * together so they share one marker at the working dir root. script_tests is only installed on
 * demand, so it gets its own marker instead. When the running app version doesn't match whats
 * recorded, whatever is currently installed gets moved into backups/ before the new copy is
 * installed. Nothing is modified until the next version change. See isForceReinstallEnabled() to
 * force this.
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
    public static final String SCRIPT_TESTS_RESOURCE = "script_tests";
    public static final String SCRIPT_TESTS_DIR_NAME = "script_tests";
    public static final String RUN_SCRIPTS_RESOURCE = "run-scripts";
    public static final String BACKUPS_DIR_NAME = "backups";
    private static final String DEV_MODULES_SYSTEM_PROPERTY = "ptcgr.devModules";
    // Forces the backup and reinstall below even if the version marker already matches
    public static final String FORCE_REINSTALL_SYSTEM_PROPERTY = "ptcgr.forceReinstallResources";
    // Shared by modules/rules/run-scripts since they always move together. Lives at the working
    // dir root since run-scripts has no dedicated subdir of its own to hold it in.
    private static final String RESOURCES_VERSION_FILE_NAME = "ptcgr-res-ver";

    private final File workingDir;

    public PtcgBundledResources() {
        this(new File(System.getProperty("user.dir")));
    }

    public PtcgBundledResources(File workingDir) {
        this.workingDir = workingDir;
    }

    public void installAll() {
        installRandomizerLibrary();
        installAppResources();
    }

    public void installRandomizerLibrary() {
        RandomizerBundledResources.install(workingDir, getBackupsDir(), isForceReinstallEnabled());
    }

    public void installAppResources() {
        try {
            File backupsDir = getBackupsDir();
            String version = PtcgRandomizerVersion.VERSION;
            File marker = new File(workingDir, RESOURCES_VERSION_FILE_NAME);

            if (VersionedResourceInstaller.needsReinstall(marker, version,
                    isForceReinstallEnabled())) {
                VersionedResourceInstaller.backupAndInstall(MODULES_RESOURCE,
                        new File(workingDir, MODULES_DIR_NAME), backupsDir, MODULES_DIR_NAME);
                VersionedResourceInstaller.backupAndInstall(RULES_RESOURCE,
                        new File(workingDir, RULES_DIR_NAME), backupsDir, RULES_DIR_NAME);
                // No backupSubDir: run-scripts extracts to workingDir's root, so back it up
                // there too instead of nesting it under a "run-scripts" folder.
                VersionedResourceInstaller.backupAndInstall(RUN_SCRIPTS_RESOURCE, workingDir,
                        backupsDir, null);
                VersionedResourceInstaller.writeVersionMarker(marker, version);

                System.out.println("Updated bundled modules/rules/run-scripts to version " + version
                        + ". Previous copies (if any) are in " + backupsDir.getAbsolutePath());
            }
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
     * never present in a packaged release jar. Must run after installAppResources so the regular
     * modules are already in place.
     */
    public void installDevAppResources() {
        try {
            ManifestResourceExtractor.extract(DEV_MODULES_RESOURCE,
                    getModulesDir().getAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to install PTCG dev test modules", e);
        }
    }

    public static boolean isDevModulesEnabled() {
        return Boolean.getBoolean(DEV_MODULES_SYSTEM_PROPERTY);
    }

    public static boolean isForceReinstallEnabled() {
        return Boolean.getBoolean(FORCE_REINSTALL_SYSTEM_PROPERTY);
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

    public File getScriptTestsDir() {
        return new File(workingDir, SCRIPT_TESTS_DIR_NAME);
    }

    public File getBackupsDir() {
        return new File(workingDir, BACKUPS_DIR_NAME);
    }

    // Only installed on demand (running with --script-tests), independently of installAppResources
    // above so it gets backed up and refreshed on its own first run under a new version
    // regardless of whether installAppResources() already ran.
    public void installScriptTests() {
        try {
            File scriptTestsDir = getScriptTestsDir();
            VersionedResourceInstaller.installIfNeeded(SCRIPT_TESTS_RESOURCE, scriptTestsDir,
                    new File(scriptTestsDir, RESOURCES_VERSION_FILE_NAME),
                    PtcgRandomizerVersion.VERSION, getBackupsDir(), isForceReinstallEnabled());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to install script tests", e);
        }
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
