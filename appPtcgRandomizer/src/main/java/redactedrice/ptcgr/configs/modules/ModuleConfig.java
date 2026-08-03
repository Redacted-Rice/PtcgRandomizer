package redactedrice.ptcgr.configs.modules;

import java.util.LinkedHashMap;
import java.util.Map;
import redactedrice.randomizer.utils.IssueTracker;
import redactedrice.randomizer.lua.Module;

class ModuleConfig {
    static final String MODULE_KEY = "module";
    private final String module;

    static final String VERSION_KEY = "version";
    private final String version;
    static final String UNKNOWN_VERSION = "<unknown>";

    protected ModuleConfig(String module, String version) {
        this.module = module;
        this.version = version;
    }

    public Map<String, Object> convertToYamlMap() {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put(MODULE_KEY, module);
        node.put(VERSION_KEY, version != null ? version : UNKNOWN_VERSION);
        return node;
    }

    static String parseModule(Object value, String entryLabel) {
        if (!(value instanceof String moduleName) || moduleName.isBlank()) {
            IssueTracker.addWarning(entryLabel + ": missing or invalid module name.");
            return null;
        }
        return moduleName;
    }

    static String parseVersion(Object value, String entryLabel) {
        if (value == null) {
            return null;
        }
        if (value instanceof String versionText && !versionText.isBlank()) {
            return versionText;
        }
        if (value instanceof Number number) {
            return String.valueOf(number);
        }
        IssueTracker.addWarning(entryLabel + ": version must be a string or number.");
        return null;
    }

    protected void checkAndWarnModuleVersion(String entryLabel, Module module) {
        if (version == null || version.isBlank()) {
            IssueTracker.addWarning(entryLabel + " does not record a version.");
            return;
        }
        if (UNKNOWN_VERSION.equals(version)) {
            IssueTracker.addWarning(entryLabel + " was saved with an unknown version.");
            return;
        }

        String currentVersion = module.getVersion();
        if (!version.equals(currentVersion)) {
            IssueTracker.addWarning(entryLabel + " was saved as version " + version
                    + "; current version is " + currentVersion + ".");
        }
    }

    public String getModule() {
        return module;
    }

    public String getVersion() {
        return version;
    }
}
