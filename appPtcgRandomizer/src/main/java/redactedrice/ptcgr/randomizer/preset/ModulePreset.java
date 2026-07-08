package redactedrice.ptcgr.randomizer.preset;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import redactedrice.randomizer.lua.Module;

class ModulePreset {
    static final String MODULE_KEY = "module";
    static final String VERSION_KEY = "version";
    static final String UNKNOWN_VERSION = "<unknown>";

    private final String module;
    private final String version;

    protected ModulePreset(String module, String version) {
        this.module = Objects.requireNonNull(module, "module");
        this.version = version;
    }

    public String getModule() {
        return module;
    }

    public String getVersion() {
        return version;
    }

    protected Map<String, Object> prepModuleForSave() {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put(MODULE_KEY, module);
        node.put(VERSION_KEY, version != null ? version : UNKNOWN_VERSION);
        return node;
    }

    static String parseModule(Object value, List<String> warnings, String entryLabel) {
        if (!(value instanceof String moduleName) || moduleName.isBlank()) {
            warnings.add(entryLabel + ": missing or invalid module name.");
            return null;
        }
        return moduleName;
    }

    static String parseVersion(Object value, List<String> warnings, String entryLabel) {
        if (value == null) {
            return null;
        }
        if (value instanceof String versionText && !versionText.isBlank()) {
            return versionText;
        }
        if (value instanceof Number number) {
            return String.valueOf(number);
        }
        warnings.add(entryLabel + ": version must be a string or number.");
        return null;
    }

    protected void checkAndWarnModuleVersion(String entryLabel, Module module,
            List<String> warnings) {
        if (version == null || version.isBlank()) {
            warnings.add(entryLabel + " does not record a version.");
            return;
        }
        if (UNKNOWN_VERSION.equals(version)) {
            warnings.add(entryLabel + " was saved with an unknown version.");
            return;
        }

        String currentVersion = module.getVersion();
        if (!version.equals(currentVersion)) {
            warnings.add(entryLabel + " was saved as version " + version + "; current version is "
                    + currentVersion + ".");
        }
    }
}
