package redactedrice.ptcgr.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import redactedrice.gbcframework.utils.IOUtils;
import redactedrice.ptcgr.config.parser.MoveAssignmentsYamlParser;
import redactedrice.ptcgr.config.parser.MoveExclusionsYamlParser;
import redactedrice.ptcgr.config.parser.YamlParser;
import redactedrice.ptcgr.config.support.ConfigWarningCollector;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;

/**
 * Reads YAML rules and merges them into their respective container classes
 *
 * Standalone rules files use a root mapping with the sub-rule nodes. For
 * other YAML documents (e.g. saves), extract the rules node first and pass it to
 * addRulesNode.
 */
public final class RulesIO {
    public static final String UNSUPPORTED_MOVES_RESOURCE = "/config/unsupported_moves.yaml";
    public static final String UNSUPPORTED_MOVES_FILE_NAME = "unsupported_moves.yaml";
    public static final String RULES_INSTALL_DIR = "configs";

    private final CardGroup<MonsterCard> allCards;
    private final MoveExclusions moveExclusions;
    private final MoveAssignments moveAssignments;
    private final ConfigWarningCollector warnings;
    private final List<String> addedRuleFiles = new ArrayList<>();

    public RulesIO(CardGroup<MonsterCard> allCards, MoveExclusions moveExclusions,
            MoveAssignments moveAssignments, ConfigWarningCollector warnings) {
        this.allCards = allCards;
        this.moveExclusions = moveExclusions;
        this.moveAssignments = moveAssignments;
        this.warnings = warnings;
    }

    public void addUnsupportedMoves() {
        try {
            File defaultFile = ensureUnsupportedMovesFilePresent();
            addRulesFile(defaultFile, RulesLoadOptions.exclusionsOnly());
        } catch (IOException e) {
            warnings.appendWarning("Unexpected IO Exception reading " + UNSUPPORTED_MOVES_FILE_NAME
                    + " - information likely was not read in successfully: " + e.getMessage());
            displayWarnings();
        }
    }

    public void addRulesFile(File rulesFile) {
        addRulesFile(rulesFile, RulesLoadOptions.all());
    }

    public void addRulesFile(File rulesFile, RulesLoadOptions options) {
        if (rulesFile == null) {
            return;
        }

        String sourceFileName = rulesFile.getName();
        try {
            addFromFile(rulesFile, sourceFileName, options);
            addedRuleFiles.add(rulesFile.getPath());
        } catch (IOException e) {
            warnings.appendWarning("Unexpected IO Exception reading " + sourceFileName
                    + " - information likely was not read in successfully: " + e.getMessage());
        }
    }

    public void displayWarnings() {
        warnings.displayWarningsIfPresent("loaded rules files");
    }

    public List<String> getAddedRuleFiles() {
        return Collections.unmodifiableList(addedRuleFiles);
    }

    public static File ensureUnsupportedMovesFilePresent() throws IOException {
        File file =
                new File(RULES_INSTALL_DIR + IOUtils.FILE_SEPARATOR + UNSUPPORTED_MOVES_FILE_NAME);
        file.getParentFile().mkdirs();
        if (file.createNewFile()) {
            try (InputStream fileIn = RulesIO.class.getResourceAsStream(UNSUPPORTED_MOVES_RESOURCE);
                    OutputStream fileOut = new FileOutputStream(file)) {
                if (fileIn == null) {
                    throw new IOException(
                            "Unsupported moves resource not found: " + UNSUPPORTED_MOVES_RESOURCE);
                }
                byte[] readBuffer = new byte[2048];
                int lengthToRead;
                while ((lengthToRead = fileIn.read(readBuffer)) != -1) {
                    fileOut.write(readBuffer, 0, lengthToRead);
                }
            }
        }
        return file;
    }

    public void addFromFile(File rulesFile, String sourceFileName, RulesLoadOptions options)
            throws IOException {
        try (FileInputStream input = new FileInputStream(rulesFile)) {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(input);
            if (loaded == null) {
                return;
            }
            if (!(loaded instanceof Map)) {
                warnings.appendWarningLine(sourceFileName + ": rules file root must be a mapping.");
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> root = (Map<String, Object>) loaded;
            addRulesNode(root, sourceFileName, options);
        }
    }

    public void addRulesNode(Map<String, Object> rulesNode, String sourceFileName,
            RulesLoadOptions options) {
        if (rulesNode == null) {
            return;
        }

        YamlParser parser = new YamlParser(allCards, warnings);
        if (options.loadExclusions()) {
            MoveExclusionsYamlParser.loadList(rulesNode.get("exclusions"), sourceFileName,
                    moveExclusions, parser, warnings);
        }
        if (options.loadAssignments()) {
            MoveAssignmentsYamlParser.loadList(rulesNode.get("assignments"), sourceFileName,
                    moveAssignments, parser, warnings);
        }
    }
}
