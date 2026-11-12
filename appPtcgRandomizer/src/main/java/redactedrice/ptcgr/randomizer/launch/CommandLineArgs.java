package redactedrice.ptcgr.randomizer.launch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import redactedrice.ptcgr.configs.AppPreferences;
import redactedrice.randomizer.utils.LogLevel;

/**
 * CLI flags for GUI, headless batch, and Lua script tests.
 * RegressionSuiteRunner copies the flags it passes to the fat JAR. Keep those strings in sync.
 */
public final class CommandLineArgs {
    public static final String HEADLESS_FLAG = "--headless";
    public static final String CONFIG_FLAG = "--config";
    public static final String ROM_FLAG = "--rom";
    public static final String SCRIPT_TESTS_FLAG = "--script-tests";
    public static final String LOG_LEVEL_FLAG = "--log-level";
    // Forces PtcgBundledResources to redo the backup and reinstall of resources
    // like on version updates
    public static final String REINSTALL_RESOURCES_FLAG = "--reinstall-resources";
    // Forces PtcgBundledResources to redo the backup and reinstall of resources
    // like on version updates, but also deletes the existing backup
    public static final String CLEAN_REINSTALL_RESOURCES_FLAG = "--clean-reinstall-resources";
    public static final LogLevel DEFAULT_LOG_LEVEL = LogLevel.WARN;

    private final boolean headless;
    private final boolean loadConfigRequested;
    private final String loadConfigCustomPath;
    private final String loadRomCustomPath;
    private final String outputDirPath;
    private final boolean saveConfigRequested;
    private final boolean scriptTests;
    private final LogLevel logLevel;
    private final String scriptTestCase;
    private final boolean reinstallResources;
    private final boolean cleanReinstallResources;
    private final List<String> unknownArgs;

    private CommandLineArgs(boolean headless, boolean loadConfigRequested, String loadConfigCustomPath,
            String loadRomCustomPath, String outputDirPath, boolean saveConfigRequested,
            boolean scriptTests, LogLevel logLevel, String scriptTestCase,
            boolean reinstallResources, boolean cleanReinstallResources, List<String> unknownArgs) {
        this.headless = headless;
        this.loadConfigRequested = loadConfigRequested;
        this.loadConfigCustomPath = loadConfigCustomPath;
        this.loadRomCustomPath = loadRomCustomPath;
        this.outputDirPath = outputDirPath;
        this.saveConfigRequested = saveConfigRequested;
        this.scriptTests = scriptTests;
        this.logLevel = logLevel;
        this.scriptTestCase = scriptTestCase;
        this.reinstallResources = reinstallResources;
        this.cleanReinstallResources = cleanReinstallResources;
        this.unknownArgs = List.copyOf(unknownArgs);
    }

    public static CommandLineArgs parse(String[] args) {
        if (args == null) {
            args = new String[0];
        }
        boolean scriptTests = false;
        boolean headless = false;
        boolean loadConfigRequested = false;
        String loadConfigCustomPath = null;
        String loadRomCustomPath = null;
        String outputDirPath = null;
        boolean saveConfigRequested = false;
        LogLevel logLevel = DEFAULT_LOG_LEVEL;
        String scriptTestCase = null;
        boolean reinstallResources = false;
        boolean cleanReinstallResources = false;
        List<String> unknown = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (REINSTALL_RESOURCES_FLAG.equals(arg)) {
                reinstallResources = true;
                continue;
            }
            if (CLEAN_REINSTALL_RESOURCES_FLAG.equals(arg)) {
                cleanReinstallResources = true;
                continue;
            }
            if (SCRIPT_TESTS_FLAG.equals(arg)) {
                scriptTests = true;
                continue;
            }
            if (HEADLESS_FLAG.equals(arg)) {
                headless = true;
                continue;
            }
            if (CONFIG_FLAG.equals(arg)) {
                if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
                    unknown.add(arg);
                } else {
                    loadConfigRequested = true;
                    loadConfigCustomPath = args[++i];
                }
                continue;
            }
            if (ROM_FLAG.equals(arg)) {
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    loadRomCustomPath = args[++i];
                }
                continue;
            }
            if (HeadlessRunner.OUTPUT_DIR_FLAG.equals(arg)) {
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    outputDirPath = args[++i];
                }
                continue;
            }
            if (HeadlessRunner.SAVE_CONFIG_FLAG.equals(arg)) {
                saveConfigRequested = true;
                continue;
            }
            if (LOG_LEVEL_FLAG.equals(arg)) {
                if (i + 1 >= args.length) {
                    unknown.add(arg);
                } else {
                    logLevel = LogLevel.parse(args[++i]);
                }
                continue;
            }
            if (arg.startsWith("--")) {
                unknown.add(arg);
                continue;
            }
            if (scriptTests && scriptTestCase == null) {
                scriptTestCase = arg;
                continue;
            }
            unknown.add(arg);
        }

        return new CommandLineArgs(headless, loadConfigRequested, loadConfigCustomPath,
                loadRomCustomPath, outputDirPath, saveConfigRequested, scriptTests,
                logLevel, scriptTestCase, reinstallResources, cleanReinstallResources, unknown);
    }

    public boolean isHeadless() {
        return headless;
    }

    public boolean isLoadConfigRequested() {
        return loadConfigRequested;
    }

    public String getLoadConfigCustomPath() {
        return loadConfigCustomPath;
    }

    public String getLoadRomCustomPath() {
        return loadRomCustomPath;
    }

    public String getOutputDirPath() {
        return outputDirPath;
    }

    public boolean isSaveConfigRequested() {
        return saveConfigRequested;
    }

    public boolean isScriptTests() {
        return scriptTests;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public String getScriptTestCase() {
        return scriptTestCase;
    }

    public boolean isReinstallResources() {
        return reinstallResources;
    }

    public boolean isCleanReinstallResources() {
        return cleanReinstallResources;
    }

    public List<String> getUnknownArgs() {
        return unknownArgs;
    }

    public File getStartupConfigFile(AppPreferences appPreferences) {
        if (!loadConfigRequested || loadConfigCustomPath == null || loadConfigCustomPath.isBlank()) {
            return null;
        }
        return new File(loadConfigCustomPath).getAbsoluteFile();
    }

    public File getStartupRomFile(AppPreferences appPreferences) {
        if (loadRomCustomPath != null && !loadRomCustomPath.isBlank()) {
            return new File(loadRomCustomPath).getAbsoluteFile();
        }
        return LaunchUtils.resolveStartupRomFile(appPreferences);
    }
}
