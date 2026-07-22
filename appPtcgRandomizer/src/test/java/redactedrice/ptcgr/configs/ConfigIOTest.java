package redactedrice.ptcgr.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.yaml.snakeyaml.Yaml;
import redactedrice.ptcgr.configs.modules.ActionConfig;
import redactedrice.ptcgr.configs.modules.ActionArgumentsConfig;
import redactedrice.ptcgr.configs.modules.ScriptConfig;
import redactedrice.ptcgr.configs.rules.RulesConfig;
import redactedrice.ptcgr.constants.PtcgRandomizerVersion;
import redactedrice.ptcgr.constants.CardConstants.CardId;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.data.Move;
import redactedrice.ptcgr.randomizer.Settings;
import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.ptcgr.rules.Rules;
import redactedrice.ptcgr.utils.FileExtensionUtils;
import redactedrice.ptcgr.utils.WarningCollector;
import redactedrice.randomizer.lua.Module;
import redactedrice.randomizer.lua.arguments.ArgumentDefinition;
import redactedrice.randomizer.lua.arguments.TypeDefinition;

class YamlIOTest {
    @TempDir
    Path tempDir;

    @Test
    void ensureYamlExtensionAddsExtensionWhenMissing() {
        File file = new File("configs/ptcgr_configs");
        File resolved = FileExtensionUtils.ensureExtension(file, YamlIO.FILE_EXTENSION);
        assertEquals("ptcgr_configs.yaml", resolved.getName());
    }

    @Test
    void loadAddsYamlExtensionWhenMissing() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                seed: 7
                actions: []
                prescripts: []
                postscripts: []
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("ptcgr_configs.yaml");
        Files.writeString(configFile, yaml);

        WarningCollector warnings = new WarningCollector(null);
        Config config = Config.readFromLoadedYamlMap(
                YamlIO.load(tempDir.resolve("ptcgr_configs").toFile(), warnings),
                tempDir.resolve("ptcgr_configs.yaml").getFileName().toString(), warnings);

        assertTrue(!warnings.hasWarnings());
        assertEquals("7", config.getSeed());
    }

    @Test
    void ensureYamlExtensionNormalizesYml() {
        File file = new File("configs/config.yml");
        File resolved = FileExtensionUtils.ensureExtension(file, YamlIO.FILE_EXTENSION);
        assertEquals("config.yaml", resolved.getName());
    }

    @Test
    void presetDocumentIncludesAppVersionSeedActionsAndScripts() {
        Config config = new Config("123456789", Collections.emptyList(), List.of(), List.of(),
                RulesConfig.empty());
        Map<String, Object> document = config.convertToYamlMap();
        assertEquals(Config.CURRENT_FORMAT_VERSION, document.get("version"));
        assertEquals(PtcgRandomizerVersion.VERSION, document.get("appVersion"));
        assertEquals("123456789", document.get("seed"));
        assertEquals(Collections.emptyList(), document.get("actions"));
        assertEquals(Collections.emptyList(), document.get("prescripts"));
        assertEquals(Collections.emptyList(), document.get("postscripts"));
        assertEquals(RulesConfig.empty().convertToYamlMap(), document.get("rules"));
        assertFalse(document.containsKey("randomizationSettings"));
    }

    @Test
    void saveWritesResolvedSeedAppVersionAndScripts() throws Exception {
        Settings settings = new Settings();
        settings.setSeed("random");

        var setupScript = scriptWithVersion("changedetector_setup", "0.1", "randomize");
        var detectScript = scriptWithVersion("changedetector_detect", "0.1", "module");
        Config config = Config.fromAppState(settings.getSeedString(), List.of(),
                testActionBank(null, null, List.of(), List.of(setupScript), List.of(detectScript)),
                RulesConfig.empty());
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
    void saveWritesYamlFile() throws Exception {
        Config config = new Config("42", Collections.emptyList(), List.of(), List.of(),
                RulesConfig.empty());
        Path output = tempDir.resolve("config.yaml");
        YamlIO.save(output.toFile(), config.convertToYamlMap());

        String yamlText = Files.readString(output);
        assertTrue(yamlText.startsWith("# PTCG Randomizer config\n"));

        @SuppressWarnings("unchecked")
        Map<String, Object> loaded =
                new Yaml().load(yamlText.substring(yamlText.indexOf('\n') + 1));
        assertEquals("42", loaded.get("seed"));
        assertEquals(Collections.emptyList(), loaded.get("actions"));
        assertEquals(Collections.emptyList(), loaded.get("prescripts"));
    }

    @Test
    void actionPresetSavesModuleVersion() {
        ActionConfig actionPreset =
                new ActionConfig("shuffle_hp", "0.1", ActionArgumentsConfig.empty());
        Map<String, Object> node = actionPreset.convertToYamlMap();
        assertEquals("shuffle_hp", node.get("module"));
        assertEquals("0.1", node.get("version"));
    }

    @Test
    void actionPresetWritesUnknownWhenVersionMissing() {
        ActionConfig actionPreset =
                new ActionConfig("shuffle_hp", null, ActionArgumentsConfig.empty());
        Map<String, Object> node = actionPreset.convertToYamlMap();
        assertEquals("<unknown>", node.get("version"));
    }

    @Test
    void scriptPresetSavesModuleVersion() {
        ScriptConfig scriptPreset = new ScriptConfig("changedetector_setup", "0.1");
        Map<String, Object> node = scriptPreset.convertToYamlMap();
        assertEquals("changedetector_setup", node.get("module"));
        assertEquals("0.1", node.get("version"));
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
                    config:
                      seedOffset: 12
                prescripts: []
                postscripts: []
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        WarningCollector warnings = new WarningCollector(null);
        Config config = loadConfig(configFile.toFile(), warnings);
        assertTrue(!warnings.hasWarnings());
        assertEquals("987654321", config.getSeed());
        assertEquals(PtcgRandomizerVersion.VERSION, config.getAppVersion());
        assertEquals(1, config.getActionConfigs().size());
        assertEquals("shuffle_hp", config.getActionConfigs().get(0).getModule());
        assertEquals("0.1", config.getActionConfigs().get(0).getVersion());
        assertEquals(12, config.getActionConfigs().get(0).getConfig().getSeedOffset());
    }

    @Test
    void actionPresetSavesStoredModuleArguments() {
        Module module = moduleWithArguments("set_num_moves", "0.9", List.of(
                new ArgumentDefinition("numMoves", TypeDefinition.integer(), 2)));
        Action action = new Action(module);
        action.setArgument("numMoves", 1);

        ActionArgumentsConfig saved = ActionArgumentsConfig.fromAction(action);
        Map<String, Object> yamlArgs = saved.convertToYamlMap();
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) yamlArgs.get("arguments");
        assertEquals(1, arguments.get("numMoves"));
    }

    @Test
    void loadRestoresModuleArguments() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                seed: 1
                actions:
                  - module: set_num_moves
                    config:
                      arguments:
                        numMoves: 1
                prescripts: []
                postscripts: []
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        WarningCollector warnings = new WarningCollector(null);
        Config config = loadConfig(configFile.toFile(), warnings);
        assertEquals(1, config.getActionConfigs().size());
        assertEquals(1, config.getActionConfigs().get(0).getConfig().getArguments().get("numMoves"));

        ActionBank actionBank = testActionBank("set_num_moves", "0.9",
                List.of(new ArgumentDefinition("numMoves", TypeDefinition.integer(), 2)),
                List.of(), List.of());
        var actions = config.getActions(actionBank, warnings);
        assertEquals(1, actions.size());
        assertEquals(1, actions.get(0).getArgument("numMoves"));
    }

    @Test
    void loadWarnsOnUnknownModuleArgument() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                seed: 1
                actions:
                  - module: set_num_moves
                    config:
                      arguments:
                        numMoves: 1
                        bogusArg: 5
                prescripts: []
                postscripts: []
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        WarningCollector warnings = new WarningCollector(null);
        Config config = loadConfig(configFile.toFile(), warnings);
        ActionBank actionBank = testActionBank("set_num_moves", "0.9",
                List.of(new ArgumentDefinition("numMoves", TypeDefinition.integer(), 2)),
                List.of(), List.of());
        var actions = config.getActions(actionBank, warnings);

        assertEquals(1, actions.size());
        assertEquals(1, actions.get(0).getArgument("numMoves"));
        assertTrue(warnings.getWarnings().stream()
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
                    config:
                      arguments: {}
                prescripts: []
                postscripts: []
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        WarningCollector warnings = new WarningCollector(null);
        Config config = loadConfig(configFile.toFile(), warnings);
        ActionBank actionBank = testActionBank("set_num_moves", "0.9",
                List.of(new ArgumentDefinition("numMoves", TypeDefinition.integer(), 2)),
                List.of(), List.of());
        var actions = config.getActions(actionBank, warnings);

        assertEquals(1, actions.size());
        assertEquals(2, actions.get(0).getArgument("numMoves"));
        assertTrue(warnings.getWarnings().stream()
                .anyMatch(w -> w.contains("numMoves") && w.contains("not specified")));
    }

    @Test
    void loadRestoresScriptsWithoutLoadingThem() throws Exception {
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

        WarningCollector warnings = new WarningCollector(null);
        Config config = loadConfig(configFile.toFile(), warnings);
        assertTrue(!warnings.hasWarnings());
        assertEquals(1, config.getPreScriptConfigs().size());
        assertEquals("changedetector_setup", config.getPreScriptConfigs().get(0).getModule());
        assertEquals(1, config.getPostScriptConfigs().size());
        assertEquals("changedetector_detect", config.getPostScriptConfigs().get(0).getModule());
    }

    @Test
    void loadRestoresSeedActionsAndSeedOffsetWithoutModuleVersion() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                seed: 987654321
                actions:
                  - module: shuffle_hp
                    config:
                      seedOffset: 12
                prescripts: []
                postscripts: []
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path configFile = tempDir.resolve("config.yaml");
        Files.writeString(configFile, yaml);

        WarningCollector warnings = new WarningCollector(null);
        Config config = loadConfig(configFile.toFile(), warnings);
        assertTrue(!warnings.hasWarnings());
        assertEquals("987654321", config.getSeed());
        assertEquals(1, config.getActionConfigs().size());
        assertEquals("shuffle_hp", config.getActionConfigs().get(0).getModule());
        assertEquals(null, config.getActionConfigs().get(0).getVersion());
        assertEquals(12, config.getActionConfigs().get(0).getConfig().getSeedOffset());
    }

    @Test
    void versionMismatchWarnsButStillLoadsAction() {
        Config config = new Config("1",
                List.of(new ActionConfig("shuffle_hp", "0.1", ActionArgumentsConfig.empty())),
                List.of(), List.of(), RulesConfig.empty());

        ActionBank actionBank = testActionBank("shuffle_hp", "0.9", List.of(), List.of(), List.of());

        WarningCollector warnings = new WarningCollector(null);
        var actions = config.getActions(actionBank, warnings);

        assertEquals(1, actions.size());
        assertTrue(warnings.getWarnings().stream()
                .anyMatch(w -> w.contains("was saved as version 0.1")));
        assertTrue(warnings.getWarnings().stream()
                .anyMatch(w -> w.contains("current version is 0.9")));
        assertFalse(warnings.getWarnings().stream().anyMatch(w -> w.contains("appVersion")));
    }

    @Test
    void scriptVersionMismatchWarnsWithoutLoadingScripts() {
        Config config = new Config("1", List.of(),
                List.of(new ScriptConfig("changedetector_setup", "0.0")),
                List.of(new ScriptConfig("changedetector_detect", "0.0")), RulesConfig.empty());

        ActionBank actionBank = testActionBank(null, null, List.of(),
                List.of(scriptWithVersion("changedetector_setup", "0.1", "randomize")),
                List.of(scriptWithVersion("changedetector_detect", "0.2", "module")));

        WarningCollector warnings = new WarningCollector(null);
        config.checkScripts(actionBank, warnings);

        assertTrue(warnings.getWarnings().stream().anyMatch(w -> w.contains("prescripts")
                && w.contains("changedetector_setup") && w.contains("0.0")));
        assertTrue(warnings.getWarnings().stream().anyMatch(w -> w.contains("postscripts")
                && w.contains("changedetector_detect") && w.contains("0.2")));
    }

    @Test
    void missingScriptInAppWarns() {
        Config config =
                new Config("1", List.of(), List.of(new ScriptConfig("missing_prescript", "0.1")),
                        List.of(), RulesConfig.empty());

        ActionBank actionBank = testActionBank(null, null, List.of(), List.of(), List.of());

        WarningCollector warnings = new WarningCollector(null);
        config.checkScripts(actionBank, warnings);

        assertTrue(warnings.getWarnings().stream()
                .anyMatch(w -> w.contains("missing_prescript") && w.contains("not loaded")));
    }

    @Test
    void extraScriptInAppWarns() {
        Config config = new Config("1", List.of(), List.of(), List.of(), RulesConfig.empty());

        ActionBank actionBank = testActionBank(null, null, List.of(),
                List.of(scriptWithVersion("changedetector_setup", "0.1", "randomize")), List.of());

        WarningCollector warnings = new WarningCollector(null);
        config.checkScripts(actionBank, warnings);

        assertTrue(warnings.getWarnings().stream().anyMatch(
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

        WarningCollector warnings = new WarningCollector(null);
        Config config = loadConfig(configFile.toFile(), warnings);

        assertTrue(!warnings.hasWarnings());
        assertTrue(config.getPreScriptConfigs().isEmpty());
        assertTrue(config.getPostScriptConfigs().isEmpty());
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

        WarningCollector warnings = new WarningCollector(null);
        loadConfig(configFile.toFile(), warnings);

        assertTrue(
                warnings.getWarnings().stream().anyMatch(w -> w.contains("PtcgRandomizer 0.1.0")));
        assertTrue(warnings.getWarnings().stream()
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

        WarningCollector warnings = new WarningCollector(null);
        loadConfig(configFile.toFile(), warnings);

        assertTrue(warnings.getWarnings().stream()
                .anyMatch(w -> w.contains("Config is missing an appVersion")));
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

        WarningCollector warnings = new WarningCollector(null);
        Config config = loadConfig(configFile.toFile(), warnings);
        assertTrue(!warnings.hasWarnings());
        assertEquals("42", config.getSeed());
    }

    @Test
    void missingModuleVersionWarnsButStillLoadsAction() {
        Config config = new Config("1",
                List.of(new ActionConfig("shuffle_hp", null, ActionArgumentsConfig.empty())),
                List.of(), List.of(), RulesConfig.empty());

        ActionBank actionBank = testActionBank("shuffle_hp", "0.9", List.of(), List.of(), List.of());

        WarningCollector warnings = new WarningCollector(null);
        var actions = config.getActions(actionBank, warnings);

        assertEquals(1, actions.size());
        assertTrue(warnings.getWarnings().stream()
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

        WarningCollector warnings = new WarningCollector(null);
        var actions = config.getActions(actionBank, warnings);

        assertTrue(actions.isEmpty());
        assertTrue(warnings.getWarnings().stream().anyMatch(w -> w.contains("missing_module")));
        assertTrue(warnings.getWarnings().stream().anyMatch(w -> w.contains("skipped")));
    }

    @Test
    void presetRoundTripsRulesUsingExistingParser() throws Exception {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(testMonster(35, CardId.MONSTER_146_1, "TestMove"));

        RulesConfig rulesPreset = RulesConfig.readFromLoadedYamlMap(
                Map.of("moveExclusions",
                        List.of(Map.of("remove_from_pool", true, "exclude_from_randomization", true,
                                "card", "SomeMonster lvl35", "move", "TestMove")),
                        "moveAssignments", List.of(Map.of("to_card", "SomeMonster lvl35",
                                "to_move_slot", 1, "move", "TestMove"))),
                "config.yaml", new WarningCollector(null));

        Config config = new Config("1", List.of(), List.of(), List.of(), rulesPreset);
        Path output = tempDir.resolve("config.yaml");
        YamlIO.save(output.toFile(), config.convertToYamlMap());

        WarningCollector warnings = new WarningCollector(null);
        Config loaded = loadConfig(output.toFile(), warnings);
        assertTrue(!warnings.hasWarnings());
        assertEquals("TestMove", loaded.getRulesConfig().getMoveExclusionConfigs().get(0).getMove());

        Rules rules = new Rules();
        loaded.getRulesConfig().recreateRules(rules, cards, warnings);

        assertTrue(!warnings.hasWarnings());
        assertEquals(1, rules.getMoveExclusions().getAllExclusions().size());
        assertEquals(1, rules.getMoveAssignments().getAllAssignments().size());
    }

    @Test
    void fromAppStateSavesLoadedRules() {
        RulesConfig rulesPreset =
                RulesConfig.readFromLoadedYamlMap(
                        Map.of("moveExclusions",
                                List.of(Map.of("remove_from_pool", true,
                                        "exclude_from_randomization", true, "move", "UserMove")),
                                "moveAssignments",
                                List.of(Map.of("to_card", "SomeMonster lvl35", "to_move_slot", 1,
                                        "move", "TestMove"))),
                        "config.yaml", new WarningCollector(null));

        Config config = Config.fromAppState("42", List.of(),
                testActionBank(null, null, List.of(), List.of(), List.of()), rulesPreset);

        assertEquals("UserMove", config.getRulesConfig().getMoveExclusionConfigs().get(0).getMove());
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
                "config.yaml", new WarningCollector(null));

        Config config = Config.fromAppState("42", List.of(),
                testActionBank(null, null, List.of(), List.of(), List.of()), rulesPreset);

        assertTrue(config.getRulesConfig().getMoveAssignmentConfigs().isEmpty());
    }

    @Test
    void applyRulesClearsExistingRulesBeforeLoadingPreset() {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(testMonster(35, CardId.MONSTER_146_1, "TestMove"));

        RulesConfig rulesPreset = RulesConfig.readFromLoadedYamlMap(Map.of("moveExclusions",
                List.of(Map.of("remove_from_pool", true, "exclude_from_randomization", true, "card",
                        "SomeMonster lvl35", "move", "TestMove")),
                "moveAssignments", List.of()), "config.yaml", new WarningCollector(null));
        Config config = new Config("1", List.of(), List.of(), List.of(), rulesPreset);

        WarningCollector warnings = new WarningCollector(null);
        Rules rules = new Rules();
        rules.getMoveExclusions().addMoveExclusion(CardId.NO_CARD, "OldMove", true, true,
                "unsupported_moves.yaml", cards, rules.getMoveAssignments());

        config.getRulesConfig().recreateRules(rules, cards, warnings);

        assertTrue(!warnings.hasWarnings());
        assertEquals(1, rules.getMoveExclusions().getAllExclusions().size());
        assertEquals("TestMove", rules.getMoveExclusions().getAllExclusions().get(0).getMoveName());
        assertEquals(1, rules.getMoveAssignments().getAllAssignments().size());
    }

    @Test
    void applyRulesWithEmptyRulesSectionClearsExistingRules() {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        Config config = new Config("1", List.of(), List.of(), List.of(), RulesConfig.empty());

        WarningCollector warnings = new WarningCollector(null);
        Rules rules = new Rules();
        rules.getMoveExclusions().addMoveExclusion(CardId.NO_CARD, "OldMove", true, true,
                "unsupported_moves.yaml", cards, rules.getMoveAssignments());

        config.getRulesConfig().recreateRules(rules, cards, warnings);

        assertTrue(!warnings.hasWarnings());
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

        WarningCollector warnings = new WarningCollector(null);
        RulesConfig rulesPreset = RulesConfig.readFromLoadedYamlMap(
                YamlIO.load(rulesFile.toFile(), warnings), rulesFile.getFileName().toString(),
                warnings);

        Config config = Config.fromAppState("42", List.of(),
                testActionBank(null, null, List.of(), List.of(), List.of()), rulesPreset);

        assertEquals(1, config.getRulesConfig().getMoveExclusionConfigs().size());
        assertEquals("TestMove", config.getRulesConfig().getMoveExclusionConfigs().get(0).getMove());
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

    private static Config loadConfig(File file, WarningCollector warnings) throws Exception {
        return Config.readFromLoadedYamlMap(YamlIO.load(file, warnings), file.getName(), warnings);
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
                null, null);
    }

    private static Module scriptWithVersion(String id, String version, String when) {
        return new Module(id, id, "", Set.of(), Set.of(), List.of(), new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.NIL;
            }
        }, null, "test.lua", 0, false, false, when, "author", version, Map.of(), null, null, null);
    }
}
