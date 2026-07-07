package redactedrice.ptcgr.randomizer.preset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Reads and writes settings/config files as YAML.
 */
public final class PresetIO {
    public static final String FILE_EXTENSION = ".yaml";
    public static final String DEFAULT_BASE_NAME = "ptcgr_configs";

    private PresetIO() {}

    public static void save(File file, RandomizerPreset preset) throws IOException {
        Map<String, Object> root = preset.toDocumentMap();

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("# PTCG Randomizer preset\n");
            yaml.dump(root, writer);
        }
    }

    public static RandomizerPreset load(File file, List<String> warnings)
            throws IOException, PresetLoadException {
        if (warnings == null) {
            throw new IllegalArgumentException("warnings list cannot be null");
        }

        try (FileInputStream input = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(input);
            if (!(loaded instanceof Map<?, ?> loadedMap)) {
                throw new PresetLoadException("Preset file root must be a mapping.");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> root = (Map<String, Object>) loadedMap;
            return RandomizerPreset.fromDocumentMap(root, warnings);
        }
    }

    public static File ensureYamlExtension(File file) {
        String name = file.getName();
        if (name.endsWith(".yml")) {
            return new File(file.getParentFile(),
                    name.substring(0, name.length() - 4) + FILE_EXTENSION);
        }
        if (!name.endsWith(FILE_EXTENSION)) {
            return new File(file.getPath() + FILE_EXTENSION);
        }
        return file;
    }
}
