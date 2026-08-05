package redactedrice.ptcgr.configs;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import redactedrice.ptcgr.randomizer.RandomizerCore;
import redactedrice.randomizer.utils.IssueTracker;
import redactedrice.randomizer.utils.LogLevel;

/**
 * Persistent UI and app defaults. Separate from randomizer Config files.
 */
public final class AppPreferences {
    public static final int CURRENT_FORMAT_VERSION = 1;
    public static final String DEFAULT_FILE_NAME = "ptcgr_app.yaml";
    private static final String HEADER = "# PTCG Randomizer app preferences\n";

    static final String FORMAT_VERSION_KEY = "version";
    static final String LOG_LEVEL_KEY = "logLevel";
    static final String LOG_DETAILS_KEY = "logDetails";
    static final String SAVE_SETTINGS_KEY = "saveSettings";
    static final String WINDOW_X_KEY = "windowX";
    static final String WINDOW_Y_KEY = "windowY";
    static final String WINDOW_WIDTH_KEY = "windowWidth";
    static final String WINDOW_HEIGHT_KEY = "windowHeight";
    static final String LAST_ROM_PATH_KEY = "lastRomPath";
    static final String OPEN_ROM_DIRECTORY_KEY = "openRomDirectory";
    static final String OPEN_ROM_FILE_NAME_KEY = "openRomFileName";
    static final String PATCH_DIRECTORY_KEY = "patchDirectory";
    static final String PATCH_FILE_NAME_KEY = "patchFileName";
    static final String SAVE_CONFIG_DIRECTORY_KEY = "saveConfigDirectory";
    static final String SAVE_CONFIG_FILE_NAME_KEY = "saveConfigFileName";
    static final String LOAD_CONFIG_DIRECTORY_KEY = "loadConfigDirectory";
    static final String LOAD_CONFIG_FILE_NAME_KEY = "loadConfigFileName";

    private int formatVersion = CURRENT_FORMAT_VERSION;
    private LogLevel logLevel = LogLevel.INFO;
    private boolean logDetails = true;
    private boolean saveSettings = true;
    private Integer windowX;
    private Integer windowY;
    private Integer windowWidth;
    private Integer windowHeight;
    private String lastRomPath;
    private String openRomDirectory;
    private String openRomFileName;
    private String patchDirectory;
    private String patchFileName;
    private String saveConfigDirectory;
    private String saveConfigFileName;
    private String loadConfigDirectory;
    private String loadConfigFileName;

    public static File defaultFile() {
        return new File(resolveAppDirectory(), DEFAULT_FILE_NAME);
    }

    static File resolveAppDirectory() {
        try {
            var codeSource = AppPreferences.class.getProtectionDomain().getCodeSource();
            if (codeSource != null && codeSource.getLocation() != null) {
                File codeLocation = new File(codeSource.getLocation().toURI());
                if (codeLocation.isFile()) {
                    File parent = codeLocation.getParentFile();
                    if (parent != null) {
                        return parent;
                    }
                }
            }
        } catch (Exception ignored) {
            // fall back to cwd below
        }
        return new File(System.getProperty("user.dir"));
    }

    public static AppPreferences loadDefaults() {
        return new AppPreferences();
    }

    public static AppPreferences load() throws IOException {
        return load(defaultFile());
    }

    public static AppPreferences load(File file) throws IOException {
        if (file == null || !file.isFile()) {
            return loadDefaults();
        }

        Map<String, Object> root = YamlIO.load(file);
        if (root == null) {
            return loadDefaults();
        }
        return readFromLoadedYamlMap(root);
    }

    public void save() throws IOException {
        save(defaultFile());
    }

    public void save(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Could not create directory: " + parent);
        }
        YamlIO.save(file, convertToYamlMap(), HEADER);
    }

    public static AppPreferences fromAppState(LogLevel logLevel, boolean logDetails,
            boolean saveSettings, int windowX, int windowY, int windowWidth, int windowHeight,
            String lastRomPath, File openRomDirectory, File openRomFile, File patchDirectory,
            File patchFile, File saveConfigDirectory, File saveConfigFile,
            File loadConfigDirectory, File loadConfigFile) {
        AppPreferences prefs = new AppPreferences();
        prefs.setLogLevel(logLevel);
        prefs.setLogDetails(logDetails);
        prefs.setSaveSettings(saveSettings);
        prefs.setWindowX(windowX);
        prefs.setWindowY(windowY);
        prefs.setWindowWidth(windowWidth);
        prefs.setWindowHeight(windowHeight);
        prefs.setLastRomPath(lastRomPath);
        prefs.setOpenRomDirectory(pathOrNull(openRomDirectory));
        prefs.setOpenRomFileName(nameOrNull(openRomFile));
        prefs.setPatchDirectory(pathOrNull(patchDirectory));
        prefs.setPatchFileName(nameOrNull(patchFile));
        prefs.setSaveConfigDirectory(pathOrNull(saveConfigDirectory));
        prefs.setSaveConfigFileName(nameOrNull(saveConfigFile));
        prefs.setLoadConfigDirectory(pathOrNull(loadConfigDirectory));
        prefs.setLoadConfigFileName(nameOrNull(loadConfigFile));
        return prefs;
    }

    public static AppPreferences readFromLoadedYamlMap(Map<String, Object> root) {
        AppPreferences prefs = loadDefaults();
        if (root == null) {
            return prefs;
        }

        prefs.formatVersion = parseFormatVersion(root.get(FORMAT_VERSION_KEY));

        prefs.logLevel = parseLogLevel(root.get(LOG_LEVEL_KEY));
        prefs.logDetails =
                ParserHelpers.parseBoolean(root.get(LOG_DETAILS_KEY), true, LOG_DETAILS_KEY,
                        "app preferences");
        prefs.saveSettings =
                ParserHelpers.parseBoolean(root.get(SAVE_SETTINGS_KEY), true, SAVE_SETTINGS_KEY,
                        "app preferences");
        prefs.windowX = ParserHelpers.parseInteger(root.get(WINDOW_X_KEY));
        prefs.windowY = ParserHelpers.parseInteger(root.get(WINDOW_Y_KEY));
        prefs.windowWidth = ParserHelpers.parseInteger(root.get(WINDOW_WIDTH_KEY));
        prefs.windowHeight = ParserHelpers.parseInteger(root.get(WINDOW_HEIGHT_KEY));
        prefs.lastRomPath = emptyToNull(ParserHelpers.parseOptionalString(root.get(LAST_ROM_PATH_KEY)));
        prefs.openRomDirectory =
                emptyToNull(ParserHelpers.parseOptionalString(root.get(OPEN_ROM_DIRECTORY_KEY)));
        prefs.openRomFileName =
                emptyToNull(ParserHelpers.parseOptionalString(root.get(OPEN_ROM_FILE_NAME_KEY)));
        prefs.patchDirectory =
                emptyToNull(ParserHelpers.parseOptionalString(root.get(PATCH_DIRECTORY_KEY)));
        prefs.patchFileName =
                emptyToNull(ParserHelpers.parseOptionalString(root.get(PATCH_FILE_NAME_KEY)));
        prefs.saveConfigDirectory = emptyToNull(
                ParserHelpers.parseOptionalString(root.get(SAVE_CONFIG_DIRECTORY_KEY)));
        prefs.saveConfigFileName =
                emptyToNull(ParserHelpers.parseOptionalString(root.get(SAVE_CONFIG_FILE_NAME_KEY)));
        prefs.loadConfigDirectory = emptyToNull(
                ParserHelpers.parseOptionalString(root.get(LOAD_CONFIG_DIRECTORY_KEY)));
        prefs.loadConfigFileName =
                emptyToNull(ParserHelpers.parseOptionalString(root.get(LOAD_CONFIG_FILE_NAME_KEY)));
        return prefs;
    }

    public Map<String, Object> convertToYamlMap() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put(FORMAT_VERSION_KEY, formatVersion);
        root.put(LOG_LEVEL_KEY, logLevel.name());
        root.put(LOG_DETAILS_KEY, logDetails);
        root.put(SAVE_SETTINGS_KEY, saveSettings);
        putIfNotNull(root, WINDOW_X_KEY, windowX);
        putIfNotNull(root, WINDOW_Y_KEY, windowY);
        putIfNotNull(root, WINDOW_WIDTH_KEY, windowWidth);
        putIfNotNull(root, WINDOW_HEIGHT_KEY, windowHeight);
        putIfNotNull(root, LAST_ROM_PATH_KEY, lastRomPath);
        putIfNotNull(root, OPEN_ROM_DIRECTORY_KEY, openRomDirectory);
        putIfNotNull(root, OPEN_ROM_FILE_NAME_KEY, openRomFileName);
        putIfNotNull(root, PATCH_DIRECTORY_KEY, patchDirectory);
        putIfNotNull(root, PATCH_FILE_NAME_KEY, patchFileName);
        putIfNotNull(root, SAVE_CONFIG_DIRECTORY_KEY, saveConfigDirectory);
        putIfNotNull(root, SAVE_CONFIG_FILE_NAME_KEY, saveConfigFileName);
        putIfNotNull(root, LOAD_CONFIG_DIRECTORY_KEY, loadConfigDirectory);
        putIfNotNull(root, LOAD_CONFIG_FILE_NAME_KEY, loadConfigFileName);
        return root;
    }

    private static int parseFormatVersion(Object value) {
        Integer version = ParserHelpers.parseInteger(value);
        if (version == null) {
            IssueTracker.addWarning(
                    "Missing or invalid version; assuming version " + CURRENT_FORMAT_VERSION + ".");
            return CURRENT_FORMAT_VERSION;
        }
        if (version > CURRENT_FORMAT_VERSION) {
            IssueTracker.addWarning("App preferences version " + version
                    + " is newer than supported version " + CURRENT_FORMAT_VERSION + ".");
        }
        return version;
    }

    private static LogLevel parseLogLevel(Object value) {
        if (value == null) {
            return LogLevel.INFO;
        }
        try {
            return LogLevel.valueOf(value.toString().trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return LogLevel.INFO;
        }
    }

    private static void putIfNotNull(Map<String, Object> root, String key, Object value) {
        if (value != null) {
            root.put(key, value);
        }
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    private static String pathOrNull(File file) {
        if (file == null) {
            return null;
        }
        if (file.isDirectory()) {
            return file.getAbsolutePath();
        }
        File parent = file.getParentFile();
        return parent != null ? parent.getAbsolutePath() : null;
    }

    private static String nameOrNull(File file) {
        return file != null ? file.getName() : null;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel != null ? logLevel : LogLevel.INFO;
    }

    public boolean isLogDetails() {
        return logDetails;
    }

    public void setLogDetails(boolean logDetails) {
        this.logDetails = logDetails;
    }

    public boolean isSaveSettings() {
        return saveSettings;
    }

    public void setSaveSettings(boolean saveSettings) {
        this.saveSettings = saveSettings;
    }

    public Integer getWindowX() {
        return windowX;
    }

    public void setWindowX(Integer windowX) {
        this.windowX = windowX;
    }

    public Integer getWindowY() {
        return windowY;
    }

    public void setWindowY(Integer windowY) {
        this.windowY = windowY;
    }

    public Integer getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(Integer windowWidth) {
        this.windowWidth = windowWidth;
    }

    public Integer getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(Integer windowHeight) {
        this.windowHeight = windowHeight;
    }

    public String getLastRomPath() {
        return lastRomPath;
    }

    public void setLastRomPath(String lastRomPath) {
        this.lastRomPath = emptyToNull(lastRomPath);
    }

    public String getOpenRomDirectory() {
        return openRomDirectory;
    }

    public void setOpenRomDirectory(String openRomDirectory) {
        this.openRomDirectory = emptyToNull(openRomDirectory);
    }

    public String getOpenRomFileName() {
        return openRomFileName;
    }

    public void setOpenRomFileName(String openRomFileName) {
        this.openRomFileName = emptyToNull(openRomFileName);
    }

    public String getPatchDirectory() {
        return patchDirectory;
    }

    public void setPatchDirectory(String patchDirectory) {
        this.patchDirectory = emptyToNull(patchDirectory);
    }

    public String getPatchFileName() {
        return patchFileName;
    }

    public void setPatchFileName(String patchFileName) {
        this.patchFileName = emptyToNull(patchFileName);
    }

    public String getSaveConfigDirectory() {
        return saveConfigDirectory;
    }

    public void setSaveConfigDirectory(String saveConfigDirectory) {
        this.saveConfigDirectory = emptyToNull(saveConfigDirectory);
    }

    public String getSaveConfigFileName() {
        return saveConfigFileName;
    }

    public void setSaveConfigFileName(String saveConfigFileName) {
        this.saveConfigFileName = emptyToNull(saveConfigFileName);
    }

    public String getLoadConfigDirectory() {
        return loadConfigDirectory;
    }

    public void setLoadConfigDirectory(String loadConfigDirectory) {
        this.loadConfigDirectory = emptyToNull(loadConfigDirectory);
    }

    public String getLoadConfigFileName() {
        return loadConfigFileName;
    }

    public void setLoadConfigFileName(String loadConfigFileName) {
        this.loadConfigFileName = emptyToNull(loadConfigFileName);
    }

    public File resolveLastRomFile() {
        if (lastRomPath != null) {
            File rom = new File(lastRomPath);
            if (rom.isFile()) {
                return rom;
            }
        }
        return null;
    }

    public File resolveOpenRomFile(String defaultRomName) {
        return resolveNamedFile(openRomDirectory, openRomFileName, defaultRomName);
    }

    public File resolvePatchFile() {
        return resolveNamedFile(patchDirectory, patchFileName, RandomizerCore.DEFAULT_PATCH_BASE_NAME);
    }

    public File resolveSaveConfigFile() {
        return resolveNamedFile(saveConfigDirectory, saveConfigFileName, YamlIO.DEFAULT_FILE_NAME);
    }

    public File resolveLoadConfigFile() {
        return resolveNamedFile(loadConfigDirectory, loadConfigFileName, YamlIO.DEFAULT_FILE_NAME);
    }

    private static File resolveNamedFile(String directoryPath, String fileName, String defaultName) {
        String name = fileName != null && !fileName.isBlank() ? fileName : defaultName;
        if (directoryPath != null && !directoryPath.isBlank()) {
            File dir = new File(directoryPath);
            if (dir.isDirectory()) {
                return new File(dir, name);
            }
        }
        return new File(name);
    }
}
