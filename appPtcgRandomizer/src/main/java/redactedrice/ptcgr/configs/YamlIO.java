package redactedrice.ptcgr.configs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import redactedrice.ptcgr.utils.FileExtensionUtils;
import redactedrice.ptcgr.utils.WarningCollector;

/**
 * Reads and writes files as YAML.
 */
public final class YamlIO {
    public static final String FILE_EXTENSION = ".yaml";
    public static final String DEFAULT_BASE_NAME = "ptcgr_configs";
    public static final String DEFAULT_FILE_NAME = DEFAULT_BASE_NAME + FILE_EXTENSION;

    private YamlIO() {}

    public static void save(File file, Map<String, Object> contents) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("# PTCG Randomizer config\n");
            yaml.dump(contents, writer);
        }
    }

    public static Map<String, Object> load(File file, WarningCollector warnings)
            throws IOException {
        File yamlFile = FileExtensionUtils.ensureExtension(file, FILE_EXTENSION);
        try (FileInputStream input = new FileInputStream(yamlFile)) {

            Yaml yaml = new Yaml();
            Object loaded = yaml.load(input);
            if (!(loaded instanceof Map<?, ?> loadedMap)) {
                warnings.addWarning(yamlFile.getName() + ": config file root must be a mapping.");
                return null;
            }

            // Need an explicit cast to var for the warning suppression to work
            @SuppressWarnings("unchecked")
            Map<String, Object> typedLoadedMap = (Map<String, Object>) loadedMap;
            return typedLoadedMap;
        }
    }
}
