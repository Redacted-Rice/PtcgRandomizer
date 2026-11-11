package redactedrice.ptcgr.regression;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

final class RegressionYaml {
    private RegressionYaml() {
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> load(File file) throws IOException {
        try (var reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            Object root = new Yaml().load(reader);
            if (root == null) {
                return Map.of();
            }
            if (!(root instanceof Map)) {
                throw new IllegalArgumentException("Expected YAML map in " + file.getName());
            }
            return (Map<String, Object>) root;
        }
    }
}
