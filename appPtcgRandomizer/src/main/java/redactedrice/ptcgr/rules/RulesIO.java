package redactedrice.ptcgr.rules;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import redactedrice.ptcgr.rules.parser.MoveAssignmentsYamlParser;
import redactedrice.ptcgr.rules.parser.MoveExclusionsYamlParser;
import redactedrice.ptcgr.rules.parser.YamlParser;
import redactedrice.ptcgr.rules.support.RulesWarningCollector;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;

/**
 * Reads YAML rules and merges them into their respective container classes
 *
 * Standalone rules files use a root mapping with the sub-rule nodes. For other YAML documents (e.g.
 * saves), extract the rules node first and pass it to addRulesNode.
 */
public final class RulesIO {
    private final CardGroup<MonsterCard> allCards;
    private final MoveExclusions moveExclusions;
    private final MoveAssignments moveAssignments;
    private final RulesWarningCollector warnings;
    private final List<String> addedRuleFiles = new ArrayList<>();

    public RulesIO(CardGroup<MonsterCard> allCards, MoveExclusions moveExclusions,
            MoveAssignments moveAssignments, RulesWarningCollector warnings) {
        this.allCards = allCards;
        this.moveExclusions = moveExclusions;
        this.moveAssignments = moveAssignments;
        this.warnings = warnings;
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
            MoveExclusionsYamlParser.loadList(rulesNode.get("moveExclusions"), sourceFileName,
                    moveExclusions, parser, warnings);
        }
        if (options.loadAssignments()) {
            MoveAssignmentsYamlParser.loadList(rulesNode.get("moveAssignments"), sourceFileName,
                    moveAssignments, parser, warnings);
        }
    }
}
