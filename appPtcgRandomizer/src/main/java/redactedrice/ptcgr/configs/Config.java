package redactedrice.ptcgr.configs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import redactedrice.ptcgr.configs.modules.ActionConfig;
import redactedrice.ptcgr.configs.modules.ScriptConfig;
import redactedrice.ptcgr.configs.rules.RulesConfig;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.rules.Rules;
import redactedrice.ptcgr.constants.PtcgRandomizerVersion;
import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.randomizer.utils.IssueTracker;
import redactedrice.randomizer.lua.Module;

/** App config for save and load. Load only applies sections that were in the file. */
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

    private final boolean metadataValid;
    private final boolean seedLoaded;
    private final boolean actionsLoaded;
    private final boolean preScriptsLoaded;
    private final boolean postScriptsLoaded;
    private final boolean rulesLoaded;

    public Config(String seed, List<ActionConfig> actionConfigs,
            List<ScriptConfig> preScriptConfigs, List<ScriptConfig> postScriptConfigs,
            RulesConfig rulesConfig) {
        this(CURRENT_FORMAT_VERSION, PtcgRandomizerVersion.VERSION, seed, actionConfigs,
                preScriptConfigs, postScriptConfigs, rulesConfig);
    }

    public Config(int formatVersion, String appVersion, String seed,
            List<ActionConfig> actionConfigs, List<ScriptConfig> preScriptConfigs,
            List<ScriptConfig> postScriptConfigs, RulesConfig rulesConfig) {
        this(formatVersion, appVersion, seed, actionConfigs, preScriptConfigs, postScriptConfigs,
                rulesConfig, true, true, true, true, true, true);
    }

    private Config(int formatVersion, String appVersion, String seed,
            List<ActionConfig> actionConfigs, List<ScriptConfig> preScriptConfigs,
            List<ScriptConfig> postScriptConfigs, RulesConfig rulesConfig, boolean metadataValid,
            boolean seedLoaded, boolean actionsLoaded, boolean preScriptsLoaded,
            boolean postScriptsLoaded, boolean rulesLoaded) {
        this.formatVersion = formatVersion;
        this.appVersion = appVersion;
        this.seed = Objects.requireNonNull(seed, "seed");
        this.actionConfigs = List.copyOf(actionConfigs);
        this.preScriptConfigs = List.copyOf(preScriptConfigs);
        this.postScriptConfigs = List.copyOf(postScriptConfigs);
        this.rulesConfig = Objects.requireNonNull(rulesConfig, "rulesConfig");
        this.metadataValid = metadataValid;
        this.seedLoaded = seedLoaded;
        this.actionsLoaded = actionsLoaded;
        this.preScriptsLoaded = preScriptsLoaded;
        this.postScriptsLoaded = postScriptsLoaded;
        this.rulesLoaded = rulesLoaded;
    }

    public static Config readFromLoadedYamlMap(Map<String, Object> root, String sourceLabel) {
        if (root == null || root.isEmpty()) {
            IssueTracker.addWarning(sourceLabel + ": file is empty.");
            return invalidLoad();
        }

        if (!root.containsKey(FORMAT_VERSION_KEY)) {
            IssueTracker.addWarning(
                    sourceLabel + ": missing required field \"" + FORMAT_VERSION_KEY + "\".");
            return invalidLoad();
        }

        if (!root.containsKey(APP_VERSION_KEY)) {
            IssueTracker.addWarning(
                    sourceLabel + ": missing required field \"" + APP_VERSION_KEY + "\".");
            return invalidLoad();
        }

        int formatVersion = parseFormatVersion(root.get(FORMAT_VERSION_KEY), sourceLabel);
        String appVersion = parseAppVersion(root.get(APP_VERSION_KEY), sourceLabel);
        if (appVersion == null) {
            return invalidLoad();
        }

        boolean seedLoaded = root.containsKey(SEED_KEY);
        boolean actionsLoaded = root.containsKey(ACTIONS_KEY);
        boolean preScriptsLoaded = root.containsKey(PRESCRIPTS_KEY);
        boolean postScriptsLoaded = root.containsKey(POSTSCRIPTS_KEY);
        boolean rulesLoaded = root.containsKey(RULES_KEY);

        String seed = seedLoaded
                ? parseSeedValue(root.get(SEED_KEY), sourceLabel)
                : "Random";
        List<ActionConfig> actions = actionsLoaded
                ? parseActions(root.get(ACTIONS_KEY), sourceLabel)
                : List.of();
        List<ScriptConfig> preScripts = preScriptsLoaded
                ? parseScripts(root.get(PRESCRIPTS_KEY), PRESCRIPTS_KEY, sourceLabel)
                : List.of();
        List<ScriptConfig> postScripts = postScriptsLoaded
                ? parseScripts(root.get(POSTSCRIPTS_KEY), POSTSCRIPTS_KEY, sourceLabel)
                : List.of();
        RulesConfig rules = rulesLoaded
                ? parseRules(root.get(RULES_KEY), sourceLabel)
                : RulesConfig.empty();

        return new Config(formatVersion, appVersion, seed, actions, preScripts, postScripts, rules,
                true, seedLoaded, actionsLoaded, preScriptsLoaded, postScriptsLoaded, rulesLoaded);
    }

    private static Config invalidLoad() {
        return new Config(CURRENT_FORMAT_VERSION, null, "Random", List.of(), List.of(), List.of(),
                RulesConfig.empty(), false, false, false, false, false, false);
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
            Rules rules, CardGroup<MonsterCard> cards) {
        RulesConfig rulesConfig = RulesConfig.fromRules(rules, cards);
        return new Config(CURRENT_FORMAT_VERSION, PtcgRandomizerVersion.VERSION, seed,
                convertActions(actions), convertScripts(actionBank.getPreScripts()),
                convertScripts(actionBank.getPostScripts()), rulesConfig);
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

    public static Map<String, Object> convertRulesOnlyToYamlMap(RulesConfig rulesConfig) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put(FORMAT_VERSION_KEY, CURRENT_FORMAT_VERSION);
        root.put(APP_VERSION_KEY, PtcgRandomizerVersion.VERSION);
        root.put(RULES_KEY, rulesConfig.convertToYamlMap());
        return root;
    }

    public boolean isValid() {
        return metadataValid;
    }

    public boolean hasSeed() {
        return seedLoaded;
    }

    public boolean hasActions() {
        return actionsLoaded;
    }

    public boolean hasPreScripts() {
        return preScriptsLoaded;
    }

    public boolean hasPostScripts() {
        return postScriptsLoaded;
    }

    public boolean hasRules() {
        return rulesLoaded && rulesConfig.hasAnySection();
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

    public List<Action> getActions(ActionBank actionBank) {
        List<Action> actions = new ArrayList<>();
        for (ActionConfig actionConfig : getActionConfigs()) {
            Action action = actionConfig.toAction(actionBank);
            if (action != null) {
                actions.add(action);
            }
        }
        return actions;
    }

    public void checkScripts(ActionBank actionBank) {
        if (preScriptsLoaded) {
            ScriptConfig.checkAndWarnDifferences(PRESCRIPTS_KEY, preScriptConfigs,
                    actionBank.getPreScripts(), actionBank);
        }
        if (postScriptsLoaded) {
            ScriptConfig.checkAndWarnDifferences(POSTSCRIPTS_KEY, postScriptConfigs,
                    actionBank.getPostScripts(), actionBank);
        }
    }

    private static int parseFormatVersion(Object value, String sourceLabel) {
        Integer version = ParserHelpers.parseInteger(value);
        if (version == null) {
            IssueTracker.addWarning(sourceLabel + ": invalid \"" + FORMAT_VERSION_KEY
                    + "\"; assuming version " + CURRENT_FORMAT_VERSION + ".");
            return CURRENT_FORMAT_VERSION;
        }
        if (version > CURRENT_FORMAT_VERSION) {
            IssueTracker.addWarning(sourceLabel + ": version " + version
                    + " is newer than supported version " + CURRENT_FORMAT_VERSION + ".");
        }
        return version;
    }

    private static String parseAppVersion(Object value, String sourceLabel) {
        if (value == null) {
            IssueTracker.addWarning(
                    sourceLabel + ": missing required field \"" + APP_VERSION_KEY + "\".");
            return null;
        }
        if (value instanceof String appVersionText) {
            if (appVersionText.isBlank()) {
                IssueTracker.addWarning(
                        sourceLabel + ": required field \"" + APP_VERSION_KEY + "\" is empty.");
                return null;
            }
            checkAndWarnAppVersionMismatch(appVersionText, sourceLabel);
            return appVersionText;
        }
        if (value instanceof Number number) {
            String parsed = String.valueOf(number);
            checkAndWarnAppVersionMismatch(parsed, sourceLabel);
            return parsed;
        }
        IssueTracker.addWarning(
                sourceLabel + ": \"" + APP_VERSION_KEY + "\" must be a string or number.");
        return null;
    }

    private static void checkAndWarnAppVersionMismatch(String savedAppVersion, String sourceLabel) {
        if (!savedAppVersion.equals(PtcgRandomizerVersion.VERSION)) {
            IssueTracker.addWarning(sourceLabel + ": saved with PtcgRandomizer " + savedAppVersion
                    + "; current version is " + PtcgRandomizerVersion.VERSION + ".");
        }
    }

    private static String parseSeedValue(Object value, String sourceLabel) {
        if (value == null) {
            return "Random";
        }
        if (value instanceof String seedText) {
            if (seedText.isBlank()) {
                IssueTracker.addWarning(
                        sourceLabel + ": seed cannot be blank; \"Random\" will be used.");
                return "Random";
            }
            return seedText;
        }
        if (value instanceof Number number) {
            return String.valueOf(number.longValue());
        }
        IssueTracker.addWarning(
                sourceLabel + ": seed must be a string or number; \"Random\" will be used.");
        return "Random";
    }

    private static List<ActionConfig> parseActions(Object value, String sourceLabel) {
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?> actionNodes)) {
            IssueTracker.addWarning(sourceLabel + ": \"" + ACTIONS_KEY + "\" must be a list.");
            return List.of();
        }

        List<ActionConfig> actions = new ArrayList<>();
        for (int i = 0; i < actionNodes.size(); i++) {
            Object actionNode = actionNodes.get(i);
            String entryLabel = ACTIONS_KEY + "[" + i + "]";
            if (!(actionNode instanceof Map<?, ?> actionMap)) {
                IssueTracker.addWarning(entryLabel + ": action entry must be a mapping.");
                continue;
            }
            @SuppressWarnings("unchecked")
            ActionConfig parsed = ActionConfig
                    .readFromLoadedYamlMap((Map<String, Object>) actionMap, entryLabel);
            if (parsed != null) {
                actions.add(parsed);
            }
        }
        return actions;
    }

    private static List<ScriptConfig> parseScripts(Object value, String sectionKey,
            String sourceLabel) {
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?> scriptNodes)) {
            IssueTracker.addWarning(sourceLabel + ": \"" + sectionKey + "\" must be a list.");
            return List.of();
        }

        List<ScriptConfig> scripts = new ArrayList<>();
        for (int i = 0; i < scriptNodes.size(); i++) {
            Object scriptNode = scriptNodes.get(i);
            String entryLabel = sectionKey + "[" + i + "]";
            if (!(scriptNode instanceof Map<?, ?> scriptMap)) {
                IssueTracker.addWarning(entryLabel + ": script entry must be a mapping.");
                continue;
            }
            @SuppressWarnings("unchecked")
            ScriptConfig parsed = ScriptConfig
                    .readFromLoadedYamlMap((Map<String, Object>) scriptMap, entryLabel);
            if (parsed != null) {
                scripts.add(parsed);
            }
        }
        return scripts;
    }

    private static RulesConfig parseRules(Object value, String sourceLabel) {
        if (value == null) {
            return RulesConfig.empty();
        }
        if (!(value instanceof Map<?, ?> rulesMap)) {
            IssueTracker.addWarning(sourceLabel + ": \"" + RULES_KEY + "\" must be a mapping.");
            return RulesConfig.empty();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> typedRules = (Map<String, Object>) rulesMap;
        return RulesConfig.readFromLoadedYamlMap(typedRules, sourceLabel);
    }
}
