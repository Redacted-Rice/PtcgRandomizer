package redactedrice.ptcgr.regression;

import java.io.File;

/** Options supported for regression test runs */
public final class RegressionOptions {
    public static final String PTCGR_ROM_ENV = "PTCGR_ROM";

    private static final String ROM_FLAG = "--rom";
    private static final String DEFAULT_ROM_FILE_NAME = "ptcg.gbc";

    public final File regressionModuleDir;
    public final File appDir;
    public final File rom;

    public RegressionOptions(File regressionModuleDir, File appDir, File rom) {
        this.regressionModuleDir = regressionModuleDir;
        this.appDir = appDir;
        this.rom = rom;
    }

    public static RegressionOptions fromArgs(String[] args) {
        if (args == null || args.length == 0 || args[0].isBlank()) {
            throw new IllegalArgumentException(
                    "Usage: RegressionSuiteRunner <appPtcgRandomizerDir> [--rom path]");
        }
        if (args[0].startsWith("-")) {
            throw new IllegalArgumentException(
                    "First argument must be the appPtcgRandomizer module directory");
        }

        File regressionModuleDir = new File(System.getProperty("user.dir")).getAbsoluteFile();
        File appDir = new File(args[0]).getAbsoluteFile();

        String romPath = null;
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (ROM_FLAG.equals(arg)) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("--rom requires a path");
                }
                romPath = args[++i];
                continue;
            }
            throw new IllegalArgumentException("Unknown argument: " + arg);
        }

        File rom = resolveRom(appDir, romPath);
        return new RegressionOptions(regressionModuleDir, appDir, rom);
    }

    private static File resolveRom(File appDir, String romPath) {
        if (romPath == null) {
            String envRom = System.getenv(PTCGR_ROM_ENV);
            if (envRom != null && !envRom.isBlank()) {
                romPath = envRom;
            }
        }
        File rom;
        if (romPath != null && !romPath.isBlank()) {
            rom = new File(romPath);
        } else {
            rom = new File(appDir, DEFAULT_ROM_FILE_NAME);
        }
        return rom.getAbsoluteFile();
    }
}
