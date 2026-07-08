package redactedrice.ptcgr.randomizer.preset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.yaml.snakeyaml.Yaml;

import redactedrice.ptcgr.constants.PtcgRandomizerVersion;
import redactedrice.ptcgr.randomizer.Settings;
import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.randomizer.lua.Module;

class PresetIOTest {
    @TempDir
    Path tempDir;

    @Test
    void ensureYamlExtensionAddsExtensionWhenMissing() {
        File file = new File("configs/ptcgr_configs");
        File resolved = PresetIO.ensureYamlExtension(file);
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
        Path presetFile = tempDir.resolve("ptcgr_configs.yaml");
        Files.writeString(presetFile, yaml);

        List<String> warnings = new ArrayList<>();
        Preset preset = PresetIO.load(tempDir.resolve("ptcgr_configs").toFile(), warnings);

        assertTrue(warnings.isEmpty());
        assertEquals("7", preset.getSeed());
    }

    @Test
    void ensureYamlExtensionNormalizesYml() {
        File file = new File("configs/preset.yml");
        File resolved = PresetIO.ensureYamlExtension(file);
        assertEquals("preset.yaml", resolved.getName());
    }

    @Test
    void presetDocumentIncludesAppVersionSeedActionsAndScripts() {
        Preset preset = new Preset("123456789", Collections.emptyList(), List.of(), List.of());
        Map<String, Object> document = preset.prepForSave();
        assertEquals(Preset.CURRENT_FORMAT_VERSION, document.get("version"));
        assertEquals(PtcgRandomizerVersion.VERSION, document.get("appVersion"));
        assertEquals("123456789", document.get("seed"));
        assertEquals(Collections.emptyList(), document.get("actions"));
        assertEquals(Collections.emptyList(), document.get("prescripts"));
        assertEquals(Collections.emptyList(), document.get("postscripts"));
        assertFalse(document.containsKey("randomizationSettings"));
    }

    @Test
    void saveWritesResolvedSeedAppVersionAndScripts() throws Exception {
        Settings settings = new Settings();
        settings.setSeed("random");

        var setupScript = scriptWithVersion("changedetector_setup", "0.1", "randomize");
        var detectScript = scriptWithVersion("changedetector_detect", "0.1", "module");
        Preset preset = Preset.fromAppState(settings.getSeedString(), List.of(),
                testActionBank(null, null, List.of(setupScript), List.of(detectScript)));
        Path output = tempDir.resolve("preset.yaml");
        PresetIO.save(output.toFile(), preset);

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
        Preset preset = new Preset("42", Collections.emptyList(), List.of(), List.of());
        Path output = tempDir.resolve("preset.yaml");
        PresetIO.save(output.toFile(), preset);

        String yamlText = Files.readString(output);
        assertTrue(yamlText.startsWith("# PTCG Randomizer preset\n"));

        @SuppressWarnings("unchecked")
        Map<String, Object> loaded =
                new Yaml().load(yamlText.substring(yamlText.indexOf('\n') + 1));
        assertEquals("42", loaded.get("seed"));
        assertEquals(Collections.emptyList(), loaded.get("actions"));
        assertEquals(Collections.emptyList(), loaded.get("prescripts"));
    }

    @Test
    void actionPresetSavesModuleVersion() {
        ActionPreset actionPreset =
                new ActionPreset("shuffle_hp", "0.1", ModuleConfigPreset.empty());
        Map<String, Object> node = actionPreset.prepForSave();
        assertEquals("shuffle_hp", node.get("module"));
        assertEquals("0.1", node.get("version"));
    }

    @Test
    void actionPresetWritesUnknownWhenVersionMissing() {
        ActionPreset actionPreset =
                new ActionPreset("shuffle_hp", null, ModuleConfigPreset.empty());
        Map<String, Object> node = actionPreset.prepForSave();
        assertEquals(ModulePreset.UNKNOWN_VERSION, node.get("version"));
    }

    @Test
    void scriptPresetSavesModuleVersion() {
        ScriptPreset scriptPreset = new ScriptPreset("changedetector_setup", "0.1");
        Map<String, Object> node = scriptPreset.prepForSave();
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
        Path presetFile = tempDir.resolve("preset.yaml");
        Files.writeString(presetFile, yaml);

        List<String> warnings = new ArrayList<>();
        Preset preset = PresetIO.load(presetFile.toFile(), warnings);
        assertTrue(warnings.isEmpty());
        assertEquals("987654321", preset.getSeed());
        assertEquals(PtcgRandomizerVersion.VERSION, preset.getAppVersion());
        assertEquals(1, preset.getActionPresets().size());
        assertEquals("shuffle_hp", preset.getActionPresets().get(0).getModule());
        assertEquals("0.1", preset.getActionPresets().get(0).getVersion());
        assertEquals(12, preset.getActionPresets().get(0).getConfig().getSeedOffset());
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
        Path presetFile = tempDir.resolve("preset.yaml");
        Files.writeString(presetFile, yaml);

        List<String> warnings = new ArrayList<>();
        Preset preset = PresetIO.load(presetFile.toFile(), warnings);
        assertTrue(warnings.isEmpty());
        assertEquals(1, preset.getPreScriptPresets().size());
        assertEquals("changedetector_setup", preset.getPreScriptPresets().get(0).getModule());
        assertEquals(1, preset.getPostScriptPresets().size());
        assertEquals("changedetector_detect", preset.getPostScriptPresets().get(0).getModule());
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
        Path presetFile = tempDir.resolve("preset.yaml");
        Files.writeString(presetFile, yaml);

        List<String> warnings = new ArrayList<>();
        Preset preset = PresetIO.load(presetFile.toFile(), warnings);
        assertTrue(warnings.isEmpty());
        assertEquals("987654321", preset.getSeed());
        assertEquals(1, preset.getActionPresets().size());
        assertEquals("shuffle_hp", preset.getActionPresets().get(0).getModule());
        assertEquals(null, preset.getActionPresets().get(0).getVersion());
        assertEquals(12, preset.getActionPresets().get(0).getConfig().getSeedOffset());
    }

    @Test
    void versionMismatchWarnsButStillLoadsAction() {
        Preset preset = new Preset("1",
                List.of(new ActionPreset("shuffle_hp", "0.1", ModuleConfigPreset.empty())),
                List.of(), List.of());

        ActionBank actionBank = testActionBank("shuffle_hp", "0.9", List.of(), List.of());

        List<String> warnings = new ArrayList<>();
        var actions = preset.getActions(actionBank, warnings);

        assertEquals(1, actions.size());
        assertTrue(warnings.stream().anyMatch(w -> w.contains("was saved as version 0.1")));
        assertTrue(warnings.stream().anyMatch(w -> w.contains("current version is 0.9")));
        assertFalse(warnings.stream().anyMatch(w -> w.contains("appVersion")));
    }

    @Test
    void scriptVersionMismatchWarnsWithoutLoadingScripts() {
        Preset preset = new Preset("1", List.of(),
                List.of(new ScriptPreset("changedetector_setup", "0.0")),
                List.of(new ScriptPreset("changedetector_detect", "0.0")));

        ActionBank actionBank = testActionBank(null, null,
                List.of(scriptWithVersion("changedetector_setup", "0.1", "randomize")),
                List.of(scriptWithVersion("changedetector_detect", "0.2", "module")));

        List<String> warnings = new ArrayList<>();
        preset.checkScripts(actionBank, warnings);

        assertTrue(warnings.stream().anyMatch(w -> w.contains("prescripts")
                && w.contains("changedetector_setup") && w.contains("0.0")));
        assertTrue(warnings.stream().anyMatch(w -> w.contains("postscripts")
                && w.contains("changedetector_detect") && w.contains("0.2")));
    }

    @Test
    void missingScriptInAppWarns() {
        Preset preset = new Preset("1", List.of(),
                List.of(new ScriptPreset("missing_prescript", "0.1")), List.of());

        ActionBank actionBank = testActionBank(null, null, List.of(), List.of());

        List<String> warnings = new ArrayList<>();
        preset.checkScripts(actionBank, warnings);

        assertTrue(warnings.stream()
                .anyMatch(w -> w.contains("missing_prescript") && w.contains("not loaded")));
    }

    @Test
    void extraScriptInAppWarns() {
        Preset preset = new Preset("1", List.of(), List.of(), List.of());

        ActionBank actionBank = testActionBank(null, null,
                List.of(scriptWithVersion("changedetector_setup", "0.1", "randomize")), List.of());

        List<String> warnings = new ArrayList<>();
        preset.checkScripts(actionBank, warnings);

        assertTrue(warnings.stream().anyMatch(
                w -> w.contains("changedetector_setup") && w.contains("not in the preset")));
    }

    @Test
    void loadWithoutScriptSectionsUsesEmptyScriptLists() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                seed: 1
                actions: []
                """.formatted(PtcgRandomizerVersion.VERSION);
        Path presetFile = tempDir.resolve("preset.yaml");
        Files.writeString(presetFile, yaml);

        List<String> warnings = new ArrayList<>();
        Preset preset = PresetIO.load(presetFile.toFile(), warnings);

        assertTrue(warnings.isEmpty());
        assertTrue(preset.getPreScriptPresets().isEmpty());
        assertTrue(preset.getPostScriptPresets().isEmpty());
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
        Path presetFile = tempDir.resolve("preset.yaml");
        Files.writeString(presetFile, yaml);

        List<String> warnings = new ArrayList<>();
        PresetIO.load(presetFile.toFile(), warnings);

        assertTrue(warnings.stream().anyMatch(w -> w.contains("PtcgRandomizer 0.1.0")));
        assertTrue(warnings.stream().anyMatch(w -> w.contains(PtcgRandomizerVersion.VERSION)));
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
        Path presetFile = tempDir.resolve("preset.yaml");
        Files.writeString(presetFile, yaml);

        List<String> warnings = new ArrayList<>();
        PresetIO.load(presetFile.toFile(), warnings);

        assertTrue(warnings.stream().anyMatch(w -> w.contains("does not record an appVersion")));
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
        Path presetFile = tempDir.resolve("preset.yaml");
        Files.writeString(presetFile, yaml);

        List<String> warnings = new ArrayList<>();
        Preset preset = PresetIO.load(presetFile.toFile(), warnings);
        assertTrue(warnings.isEmpty());
        assertEquals("42", preset.getSeed());
    }

    @Test
    void missingModuleVersionWarnsButStillLoadsAction() {
        Preset preset = new Preset("1",
                List.of(new ActionPreset("shuffle_hp", null, ModuleConfigPreset.empty())),
                List.of(), List.of());

        ActionBank actionBank = testActionBank("shuffle_hp", "0.9", List.of(), List.of());

        List<String> warnings = new ArrayList<>();
        var actions = preset.getActions(actionBank, warnings);

        assertEquals(1, actions.size());
        assertTrue(warnings.stream().anyMatch(w -> w.contains("does not record a version")));
    }

    @Test
    void missingModulesAreSkippedWithWarning() {
        Preset preset = new Preset("1",
                List.of(new ActionPreset("missing_module", null, ModuleConfigPreset.empty())),
                List.of(), List.of());

        ActionBank actionBank = new ActionBank(null) {
            @Override
            public Module getModule(String moduleName) {
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

        List<String> warnings = new ArrayList<>();
        var actions = preset.getActions(actionBank, warnings);

        assertTrue(actions.isEmpty());
        assertTrue(warnings.stream().anyMatch(w -> w.contains("missing_module")));
        assertTrue(warnings.stream().anyMatch(w -> w.contains("skipped")));
    }

    private static ActionBank testActionBank(String moduleName, String version,
            List<Module> preScripts, List<Module> postScripts) {
        return new ActionBank(null) {
            @Override
            public Module getModule(String name) {
                if (moduleName != null && moduleName.equals(name)) {
                    return moduleWithVersion(name, version);
                }
                for (Module script : preScripts) {
                    if (script.getName().equals(name)) {
                        return script;
                    }
                }
                for (Module script : postScripts) {
                    if (script.getName().equals(name)) {
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

    private static Module moduleWithVersion(String name, String version) {
        return new Module(name, "", Set.of("pokemon cards"), Set.of(), List.of(),
                new ZeroArgFunction() {
                    @Override
                    public LuaValue call() {
                        return LuaValue.NIL;
                    }
                }, null, "test.lua", 0, true, true, null, "author", version,
                Map.of("UniversalRandomizerJava", "0.5.0"), null, null, null);
    }

    private static Module scriptWithVersion(String name, String version, String when) {
        return new Module(name, "", Set.of(), Set.of(), List.of(), new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.NIL;
            }
        }, null, "test.lua", 0, false, false, when, "author", version,
                Map.of("UniversalRandomizerJava", "0.5.0"), null, null, null);
    }
}
