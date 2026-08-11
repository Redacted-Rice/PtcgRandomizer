package redactedrice.ptcgr.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.yaml.snakeyaml.Yaml;

import redactedrice.ptcgr.configs.modules.ActionArgumentsConfig;
import redactedrice.ptcgr.configs.modules.ActionConfig;
import redactedrice.ptcgr.configs.modules.ScriptConfig;
import redactedrice.ptcgr.configs.rules.MoveExclusionConfig;
import redactedrice.ptcgr.configs.rules.RulesConfig;
import redactedrice.ptcgr.constants.romenums.CardId;
import redactedrice.ptcgr.constants.PtcgRandomizerVersion;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.data.Move;
import redactedrice.ptcgr.rules.Rules;
import redactedrice.ptcgr.randomizer.Settings;
import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.randomizer.utils.IssueTracker;
import redactedrice.randomizer.lua.Module;
import redactedrice.randomizer.lua.arguments.ArgumentConstraint;
import redactedrice.randomizer.lua.arguments.ArgumentDefinition;
import redactedrice.randomizer.lua.arguments.TypeDefinition;

class ConfigTest {
    @TempDir
    Path tempDir;

    @Test
    void presetDocumentIncludesAppVersionSeedActionsAndScripts() {
        Config config = new Config("123456789", Collections.emptyList(), List.of(), List.of(),
                RulesConfig.empty());
        Map<String, Object> document = config.convertToYamlMap();
        assertEquals(Config.CURRENT_FORMAT_VERSION, config.getFormatVersion());
        assertEquals(Config.CURRENT_FORMAT_VERSION, document.get("version"));
        assertEquals(PtcgRandomizerVersion.VERSION, document.get("appVersion"));
        assertEquals("123456789", document.get("seed"));
        assertEquals(Collections.emptyList(), document.get("actions"));
        assertFalse(document.containsKey("prescripts"));
        assertFalse(document.containsKey("postscripts"));
        assertEquals(RulesConfig.empty().convertToYamlMap(), document.get("rules"));
        assertFalse(document.containsKey("randomizationSettings"));
    }

    @Test
    void saveWritesResolvedSeedAppVersionAndScripts() throws Exception {
        Settings settings = new Settings();
        settings.setSeed("random");

        var setupScript = scriptWithVersion("changedetector_setup", "0.1", "randomize");
        var detectScript = scriptWithVersion("changedetector_detect", "0.1", "module");
        ActionBank actionBank =
                testActionBank("shuffle_hp", "0.9", List.of(), List.of(setupScript), List.of(detectScript));
        Action action = actionBank.getModule("shuffle_hp") != null
                ? new Action(actionBank.getModule("shuffle_hp"), actionBank.getEnumRegistry())
                : null;
        assertTrue(action != null);
        Config config = Config.fromAppState(settings.getSeedString(), List.of(action), actionBank,
                new Rules(), null);
        Path output = tempDir.resolve("config.yaml");
        YamlIO.save(output.toFile(), config.convertToYamlMap());

        @SuppressWarnings("unchecked")
        Map<String, Object> loaded = new Yaml().load(Files.readString(output));
        assertEquals(PtcgRandomizerVersion.VERSION, loaded.get("appVersion"));
        assertFalse(loaded.get("seed").equals("random"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> preScripts = (List<Map<String, Object>>) loaded.get("prescripts");
        assertEquals(1, preScripts.size());
        assertEquals("changedetector_setup", preScripts.get(0).get("module"));
        assertEquals("0.1", preScripts.get(0).get("version"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> postScripts =
                (List<Map<String, Object>>) loaded.get("postscripts");
        assertEquals(1, postScripts.size());
        assertEquals("changedetector_detect", postScripts.get(0).get("module"));
    }

    @Test
    void loadRestoresSeedAppVersionActionsVersionAndSeedOffset() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                seed: 987654321
                actions:
                  - module: shuffle_hp
                    version: 0.1
                    seedOffset: 12
                prescripts: []
                postscripts: []
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        IssueTracker.clear();
        Config loaded = readYaml(configFile.toFile());
        assertTrue(!IssueTracker.hasWarnings());
        assertEquals("987654321", loaded.getSeed());
        assertEquals(PtcgRandomizerVersion.VERSION, loaded.getAppVersion());
        assertEquals(1, loaded.getActionConfigs().size());
        assertEquals("shuffle_hp", loaded.getActionConfigs().get(0).getModule());
        assertEquals("0.1", loaded.getActionConfigs().get(0).getVersion());
        assertEquals(12, loaded.getActionConfigs().get(0).getConfig().getSeedOffset());
    }

    @Test
    void loadRestoresModuleArguments() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                seed: 1
                actions:
                  - module: set_num_moves
                    arguments:
                      numMoves: 1
                prescripts: []
                postscripts: []
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        IssueTracker.clear();
        Config loaded = readYaml(configFile.toFile());
        assertEquals(1, loaded.getActionConfigs().size());
        assertEquals(1, loaded.getActionConfigs().get(0).getConfig().getArguments().get("numMoves"));

        ActionBank actionBank = testActionBank("set_num_moves", "0.9",
                List.of(new ArgumentDefinition("numMoves", TypeDefinition.integer(), 2)),
                List.of(), List.of());
        var actions = loaded.getActions(actionBank);
        assertEquals(1, actions.size());
        assertEquals(1, actions.get(0).getArgument("numMoves"));
    }

    @Test
    void loadCoercesStringYamlValueToModuleArgumentType() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                seed: 1
                actions:
                  - module: set_num_moves
                    arguments:
                      numMoves: "1"
                prescripts: []
                postscripts: []
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        IssueTracker.clear();
        Config loaded = readYaml(configFile.toFile());
        ActionBank actionBank = testActionBank("set_num_moves", "0.9",
                List.of(new ArgumentDefinition("numMoves",
                        TypeDefinition.integer(ArgumentConstraint.range(0, 2)), 2)),
                List.of(), List.of());
        var actions = loaded.getActions(actionBank);

        assertEquals(1, actions.size());
        assertEquals(1, actions.get(0).getArgument("numMoves"));
    }

    @Test
    void loadWarnsAndKeepsDefaultForInvalidModuleArgumentValue() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                seed: 1
                actions:
                  - module: set_num_moves
                    arguments:
                      numMoves: "not-a-number"
                prescripts: []
                postscripts: []
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        IssueTracker.clear();
        Config loaded = readYaml(configFile.toFile());
        ActionBank actionBank = testActionBank("set_num_moves", "0.9",
                List.of(new ArgumentDefinition("numMoves",
                        TypeDefinition.integer(ArgumentConstraint.range(0, 2)), 2)),
                List.of(), List.of());
        var actions = loaded.getActions(actionBank);

        assertEquals(1, actions.size());
        assertEquals(2, actions.get(0).getArgument("numMoves"));
        assertTrue(IssueTracker.getWarnings().stream()
                .anyMatch(w -> w.contains("numMoves") && w.contains("invalid value")
                        && w.contains("using default value (2)")));
    }

    @Test
    void loadWarnsWithEmptyCollectionFallbackForInvalidListArgument() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                seed: 1
                actions:
                  - module: tag_module
                    arguments:
                      tags: "not-a-list"
                prescripts: []
                postscripts: []
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        IssueTracker.clear();
        Config loaded = readYaml(configFile.toFile());
        ActionBank actionBank = testActionBank("tag_module", "0.9",
                List.of(new ArgumentDefinition("tags",
                        TypeDefinition.listOf(TypeDefinition.string()), null)),
                List.of(), List.of());
        var actions = loaded.getActions(actionBank);

        assertEquals(1, actions.size());
        assertTrue(((List<?>) actions.get(0).getArgument("tags")).isEmpty());
        assertTrue(IssueTracker.getWarnings().stream()
                .anyMatch(w -> w.contains("tags") && w.contains("invalid value")
                        && w.contains("using default value ([])")));
    }

    @Test
    void loadWarnsWithEmptyCollectionFallbackForMissingListArgument() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                seed: 1
                actions:
                  - module: tag_module
                    arguments: {}
                prescripts: []
                postscripts: []
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        IssueTracker.clear();
        Config loaded = readYaml(configFile.toFile());
        ActionBank actionBank = testActionBank("tag_module", "0.9",
                List.of(new ArgumentDefinition("tags",
                        TypeDefinition.listOf(TypeDefinition.string()), null)),
                List.of(), List.of());
        var actions = loaded.getActions(actionBank);

        assertEquals(1, actions.size());
        assertTrue(((List<?>) actions.get(0).getArgument("tags")).isEmpty());
        assertTrue(IssueTracker.getWarnings().stream()
                .anyMatch(w -> w.contains("tags") && w.contains("not specified")
                        && w.contains("using default value ([])")));
    }

    @Test
    void loadWarnsOnUnknownModuleArgument() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                seed: 1
                actions:
                  - module: set_num_moves
                    arguments:
                      numMoves: 1
                      bogusArg: 5
                prescripts: []
                postscripts: []
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        IssueTracker.clear();
        Config loaded = readYaml(configFile.toFile());
        ActionBank actionBank = testActionBank("set_num_moves", "0.9",
                List.of(new ArgumentDefinition("numMoves", TypeDefinition.integer(), 2)),
                List.of(), List.of());
        var actions = loaded.getActions(actionBank);

        assertEquals(1, actions.size());
        assertEquals(1, actions.get(0).getArgument("numMoves"));
        assertTrue(IssueTracker.getWarnings().stream()
                .anyMatch(w -> w.contains("bogusArg") && w.contains("ignoring")));
    }

    @Test
    void loadWarnsOnMissingModuleArgument() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                seed: 1
                actions:
                  - module: set_num_moves
                    arguments: {}
                prescripts: []
                postscripts: []
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        IssueTracker.clear();
        Config loaded = readYaml(configFile.toFile());
        ActionBank actionBank = testActionBank("set_num_moves", "0.9",
                List.of(new ArgumentDefinition("numMoves", TypeDefinition.integer(), 2)),
                List.of(), List.of());
        var actions = loaded.getActions(actionBank);

        assertEquals(1, actions.size());
        assertEquals(2, actions.get(0).getArgument("numMoves"));
        assertTrue(IssueTracker.getWarnings().stream()
                .anyMatch(w -> w.contains("numMoves") && w.contains("not specified")
                        && w.contains("using default value (2)")));
    }

    @Test
    void loadIgnoresScriptsWhenActionsListIsEmpty() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                seed: 1
                actions: []
                prescripts:
                  - module: changedetector_setup
                    version: 0.1
                postscripts:
                  - module: changedetector_detect
                    version: 0.1
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        IssueTracker.clear();
        Config loaded = readYaml(configFile.toFile());
        assertTrue(IssueTracker.getWarnings().stream()
                .anyMatch(w -> w.contains("prescripts and postscripts were ignored")));
        assertTrue(loaded.getPreScriptConfigs().isEmpty());
        assertTrue(loaded.getPostScriptConfigs().isEmpty());
        assertFalse(loaded.hasPreScripts());
        assertFalse(loaded.hasPostScripts());
    }

    @Test
    void loadRestoresSeedActionsAndSeedOffsetWithoutModuleVersion() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                seed: 987654321
                actions:
                  - module: shuffle_hp
                    seedOffset: 12
                prescripts: []
                postscripts: []
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        IssueTracker.clear();
        Config loaded = readYaml(configFile.toFile());
        assertTrue(!IssueTracker.hasWarnings());
        assertEquals("987654321", loaded.getSeed());
        assertEquals(1, loaded.getActionConfigs().size());
        assertEquals("shuffle_hp", loaded.getActionConfigs().get(0).getModule());
        assertEquals(null, loaded.getActionConfigs().get(0).getVersion());
        assertEquals(12, loaded.getActionConfigs().get(0).getConfig().getSeedOffset());
    }

    @Test
    void fromAppStateOmitsScriptSectionsWhenNoActions() {
        var setupScript = scriptWithVersion("changedetector_setup", "0.1", "randomize");
        ActionBank actionBank =
                testActionBank(null, null, List.of(), List.of(setupScript), List.of());

        Config config = Config.fromAppState("42", List.of(), actionBank, new Rules(), null);
        Map<String, Object> document = config.convertToYamlMap();

        assertTrue(config.getPreScriptConfigs().isEmpty());
        assertTrue(config.getPostScriptConfigs().isEmpty());
        assertFalse(document.containsKey("prescripts"));
        assertFalse(document.containsKey("postscripts"));
    }

    @Test
    void scriptsOnlyYamlWithoutActionsIsNotAddable() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                prescripts:
                  - module: changedetector_setup
                    version: 0.1
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("scripts_only.yaml");
        Files.writeString(configFile, yaml);

        IssueTracker.clear();
        Config loaded = readYaml(configFile.toFile());

        assertFalse(loaded.hasAddableActions());
        assertTrue(loaded.getPreScriptConfigs().isEmpty());
    }

    @Test
    void scriptFingerprintsPassWhenPreAndPostScriptsMatch() {
        var setupScript = scriptWithVersion("changedetector_setup", "0.1", "randomize");
        var detectScript = scriptWithVersion("changedetector_detect", "0.2", "module");
        ActionBank actionBank =
                testActionBank("shuffle_hp", "0.9", List.of(), List.of(setupScript), List.of(detectScript));
        Config config = new Config("1",
                List.of(new ActionConfig("shuffle_hp", "0.9", ActionArgumentsConfig.empty())),
                List.of(new ScriptConfig("changedetector_setup", "0.1")),
                List.of(new ScriptConfig("changedetector_detect", "0.2")), RulesConfig.empty());

        IssueTracker.clear();
        config.checkRequiredScriptFingerprints(actionBank);

        assertFalse(IssueTracker.hasWarnings());
    }

    @Test
    void scriptFingerprintsWarnWhenRequiredScriptMissingFromApp() {
        Config config = new Config("1",
                List.of(new ActionConfig("shuffle_hp", "0.9", ActionArgumentsConfig.empty())),
                List.of(new ScriptConfig("changedetector_setup", "0.1")),
                List.of(new ScriptConfig("missing_postscript", "0.1")), RulesConfig.empty());
        ActionBank actionBank = testActionBank("shuffle_hp", "0.9", List.of(),
                List.of(scriptWithVersion("changedetector_setup", "0.1", "randomize")), List.of());

        IssueTracker.clear();
        config.checkRequiredScriptFingerprints(actionBank);

        assertTrue(IssueTracker.getWarnings().stream()
                .anyMatch(w -> w.contains("missing_postscript") && w.contains("not loaded")));
    }

    @Test
    void scriptFingerprintsWarnOnVersionMismatch() {
        Config config = new Config("1",
                List.of(new ActionConfig("shuffle_hp", "0.9", ActionArgumentsConfig.empty())),
                List.of(new ScriptConfig("changedetector_setup", "0.0")),
                List.of(new ScriptConfig("changedetector_detect", "0.0")), RulesConfig.empty());
        ActionBank actionBank = testActionBank("shuffle_hp", "0.9", List.of(),
                List.of(scriptWithVersion("changedetector_setup", "0.1", "randomize")),
                List.of(scriptWithVersion("changedetector_detect", "0.2", "module")));

        IssueTracker.clear();
        config.checkRequiredScriptFingerprints(actionBank);

        assertTrue(IssueTracker.getWarnings().stream().anyMatch(w -> w.contains("prescripts")
                && w.contains("changedetector_setup") && w.contains("0.0")));
        assertTrue(IssueTracker.getWarnings().stream().anyMatch(w -> w.contains("postscripts")
                && w.contains("changedetector_detect") && w.contains("0.2")));
    }

    @Test
    void versionMismatchWarnsButStillLoadsAction() {
        Config config = new Config("1",
                List.of(new ActionConfig("shuffle_hp", "0.1", ActionArgumentsConfig.empty())),
                List.of(), List.of(), RulesConfig.empty());

        ActionBank actionBank = testActionBank("shuffle_hp", "0.9", List.of(), List.of(), List.of());

        IssueTracker.clear();
        var actions = config.getActions(actionBank);

        assertEquals(1, actions.size());
        assertTrue(IssueTracker.getWarnings().stream()
                .anyMatch(w -> w.contains("was saved as version 0.1")));
        assertTrue(IssueTracker.getWarnings().stream()
                .anyMatch(w -> w.contains("current version is 0.9")));
        assertFalse(IssueTracker.getWarnings().stream().anyMatch(w -> w.contains("appVersion")));
    }

    @Test
    void extraScriptInAppWarns() {
        Config config = new Config("1", List.of(), List.of(), List.of(), RulesConfig.empty());

        ActionBank actionBank = testActionBank(null, null, List.of(),
                List.of(scriptWithVersion("changedetector_setup", "0.1", "randomize")), List.of());

        IssueTracker.clear();
        config.checkScripts(actionBank);

        assertTrue(IssueTracker.getWarnings().stream().anyMatch(
                w -> w.contains("changedetector_setup") && w.contains("not in the config")));
    }

    @Test
    void loadWithoutScriptSectionsUsesEmptyScriptLists() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                seed: 1
                actions: []
                prescripts: []
                postscripts: []
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        IssueTracker.clear();
        Config loaded = readYaml(configFile.toFile());

        assertTrue(!IssueTracker.hasWarnings());
        assertTrue(loaded.getPreScriptConfigs().isEmpty());
        assertTrue(loaded.getPostScriptConfigs().isEmpty());
    }

    @Test
    void appVersionMismatchWarnsOnLoad() throws Exception {
        String yaml = """
                version: 1
                appVersion: 0.1.0
                seed: 1
                actions: []
                prescripts: []
                postscripts: []
                """;
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        IssueTracker.clear();
        readYaml(configFile.toFile());

        assertTrue(
                IssueTracker.getWarnings().stream().anyMatch(w -> w.contains("PtcgRandomizer 0.1.0")));
        assertTrue(IssueTracker.getWarnings().stream()
                .anyMatch(w -> w.contains(PtcgRandomizerVersion.VERSION)));
    }

    @Test
    void missingAppVersionWarnsOnLoad() throws Exception {
        String yaml = """
                version: 1
                seed: 42
                actions: []
                prescripts: []
                postscripts: []
                """;
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        IssueTracker.clear();
        readYaml(configFile.toFile());

        assertTrue(IssueTracker.getWarnings().stream()
                .anyMatch(w -> w.contains("missing required field \"appVersion\"")));
    }

    @Test
    void loadSkipsMissingSectionsWithoutWarnings() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                seed: 42
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("partial_seed.yaml");
        Files.writeString(configFile, yaml);

        IssueTracker.clear();
        Config loaded = Config.readFromLoadedYamlMap(YamlIO.load(configFile.toFile()),
                configFile.getFileName().toString());

        assertTrue(!IssueTracker.hasWarnings());
        assertTrue(loaded.isValid());
        assertTrue(loaded.hasSeed());
        assertEquals("42", loaded.getSeed());
        assertTrue(!loaded.hasActions());
        assertTrue(!loaded.hasRules());
    }

    @Test
    void loadMergesOnlyPresentRulesSections() throws Exception {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(testMonster(35, CardId.MONSTER_146_1, "TestMove"));

        String yaml = """
                version: 1
                appVersion: %s
                rules:
                  moveExclusions:
                    - remove_from_pool: true
                      exclude_from_randomization: true
                      card: SomeMonster lvl35
                      move: TestMove
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("partial_rules.yaml");
        Files.writeString(configFile, yaml);

        IssueTracker.clear();
        Config loaded = Config.readFromLoadedYamlMap(YamlIO.load(configFile.toFile()),
                configFile.getFileName().toString());
        assertTrue(!IssueTracker.hasWarnings());
        assertTrue(loaded.hasRules());
        assertTrue(!loaded.getRulesConfig().hasMoveAssignments());

        Rules rules = new Rules();
        rules.getMoveExclusions().addMoveExclusion(CardId.NO_CARD, "OldMove", true, true,
                "unsupported_moves.yaml", cards, rules.getMoveAssignments());
        loaded.getRulesConfig().applyTo(rules, cards);

        assertEquals(2, rules.getMoveExclusions().getAllExclusions().size());
    }

    @Test
    void missingVersionRejectsLoad() throws Exception {
        String yaml = """
                appVersion: %s
                seed: 42
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        IssueTracker.clear();
        Config loaded = Config.readFromLoadedYamlMap(YamlIO.load(configFile.toFile()),
                configFile.getFileName().toString());

        assertTrue(!loaded.isValid());
        assertTrue(IssueTracker.getWarnings().stream()
                .anyMatch(w -> w.contains("missing required field \"version\"")));
    }

    @Test
    void convertRulesOnlyToYamlMapUsesConfigStructure() {
        RulesConfig rules = new RulesConfig("user added",
                List.of(new MoveExclusionConfig("user added", true, true, "Sharp Sickle",
                        "Kabutops lvl30")),
                List.of());

        Map<String, Object> document = Config.convertRulesOnlyToYamlMap(rules);

        assertEquals(Config.CURRENT_FORMAT_VERSION, document.get("version"));
        assertEquals(PtcgRandomizerVersion.VERSION, document.get("appVersion"));
        assertFalse(document.containsKey("seed"));
        assertFalse(document.containsKey("actions"));
        @SuppressWarnings("unchecked")
        Map<String, Object> rulesNode = (Map<String, Object>) document.get("rules");
        assertTrue(rulesNode.containsKey("moveExclusions"));
        assertTrue(rulesNode.containsKey("moveAssignments"));
    }

    @Test
    void convertActionsOnlyToYamlMapUsesConfigStructure() {
        ActionBank actionBank = testActionBank("shuffle_hp", "0.9", List.of(), List.of(), List.of());
        Action action = actionBank.getModule("shuffle_hp") != null
                ? new Action(actionBank.getModule("shuffle_hp"), actionBank.getEnumRegistry())
                : null;
        assertTrue(action != null);

        Map<String, Object> document = Config.convertActionsOnlyToYamlMap(List.of(action), actionBank);

        assertEquals(Config.CURRENT_FORMAT_VERSION, document.get("version"));
        assertEquals(PtcgRandomizerVersion.VERSION, document.get("appVersion"));
        assertFalse(document.containsKey("seed"));
        assertFalse(document.containsKey("rules"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> actions = (List<Map<String, Object>>) document.get("actions");
        assertEquals(1, actions.size());
        assertEquals("shuffle_hp", actions.get(0).get("module"));
        assertFalse(document.containsKey("prescripts"));
        assertFalse(document.containsKey("postscripts"));
    }

    @Test
    void actionsOnlyExportRoundTripsWithScriptFingerprints() throws Exception {
        var setupScript = scriptWithVersion("changedetector_setup", "0.1", "randomize");
        var detectScript = scriptWithVersion("changedetector_detect", "0.1", "module");
        ActionBank actionBank =
                testActionBank("shuffle_hp", "0.9", List.of(), List.of(setupScript), List.of(detectScript));
        Action action = new Action(actionBank.getModule("shuffle_hp"), actionBank.getEnumRegistry());

        Path actionsFile = tempDir.resolve("user_actions.yaml");
        YamlIO.save(actionsFile.toFile(), Config.convertActionsOnlyToYamlMap(List.of(action), actionBank));

        IssueTracker.clear();
        Config loaded = Config.readFromLoadedYamlMap(YamlIO.load(actionsFile.toFile()),
                actionsFile.getFileName().toString());
        assertTrue(loaded.hasPreScripts());
        assertTrue(loaded.hasPostScripts());
        loaded.checkRequiredScriptFingerprints(actionBank);
        assertFalse(IssueTracker.hasWarnings());
        assertEquals("shuffle_hp", loaded.getActions(actionBank).get(0).getModule().getId());
    }

    @Test
    void rulesOnlyExportRoundTripsThroughPartialImport() throws Exception {
        RulesConfig rules = new RulesConfig("user added",
                List.of(new MoveExclusionConfig("user added", true, true, "TestMove",
                        "SomeMonster lvl35")),
                List.of());
        Path rulesFile = tempDir.resolve("user_config.yaml");
        YamlIO.save(rulesFile.toFile(), Config.convertRulesOnlyToYamlMap(rules));

        IssueTracker.clear();
        Config loaded = Config.readFromLoadedYamlMap(YamlIO.load(rulesFile.toFile()),
                rulesFile.getFileName().toString());

        assertTrue(!IssueTracker.hasWarnings());
        assertTrue(loaded.hasRules());
        assertEquals("TestMove",
                loaded.getRulesConfig().getMoveExclusionConfigs().get(0)
                        .convertToYamlMap().get("move"));
    }


    @Test
    void loadAcceptsNumericSeed() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                seed: 42
                actions: []
                prescripts: []
                postscripts: []
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        IssueTracker.clear();
        Config loaded = readYaml(configFile.toFile());
        assertTrue(!IssueTracker.hasWarnings());
        assertEquals("42", loaded.getSeed());
    }

    @Test
    void missingModuleVersionWarnsButStillLoadsAction() {
        Config config = new Config("1",
                List.of(new ActionConfig("shuffle_hp", null, ActionArgumentsConfig.empty())),
                List.of(), List.of(), RulesConfig.empty());

        ActionBank actionBank = testActionBank("shuffle_hp", "0.9", List.of(), List.of(), List.of());

        IssueTracker.clear();
        var actions = config.getActions(actionBank);

        assertEquals(1, actions.size());
        assertTrue(IssueTracker.getWarnings().stream()
                .anyMatch(w -> w.contains("does not record a version")));
    }

    @Test
    void missingModulesAreSkippedWithWarning() {
        Config config = new Config("1",
                List.of(new ActionConfig("missing_module", null, ActionArgumentsConfig.empty())),
                List.of(), List.of(), RulesConfig.empty());

        ActionBank actionBank = new ActionBank(null) {
            @Override
            public Module getModule(String moduleId) {
                return null;
            }

            @Override
            public List<Module> getPreScripts() {
                return List.of();
            }

            @Override
            public List<Module> getPostScripts() {
                return List.of();
            }
        };

        IssueTracker.clear();
        var actions = config.getActions(actionBank);

        assertTrue(actions.isEmpty());
        assertTrue(IssueTracker.getWarnings().stream().anyMatch(w -> w.contains("missing_module")));
        assertTrue(IssueTracker.getWarnings().stream().anyMatch(w -> w.contains("skipped")));
    }

    @Test
    void presetRoundTripsRulesUsingExistingParser() throws Exception {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(testMonster(35, CardId.MONSTER_146_1, "TestMove"));

        RulesConfig rulesPreset = RulesConfig.readFromLoadedYamlMap(
                Map.of("moveExclusions",
                        List.of(Map.of("remove_from_pool", true, "exclude_from_randomization", false,
                                "card", "SomeMonster lvl35", "move", "TestMove")),
                        "moveAssignments", List.of(Map.of("to_card", "SomeMonster lvl35",
                                "to_move_slot", 1, "move", "TestMove"))),
                "config.yaml");

        Config config = new Config("1", List.of(), List.of(), List.of(), rulesPreset);
        Path output = tempDir.resolve("config.yaml");
        YamlIO.save(output.toFile(), config.convertToYamlMap());

        IssueTracker.clear();
        Config loaded = readYaml(output.toFile());
        assertTrue(!IssueTracker.hasWarnings());
        assertEquals("TestMove",
                loaded.getRulesConfig().getMoveExclusionConfigs().get(0).convertToYamlMap()
                        .get("move"));

        Rules rules = new Rules();
        rules.clear();
        loaded.getRulesConfig().applyTo(rules, cards);

        assertTrue(!IssueTracker.hasWarnings());
        assertEquals(1, rules.getMoveExclusions().getAllExclusions().size());
        assertEquals(1, rules.getMoveAssignments().getAllAssignments().size());
    }

    @Test
    void fromAppStateSavesLoadedRules() {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(testMonster(35, CardId.MONSTER_146_1, "TestMove"));

        RulesConfig rulesPreset =
                RulesConfig.readFromLoadedYamlMap(
                        Map.of("moveExclusions",
                                List.of(Map.of("remove_from_pool", true,
                                        "exclude_from_randomization", false, "move", "TestMove")),
                                "moveAssignments",
                                List.of(Map.of("to_card", "SomeMonster lvl35", "to_move_slot", 1,
                                        "move", "TestMove"))),
                        "config.yaml");
        Rules rules = new Rules();
        rulesPreset.applyTo(rules, cards);

        Config config = Config.fromAppState("42", List.of(),
                testActionBank(null, null, List.of(), List.of(), List.of()), rules, cards);

        assertEquals("TestMove",
                config.getRulesConfig().getMoveExclusionConfigs().get(0).convertToYamlMap()
                        .get("move"));
        assertEquals(1, config.getRulesConfig().getMoveExclusionConfigs().size());
        assertEquals(1, config.getRulesConfig().getMoveAssignmentConfigs().size());
    }

    @Test
    void fromAppStateIncludesEmptyAssignmentsNode() {
        RulesConfig rulesPreset = RulesConfig.readFromLoadedYamlMap(
                Map.of("moveExclusions",
                        List.of(Map.of("remove_from_pool", true, "exclude_from_randomization", true,
                                "move", "UserMove")),
                        "moveAssignments", List.of()),
                "config.yaml");
        Rules rules = new Rules();
        rulesPreset.applyTo(rules, new CardGroup<>());

        Config config = Config.fromAppState("42", List.of(),
                testActionBank(null, null, List.of(), List.of(), List.of()), rules,
                new CardGroup<>());

        assertTrue(config.getRulesConfig().getMoveAssignmentConfigs().isEmpty());
    }

    @Test
    void applyRulesClearsExistingRulesBeforeLoadingPreset() {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(testMonster(35, CardId.MONSTER_146_1, "TestMove"));

        RulesConfig rulesPreset = RulesConfig.readFromLoadedYamlMap(Map.of("moveExclusions",
                List.of(Map.of("remove_from_pool", true, "exclude_from_randomization", true, "card",
                        "SomeMonster lvl35", "move", "TestMove")),
                "moveAssignments", List.of()), "config.yaml");
        Config config = new Config("1", List.of(), List.of(), List.of(), rulesPreset);

        IssueTracker.clear();
        Rules rules = new Rules();
        rules.getMoveExclusions().addMoveExclusion(CardId.NO_CARD, "OldMove", true, true,
                "unsupported_moves.yaml", cards, rules.getMoveAssignments());

        rules.clear();
        config.getRulesConfig().applyTo(rules, cards);

        assertTrue(!IssueTracker.hasWarnings());
        assertEquals(1, rules.getMoveExclusions().getAllExclusions().size());
        assertEquals("TestMove", rules.getMoveExclusions().getAllExclusions().get(0).getMoveName());
        assertEquals(1, rules.getMoveAssignments().getAllAssignments().size());
    }

    @Test
    void applyRulesWithEmptyRulesSectionClearsExistingRules() {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        Config config = new Config("1", List.of(), List.of(), List.of(), RulesConfig.empty());

        IssueTracker.clear();
        Rules rules = new Rules();
        rules.getMoveExclusions().addMoveExclusion(CardId.NO_CARD, "OldMove", true, true,
                "unsupported_moves.yaml", cards, rules.getMoveAssignments());

        rules.clear();
        config.getRulesConfig().applyTo(rules, cards);

        assertTrue(!IssueTracker.hasWarnings());
        assertTrue(rules.getMoveExclusions().getAllExclusions().isEmpty());
        assertTrue(rules.getMoveAssignments().getAllAssignments().isEmpty());
    }

    @Test
    void fromAppStateSavesExclusionsLoadedFromRulesFile() throws Exception {
        Path rulesFile = tempDir.resolve("custom_rules.yaml");
        Files.writeString(rulesFile, """
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    card: SomeMonster lvl35
                    move: TestMove
                """);

        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(testMonster(35, CardId.MONSTER_146_1, "TestMove"));

        IssueTracker.clear();
        RulesConfig rulesPreset = RulesConfig.readFromLoadedYamlMap(
                YamlIO.load(rulesFile.toFile()), rulesFile.getFileName().toString());
        Rules rules = new Rules();
        rulesPreset.applyTo(rules, cards);

        Config config = Config.fromAppState("42", List.of(),
                testActionBank(null, null, List.of(), List.of(), List.of()), rules, cards);

        assertEquals(1, config.getRulesConfig().getMoveExclusionConfigs().size());
        assertEquals("TestMove",
                config.getRulesConfig().getMoveExclusionConfigs().get(0).convertToYamlMap()
                        .get("move"));
        assertTrue(config.getRulesConfig().getMoveAssignmentConfigs().isEmpty());
    }

    private static MonsterCard testMonster(int level, CardId id, String moveName) {
        MonsterCard card = new MonsterCard();
        card.id = id;
        card.name.setText("SomeMonster");
        card.level = (byte) level;
        Move move = card.getMove(0);
        move.name.setText(moveName);
        card.setMoves(List.of(move, card.getMove(1)));
        return card;
    }

    private static Config readYaml(File file) throws Exception {
        return Config.readFromLoadedYamlMap(YamlIO.load(file), file.getName());
    }

    private static ActionBank testActionBank(String moduleId, String version,
            List<ArgumentDefinition> arguments, List<Module> preScripts,
            List<Module> postScripts) {
        return new ActionBank(null) {
            @Override
            public Module getModule(String id) {
                if (moduleId != null && moduleId.equals(id)) {
                    return moduleWithVersion(id, version, arguments);
                }
                return null;
            }

            @Override
            public Module getScript(String id) {
                for (Module script : preScripts) {
                    if (script.getId().equals(id)) {
                        return script;
                    }
                }
                for (Module script : postScripts) {
                    if (script.getId().equals(id)) {
                        return script;
                    }
                }
                return null;
            }

            @Override
            public List<Module> getPreScripts() {
                return preScripts;
            }

            @Override
            public List<Module> getPostScripts() {
                return postScripts;
            }
        };
    }

    private static Module moduleWithVersion(String id, String version) {
        return moduleWithVersion(id, version, List.of());
    }

    private static Module moduleWithArguments(String id, String version,
            List<ArgumentDefinition> arguments) {
        return moduleWithVersion(id, version, arguments);
    }

    private static Module moduleWithVersion(String id, String version,
            List<ArgumentDefinition> arguments) {
        return new Module(id, id, "", Set.of("pokemon cards"), Set.of(), arguments,
                new ZeroArgFunction() {
                    @Override
                    public LuaValue call() {
                        return LuaValue.NIL;
                    }
                }, null, "test.lua", 0, true, true, null, "author", version, Map.of(), null,
                null, null, null, null);
    }

    private static Module scriptWithVersion(String id, String version, String when) {
        return new Module(id, id, "", Set.of(), Set.of(), List.of(), new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.NIL;
            }
        }, null, "test.lua", 0, false, false, when, "author", version, Map.of(), null, null, null, null, null);
    }
}
