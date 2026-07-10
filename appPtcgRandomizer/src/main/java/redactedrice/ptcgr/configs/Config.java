package redactedrice.ptcgr.configs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import redactedrice.ptcgr.configs.modules.ActionConfig;
import redactedrice.ptcgr.configs.modules.ScriptConfig;
import redactedrice.ptcgr.configs.rules.RulesConfig;
import redactedrice.ptcgr.constants.PtcgRandomizerVersion;
import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.ptcgr.utils.WarningCollector;
import redactedrice.randomizer.lua.Module;

/**
 * In memory representation of saved randomizer settings. Used for both saving and loading.
 */
public final class Config {
    public static final int CURRENT_FORMAT_VERSION = 1;

    static final String FORMAT_VERSION_KEY = "version";
    private final int formatVersion;

    static final String APP_VERSION_KEY = "appVersion";
    private final String appVersion;

    static final String SEED_KEY = "seed";
    private final String seed;

    static final String ACTIONS_KEY = "actions";
    private final List<ActionConfig> actionConfigs;

    static final String PRESCRIPTS_KEY = "prescripts";
    private final List<ScriptConfig> preScriptConfigs;

    static final String POSTSCRIPTS_KEY = "postscripts";
    private final List<ScriptConfig> postScriptConfigs;

    static final String RULES_KEY = "rules";
    private final RulesConfig rulesConfig;

    public Config(String seed, List<ActionConfig> actionConfigs,
            List<ScriptConfig> preScriptConfigs, List<ScriptConfig> postScriptConfigs,
            RulesConfig rulesConfig) {
        this(CURRENT_FORMAT_VERSION, PtcgRandomizerVersion.VERSION, seed, actionConfigs,
                preScriptConfigs, postScriptConfigs, rulesConfig);
    }

    public Config(int formatVersion, String appVersion, String seed,
            List<ActionConfig> actionConfigs, List<ScriptConfig> preScriptConfigs,
            List<ScriptConfig> postScriptConfigs, RulesConfig rulesConfig) {
        this.formatVersion = formatVersion;
        this.appVersion = appVersion;
        this.seed = Objects.requireNonNull(seed, "seed");
        this.actionConfigs = List.copyOf(actionConfigs);
        this.preScriptConfigs = List.copyOf(preScriptConfigs);
        this.postScriptConfigs = List.copyOf(postScriptConfigs);
        this.rulesConfig = Objects.requireNonNull(rulesConfig, "rulesConfig");
    }

    private static List<ActionConfig> convertActions(List<Action> actions) {
        List<ActionConfig> configs = new ArrayList<>();
        for (Action action : actions) {
            configs.add(ActionConfig.fromAction(action));
        }
        return configs;
    }

    private static List<ScriptConfig> convertScripts(List<Module> scripts) {
        List<ScriptConfig> configs = new ArrayList<>();
        for (Module script : scripts) {
            configs.add(ScriptConfig.fromModule(script));
        }
        return configs;
    }

    public static Config empty() {
        return new Config("Random", List.of(), List.of(), List.of(), RulesConfig.empty());
    }

    public static Config fromAppState(String seed, List<Action> actions, ActionBank actionBank,
            RulesConfig rulesConfig) {
        return new Config(CURRENT_FORMAT_VERSION, PtcgRandomizerVersion.VERSION, seed,
                convertActions(actions), convertScripts(actionBank.getPreScripts()),
                convertScripts(actionBank.getPostScripts()), rulesConfig);
    }

    public static Config readFromLoadedYamlMap(Map<String, Object> root, String sourceLabel,
            WarningCollector warnings) {
        if (root == null) {
            warnings.addWarning(sourceLabel + ": config file is empty.");
            return empty();
        }

        int formatVersion = parseFormatVersion(root.get(FORMAT_VERSION_KEY), warnings);
        String appVersion = parseAppVersion(root.get(APP_VERSION_KEY), warnings);
        String seed = parseSeed(root.get(SEED_KEY), warnings);
        List<ActionConfig> actions = parseActions(root.get(ACTIONS_KEY), warnings);
        List<ScriptConfig> preScripts =
                parseScripts(root.get(PRESCRIPTS_KEY), PRESCRIPTS_KEY, warnings);
        List<ScriptConfig> postScripts =
                parseScripts(root.get(POSTSCRIPTS_KEY), POSTSCRIPTS_KEY, warnings);
        RulesConfig rules = parseRules(root.get(RULES_KEY), sourceLabel, warnings);
        return new Config(formatVersion, appVersion, seed, actions, preScripts, postScripts, rules);
    }

    private static List<Map<String, Object>> convertToYamlMapActions(
            List<ActionConfig> actionConfigs) {
        List<Map<String, Object>> actionNodes = new ArrayList<>();
        for (ActionConfig actionConfig : actionConfigs) {
            actionNodes.add(actionConfig.convertToYamlMap());
        }
        return actionNodes;
    }

    private static List<Map<String, Object>> convertToYamlMapScripts(
            List<ScriptConfig> scriptConfigs) {
        List<Map<String, Object>> scriptNodes = new ArrayList<>();
        for (ScriptConfig script : scriptConfigs) {
            scriptNodes.add(script.convertToYamlMap());
        }
        return scriptNodes;
    }

    public Map<String, Object> convertToYamlMap() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put(FORMAT_VERSION_KEY, formatVersion);
        if (appVersion != null) {
            root.put(APP_VERSION_KEY, appVersion);
        }
        root.put(SEED_KEY, seed);
        root.put(ACTIONS_KEY, convertToYamlMapActions(actionConfigs));
        root.put(PRESCRIPTS_KEY, convertToYamlMapScripts(preScriptConfigs));
        root.put(POSTSCRIPTS_KEY, convertToYamlMapScripts(postScriptConfigs));
        root.put(RULES_KEY, rulesConfig.convertToYamlMap());
        return root;
    }

    public int getFormatVersion() {
        return formatVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getSeed() {
        return seed;
    }

    public List<ActionConfig> getActionConfigs() {
        return actionConfigs;
    }

    public List<ScriptConfig> getPreScriptConfigs() {
        return preScriptConfigs;
    }

    public List<ScriptConfig> getPostScriptConfigs() {
        return postScriptConfigs;
    }

    public RulesConfig getRulesConfig() {
        return rulesConfig;
    }

    public List<Action> getActions(ActionBank actionBank, WarningCollector warnings) {
        List<Action> actions = new ArrayList<>();
        for (ActionConfig actionConfig : getActionConfigs()) {
            Action action = actionConfig.toAction(actionBank, warnings);
            if (action != null) {
                actions.add(action);
            }
        }
        return actions;
    }

    public void checkScripts(ActionBank actionBank, WarningCollector warnings) {
        ScriptConfig.checkAndWarnDifferences(PRESCRIPTS_KEY, preScriptConfigs,
                actionBank.getPreScripts(), actionBank, warnings);
        ScriptConfig.checkAndWarnDifferences(POSTSCRIPTS_KEY, postScriptConfigs,
                actionBank.getPostScripts(), actionBank, warnings);
    }

    private static int parseFormatVersion(Object value, WarningCollector warnings) {
        Integer version = ParserHelpers.parseInteger(value);
        if (version == null) {
            warnings.addWarning(
                    "Missing or invalid version; assuming version " + CURRENT_FORMAT_VERSION + ".");
            return CURRENT_FORMAT_VERSION;
        }
        if (version > CURRENT_FORMAT_VERSION) {
            warnings.addWarning("Config version " + version + " is newer than supported version "
                    + CURRENT_FORMAT_VERSION + ".");
        }
        return version;
    }

    private static void checkAndWarnAppVersionMismatch(String savedAppVersion,
            WarningCollector warnings) {
        if (savedAppVersion == null || savedAppVersion.isBlank()) {
            warnings.addWarning("Config is missing an appVersion.");
            return;
        }
        if (!savedAppVersion.equals(PtcgRandomizerVersion.VERSION)) {
            warnings.addWarning("Config was saved with PtcgRandomizer " + savedAppVersion
                    + "; current version is " + PtcgRandomizerVersion.VERSION + ".");
        }
    }

    private static String parseAppVersion(Object value, WarningCollector warnings) {
        if (value == null) {
            warnings.addWarning("Config is missing an appVersion.");
            return null;
        }
        if (value instanceof String appVersionText) {
            if (appVersionText.isBlank()) {
                warnings.addWarning("appVersion cannot be blank.");
                return null;
            }
            checkAndWarnAppVersionMismatch(appVersionText, warnings);
            return appVersionText;
        }
        if (value instanceof Number number) {
            String appVersion = String.valueOf(number);
            checkAndWarnAppVersionMismatch(appVersion, warnings);
            return appVersion;
        }
        warnings.addWarning("appVersion must be a string or number.");
        return null;
    }

    private static String parseSeed(Object value, WarningCollector warnings) {
        if (value == null) {
            warnings.addWarning("Config is missing seed; \"Random\" will be used.");
            return "Random";
        }
        if (value instanceof String seedText) {
            if (seedText.isBlank()) {
                warnings.addWarning("Config seed cannot be blank; \"Random\" will be used.");
                return "Random";
            }
            return seedText;
        }
        if (value instanceof Number number) {
            return String.valueOf(number.longValue());
        }
        warnings.addWarning("Config seed must be a string or number; \"Random\" will be used.");
        return "Random";
    }

    private static List<ActionConfig> parseActions(Object value, WarningCollector warnings) {
        if (value == null) {
            warnings.addWarning("Config is missing actions; ignoring actions.");
            return List.of();
        }
        if (!(value instanceof List<?> actionNodes)) {
            warnings.addWarning("Actions must be a list; ignoring actions.");
            return List.of();
        }

        List<ActionConfig> actions = new ArrayList<>();
        for (int i = 0; i < actionNodes.size(); i++) {
            Object actionNode = actionNodes.get(i);
            String entryLabel = ACTIONS_KEY + "[" + i + "]";
            if (!(actionNode instanceof Map<?, ?> actionMap)) {
                warnings.addWarning(entryLabel + ": action entry must be a mapping.");
                continue;
            }
            @SuppressWarnings("unchecked")
            ActionConfig parsed = ActionConfig
                    .readFromLoadedYamlMap((Map<String, Object>) actionMap, warnings, entryLabel);
            if (parsed != null) {
                actions.add(parsed);
            }
        }
        return actions;
    }

    private static List<ScriptConfig> parseScripts(Object value, String sectionKey,
            WarningCollector warnings) {
        if (value == null) {
            warnings.addWarning(
                    "Config is missing " + sectionKey + "; ignoring " + sectionKey + ".");
            return List.of();
        }
        if (!(value instanceof List<?> scriptNodes)) {
            warnings.addWarning(sectionKey + " must be a list; ignoring " + sectionKey + ".");
            return List.of();
        }

        List<ScriptConfig> scripts = new ArrayList<>();
        for (int i = 0; i < scriptNodes.size(); i++) {
            Object scriptNode = scriptNodes.get(i);
            String entryLabel = sectionKey + "[" + i + "]";
            if (!(scriptNode instanceof Map<?, ?> scriptMap)) {
                warnings.addWarning(entryLabel + ": script entry must be a mapping.");
                continue;
            }
            @SuppressWarnings("unchecked")
            ScriptConfig parsed = ScriptConfig
                    .readFromLoadedYamlMap((Map<String, Object>) scriptMap, warnings, entryLabel);
            if (parsed != null) {
                scripts.add(parsed);
            }
        }
        return scripts;
    }

    private static RulesConfig parseRules(Object value, String sourceLabel,
            WarningCollector warnings) {
        if (value == null) {
            return RulesConfig.empty();
        }
        if (!(value instanceof Map<?, ?> rulesMap)) {
            warnings.addWarning("Rules must be a mapping; using empty rules.");
            return RulesConfig.empty();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> typedRules = (Map<String, Object>) rulesMap;
        return RulesConfig.readFromLoadedYamlMap(typedRules, sourceLabel, warnings);
    }
}
