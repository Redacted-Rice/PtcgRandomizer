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

class RulesYamlIOTest {
    @TempDir
    Path tempDir;

    @Test
    void loadReturnsNullForNonMapping() throws IOException {
        Path rulesFile = tempDir.resolve("test.yaml");
        Files.writeString(rulesFile, "[]");

        WarningCollector warnings = new WarningCollector(null);
        assertEquals(null, YamlIO.load(rulesFile.toFile(), warnings));
    }

    @Test
    void saveRoundTripsMapping() throws IOException {
        Map<String, Object> root = RulesConfig.empty().convertToYamlMap();
        Path output = tempDir.resolve("saved_rules.yaml");
        YamlIO.save(output.toFile(), root);

        WarningCollector warnings = new WarningCollector(null);
        Map<String, Object> reloaded = YamlIO.load(output.toFile(), warnings);
        assertEquals(root, reloaded);
    }
}
