package redactedrice.ptcgr.configs.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import redactedrice.ptcgr.configs.YamlIO;
import redactedrice.ptcgr.utils.WarningCollector;

class RulesConfigIOTest {
    @TempDir
    Path tempDir;

    @Test
    void yamlLoadAndSaveRoundTripMapping() throws IOException {
        Path invalidFile = tempDir.resolve("invalid.yaml");
        Files.writeString(invalidFile, "[]");

        WarningCollector warnings = new WarningCollector(null);
        assertEquals(null, YamlIO.load(invalidFile.toFile(), warnings));

        Map<String, Object> root = RulesConfig.empty().convertToYamlMap();
        Path output = tempDir.resolve("saved_rules.yaml");
        YamlIO.save(output.toFile(), root);

        Map<String, Object> reloaded = YamlIO.load(output.toFile(), warnings);
        assertEquals(root, reloaded);
    }
}
