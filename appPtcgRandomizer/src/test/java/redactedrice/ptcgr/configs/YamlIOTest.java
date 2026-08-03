package redactedrice.ptcgr.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

import redactedrice.ptcgr.configs.rules.RulesConfig;
import redactedrice.ptcgr.constants.PtcgRandomizerVersion;
import redactedrice.ptcgr.utils.FileExtensionUtils;
import redactedrice.randomizer.utils.IssueTracker;

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
    void ensureYamlExtensionNormalizesYml() {
        File file = new File("configs/config.yml");
        File resolved = FileExtensionUtils.ensureExtension(file, YamlIO.FILE_EXTENSION);
        assertEquals("config.yaml", resolved.getName());
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

        IssueTracker.clear();
        Config config = Config.readFromLoadedYamlMap(
                YamlIO.load(tempDir.resolve("ptcgr_configs").toFile()),
                tempDir.resolve("ptcgr_configs.yaml").getFileName().toString());

        assertTrue(!IssueTracker.hasWarnings());
        assertEquals("7", config.getSeed());
    }

    @Test
    void saveWritesConfigHeaderAndRoundTripsMapping() throws Exception {
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
    void loadReturnsNullForNonMapping() throws IOException {
        Path invalidFile = tempDir.resolve("invalid.yaml");
        Files.writeString(invalidFile, "[]");

        IssueTracker.clear();
        assertEquals(null, YamlIO.load(invalidFile.toFile()));
    }

    @Test
    void saveRoundTripsRulesMapping() throws IOException {
        Map<String, Object> root = RulesConfig.empty().convertToYamlMap();
        Path output = tempDir.resolve("saved_rules.yaml");
        YamlIO.save(output.toFile(), root);

        IssueTracker.clear();
        Map<String, Object> reloaded = YamlIO.load(output.toFile());
        assertEquals(root, reloaded);
    }
}
