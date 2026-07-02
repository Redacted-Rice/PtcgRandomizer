package redactedrice.ptcgr.randomizer.preset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

import redactedrice.ptcgr.randomizer.Settings;

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
    void ensureYamlExtensionNormalizesYml() {
        File file = new File("configs/preset.yml");
        File resolved = PresetIO.ensureYamlExtension(file);
        assertEquals("preset.yaml", resolved.getName());
    }

    @Test
    void presetDocumentIncludesResolvedSeedAndActions() {
        RandomizerPreset preset = new RandomizerPreset("123456789", Collections.emptyList());
        Map<String, Object> document = preset.toDocumentMap();
        assertEquals(1, document.get("version"));
        assertEquals("123456789", document.get("seed"));
        assertEquals(Collections.emptyList(), document.get("actions"));
        assertFalse(document.containsKey("randomizationSettings"));
    }

    @Test
    void saveWritesResolvedSeed() throws Exception {
        Settings settings = new Settings();
        settings.setSeed("random");

        RandomizerPreset preset =
                RandomizerPreset.fromActions(settings.getSeedString(), List.of());
        Path output = tempDir.resolve("preset.yaml");
        PresetIO.save(output.toFile(), preset);

        @SuppressWarnings("unchecked")
        Map<String, Object> loaded = new Yaml().load(Files.readString(output));
        assertEquals(settings.getSeedString(), loaded.get("seed"));
        assertFalse(loaded.get("seed").equals("random"));
    }

    @Test
    void saveWritesYamlFile() throws Exception {
        RandomizerPreset preset = new RandomizerPreset("42", Collections.emptyList());
        Path output = tempDir.resolve("preset.yaml");
        PresetIO.save(output.toFile(), preset);

        String yamlText = Files.readString(output);
        assertTrue(yamlText.startsWith("# PTCG Randomizer preset\n"));

        @SuppressWarnings("unchecked")
        Map<String, Object> loaded = new Yaml().load(yamlText.substring(yamlText.indexOf('\n') + 1));
        assertEquals("42", loaded.get("seed"));
        assertEquals(Collections.emptyList(), loaded.get("actions"));
    }
}
