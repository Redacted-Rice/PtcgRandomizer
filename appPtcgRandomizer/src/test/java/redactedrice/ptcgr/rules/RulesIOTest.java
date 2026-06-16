package redactedrice.ptcgr.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import redactedrice.ptcgr.rules.parser.YamlParser;
import redactedrice.ptcgr.rules.support.RulesWarningCollector;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;

class RulesIOTest {
    @TempDir
    Path tempDir;

    private RulesIO createIo(MoveExclusions exclusions, MoveAssignments assignments,
            RulesWarningCollector warnings) {
        return new RulesIO(new CardGroup<MonsterCard>(), exclusions, assignments, warnings);
    }

    private void addYamlFromString(RulesIO io, String yaml, String sourceFileName,
            RulesLoadOptions options) throws IOException {
        Path rulesFile = tempDir.resolve(sourceFileName);
        Files.writeString(rulesFile, yaml);
        io.addFromFile(rulesFile.toFile(), sourceFileName, options);
    }

    @Test
    void rejectsNonMappingRoot() throws IOException {
        RulesWarningCollector warnings = new RulesWarningCollector(null);
        MoveExclusions exclusions = new MoveExclusions();
        MoveAssignments assignments = new MoveAssignments();
        RulesIO io = createIo(exclusions, assignments, warnings);

        addYamlFromString(io, "[]", "test.yaml", RulesLoadOptions.all());

        assertTrue(warnings.hasWarnings());
    }

    @Test
    void rejectsExclusionWithoutMove() throws IOException {
        RulesWarningCollector warnings = new RulesWarningCollector(null);
        MoveExclusions exclusions = new MoveExclusions();
        MoveAssignments assignments = new MoveAssignments();
        RulesIO io = createIo(exclusions, assignments, warnings);

        String yaml = """
                exclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                """;
        addYamlFromString(io, yaml, "test.yaml", RulesLoadOptions.all());

        assertTrue(warnings.hasWarnings());
    }

    @Test
    void entryContextIncludesFileNameAndEntryPath() {
        assertEquals("sources.yaml:exclusions[0]",
                YamlParser.entryContext("sources.yaml", "exclusions[0]"));
    }

    @Test
    void rejectsUnknownMoveOnEmptyCardPool() throws IOException {
        RulesWarningCollector warnings = new RulesWarningCollector(null);
        MoveExclusions exclusions = new MoveExclusions();
        MoveAssignments assignments = new MoveAssignments();
        RulesIO io = createIo(exclusions, assignments, warnings);

        String yaml = """
                exclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    move: Ember
                """;
        addYamlFromString(io, yaml, "sources.yaml", RulesLoadOptions.all());

        assertTrue(warnings.hasWarnings());
        assertTrue(exclusions.getAllExclusions().isEmpty());
    }

    @Test
    void loadsUnsupportedMovesResource() throws IOException {
        Path defaultFile = tempDir.resolve(RulesIO.UNSUPPORTED_MOVES_FILE_NAME);
        try (InputStream in = getClass().getResourceAsStream(RulesIO.UNSUPPORTED_MOVES_RESOURCE)) {
            Files.copy(in, defaultFile);
        }

        RulesWarningCollector warnings = new RulesWarningCollector(null);
        MoveExclusions exclusions = new MoveExclusions();
        MoveAssignments assignments = new MoveAssignments();
        RulesIO io = createIo(exclusions, assignments, warnings);

        io.addFromFile(defaultFile.toFile(), RulesIO.UNSUPPORTED_MOVES_FILE_NAME,
                RulesLoadOptions.exclusionsOnly());

        assertTrue(warnings.hasWarnings());
    }

    @Test
    void rulesCombinesMultipleRuleFiles() throws IOException {
        Rules rules = new Rules(new CardGroup<MonsterCard>(), null, false);

        Path firstFile = tempDir.resolve("base_rules.yaml");
        Files.writeString(firstFile, "exclusions: []\n");

        Path secondFile = tempDir.resolve("extra_rules.yaml");
        Files.writeString(secondFile, "assignments: []\n");

        rules.getIo().addRulesFile(firstFile.toFile());
        rules.getIo().addRulesFile(secondFile.toFile());

        assertEquals(2, rules.getIo().getAddedRuleFiles().size());
        assertTrue(rules.getMoveAssignments().getAllAssignments().isEmpty());
    }

    @Test
    void selectiveLoadSkipsAssignments() throws IOException {
        RulesWarningCollector warnings = new RulesWarningCollector(null);
        MoveExclusions exclusions = new MoveExclusions();
        MoveAssignments assignments = new MoveAssignments();
        RulesIO io = createIo(exclusions, assignments, warnings);

        String yaml = """
                exclusions: []
                assignments:
                  - to_card: Charizard_1
                    to_move_slot: 1
                    from_card: Blastoise_1
                    move: Hydro Pump
                """;
        addYamlFromString(io, yaml, "test.yaml", RulesLoadOptions.exclusionsOnly());

        assertTrue(assignments.getAllAssignments().isEmpty());
    }

    @Test
    void rejectsCardWithoutVersionNumber() throws IOException {
        RulesWarningCollector warnings = new RulesWarningCollector(null);
        MoveExclusions exclusions = new MoveExclusions();
        MoveAssignments assignments = new MoveAssignments();
        RulesIO io = createIo(exclusions, assignments, warnings);

        String yaml = """
                exclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    card: Charizard
                    move: Ember
                """;
        addYamlFromString(io, yaml, "test.yaml", RulesLoadOptions.all());

        assertTrue(warnings.hasWarnings());
        assertTrue(exclusions.getAllExclusions().isEmpty());
    }
}
