package redactedrice.ptcgr.configs;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import redactedrice.ptcgr.utils.FileExtensionUtils;
import redactedrice.randomizer.utils.IssueTracker;

/**
 * Reads and writes files as YAML.
 */
public final class YamlIO {
    public static final String FILE_EXTENSION = ".yaml";
    public static final String DEFAULT_BASE_NAME = "ptcgr_randomize";
    public static final String DEFAULT_FILE_NAME = DEFAULT_BASE_NAME + FILE_EXTENSION;

    private YamlIO() {}

    public static void save(File file, Map<String, Object> contents) throws IOException {
        save(file, contents, "# PTCGR Randomization config\n");
    }

    public static void save(File file, Map<String, Object> contents, String header)
            throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);

        try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write(header);
            yaml.dump(contents, writer);
        }
    }

    public static Map<String, Object> load(File file) throws IOException {
        File yamlFile = FileExtensionUtils.ensureExtension(file, FILE_EXTENSION);
        try (Reader reader =
                Files.newBufferedReader(yamlFile.toPath(), StandardCharsets.UTF_8)) {

            Yaml yaml = new Yaml();
            Object loaded = yaml.load(reader);
            if (!(loaded instanceof Map<?, ?> loadedMap)) {
                IssueTracker
                        .addWarning(yamlFile.getName() + ": config file root must be a mapping.");
                return null;
            }

            // Need an explicit cast to var for the warning suppression to work
            @SuppressWarnings("unchecked")
            Map<String, Object> typedLoadedMap = (Map<String, Object>) loadedMap;
            return typedLoadedMap;
        }
    }
}
