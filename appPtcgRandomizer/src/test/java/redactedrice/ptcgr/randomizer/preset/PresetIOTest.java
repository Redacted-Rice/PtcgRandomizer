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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;
import redactedrice.ptcgr.constants.PtcgRandomizerVersion;
import redactedrice.ptcgr.randomizer.Settings;
import redactedrice.ptcgr.randomizer.actions.ActionBank;

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
    void presetDocumentIncludesAppVersionSeedAndActions() {
        Preset preset = new Preset("123456789", Collections.emptyList());
        Map<String, Object> document = preset.prepForSave();
        assertEquals(Preset.CURRENT_FORMAT_VERSION, document.get("version"));
        assertEquals(PtcgRandomizerVersion.VERSION, document.get("appVersion"));
        assertEquals("123456789", document.get("seed"));
        assertEquals(Collections.emptyList(), document.get("actions"));
        assertFalse(document.containsKey("randomizationSettings"));
    }

    @Test
    void saveWritesResolvedSeedAndAppVersion() throws Exception {
        Settings settings = new Settings();
        settings.setSeed("random");

        Preset preset = Preset.fromAppState(settings.getSeedString(), List.of());
        Path output = tempDir.resolve("preset.yaml");
        PresetIO.save(output.toFile(), preset);

        @SuppressWarnings("unchecked")
        Map<String, Object> loaded = new Yaml().load(Files.readString(output));
        assertEquals(PtcgRandomizerVersion.VERSION, loaded.get("appVersion"));
        assertFalse(loaded.get("seed").equals("random"));
    }

    @Test
    void saveWritesYamlFile() throws Exception {
        Preset preset = new Preset("42", Collections.emptyList());
        Path output = tempDir.resolve("preset.yaml");
        PresetIO.save(output.toFile(), preset);

        String yamlText = Files.readString(output);
        assertTrue(yamlText.startsWith("# PTCG Randomizer preset\n"));

        @SuppressWarnings("unchecked")
        Map<String, Object> loaded =
                new Yaml().load(yamlText.substring(yamlText.indexOf('\n') + 1));
        assertEquals("42", loaded.get("seed"));
        assertEquals(Collections.emptyList(), loaded.get("actions"));
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
        assertEquals(ActionPreset.UNKNOWN_VERSION, node.get("version"));
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
    void loadRestoresSeedActionsAndSeedOffsetWithoutModuleVersion() throws Exception {
        String yaml = """
                version: 1
                appVersion: %s
                seed: 987654321
                actions:
                  - module: shuffle_hp
                    config:
                      seedOffset: 12
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
                List.of(new ActionPreset("shuffle_hp", "0.1", ModuleConfigPreset.empty())));

        ActionBank actionBank = testActionBank("shuffle_hp", "0.9");

        List<String> warnings = new ArrayList<>();
        var actions = preset.getActions(actionBank, warnings);

        assertEquals(1, actions.size());
        assertTrue(warnings.stream().anyMatch(w -> w.contains("was saved as version 0.1")));
        assertTrue(warnings.stream().anyMatch(w -> w.contains("current version is 0.9")));
        assertFalse(warnings.stream().anyMatch(w -> w.contains("appVersion")));
    }

    @Test
    void appVersionMismatchWarnsOnLoad() throws Exception {
        String yaml = """
                version: 1
                appVersion: 0.1.0
                seed: 1
                actions: []
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
                List.of(new ActionPreset("shuffle_hp", null, ModuleConfigPreset.empty())));

        ActionBank actionBank = testActionBank("shuffle_hp", "0.9");

        List<String> warnings = new ArrayList<>();
        var actions = preset.getActions(actionBank, warnings);

        assertEquals(1, actions.size());
        assertTrue(warnings.stream().anyMatch(w -> w.contains("does not record a version")));
    }

    @Test
    void missingModulesAreSkippedWithWarning() {
        Preset preset = new Preset("1",
                List.of(new ActionPreset("missing_module", null, ModuleConfigPreset.empty())));

        ActionBank actionBank = new ActionBank(null) {
            @Override
            public redactedrice.randomizer.lua.Module getModule(String moduleName) {
                return null;
            }
        };

        List<String> warnings = new ArrayList<>();
        var actions = preset.getActions(actionBank, warnings);

        assertTrue(actions.isEmpty());
        assertTrue(warnings.stream().anyMatch(w -> w.contains("missing_module")));
        assertTrue(warnings.stream().anyMatch(w -> w.contains("skipped")));
    }

    private static ActionBank testActionBank(String moduleName, String version) {
        return new ActionBank(null) {
            @Override
            public redactedrice.randomizer.lua.Module getModule(String name) {
                return moduleName.equals(name) ? moduleWithVersion(name, version) : null;
            }
        };
    }

    private static redactedrice.randomizer.lua.Module moduleWithVersion(String name,
            String version) {
        return new redactedrice.randomizer.lua.Module(name, "", java.util.Set.of("pokemon cards"),
                java.util.Set.of(), java.util.List.of(), new org.luaj.vm2.lib.ZeroArgFunction() {
                    @Override
                    public org.luaj.vm2.LuaValue call() {
                        return org.luaj.vm2.LuaValue.NIL;
                    }
                }, null, "test.lua", 0, true, true, null, "author", version,
                java.util.Map.of("UniversalRandomizerJava", "0.5.0"), null, null, null);
    }
}
