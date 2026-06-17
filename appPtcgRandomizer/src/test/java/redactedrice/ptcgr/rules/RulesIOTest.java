package redactedrice.ptcgr.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import redactedrice.ptcgr.randomizer.AppResourceInstaller;
import redactedrice.ptcgr.rules.parser.YamlParser;
import redactedrice.ptcgr.rules.support.RulesWarningCollector;
import redactedrice.ptcgr.constants.CardConstants.CardId;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.data.Move;

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

    private MonsterCard someMonster(int level, CardId id, String moveName) {
        MonsterCard card = new MonsterCard();
        card.id = id;
        card.name.setText("SomeMonster");
        card.level = (byte) level;
        Move move = new Move();
        move.name.setText(moveName);
        card.setMoves(List.of(move, new Move()));
        return card;
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
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                """;
        addYamlFromString(io, yaml, "test.yaml", RulesLoadOptions.all());

        assertTrue(warnings.hasWarnings());
    }

    @Test
    void entryContextIncludesFileNameAndEntryPath() {
        assertEquals("sources.yaml:moveExclusions[0]",
                YamlParser.entryContext("sources.yaml", "moveExclusions[0]"));
    }

    @Test
    void rejectsUnknownMoveOnEmptyCardPool() throws IOException {
        RulesWarningCollector warnings = new RulesWarningCollector(null);
        MoveExclusions exclusions = new MoveExclusions();
        MoveAssignments assignments = new MoveAssignments();
        RulesIO io = createIo(exclusions, assignments, warnings);

        String yaml = """
                moveExclusions:
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
        Path defaultFile = tempDir.resolve(AppResourceInstaller.UNSUPPORTED_MOVES_FILE_NAME);
        try (InputStream in =
                getClass().getResourceAsStream(AppResourceInstaller.UNSUPPORTED_MOVES_CLASSPATH)) {
            Files.copy(in, defaultFile);
        }

        RulesWarningCollector warnings = new RulesWarningCollector(null);
        MoveExclusions exclusions = new MoveExclusions();
        MoveAssignments assignments = new MoveAssignments();
        RulesIO io = createIo(exclusions, assignments, warnings);

        io.addFromFile(defaultFile.toFile(), AppResourceInstaller.UNSUPPORTED_MOVES_FILE_NAME,
                RulesLoadOptions.exclusionsOnly());

        assertTrue(warnings.hasWarnings());
    }

    @Test
    void rulesCombinesMultipleRuleFiles() throws IOException {
        Rules rules = new Rules(new CardGroup<MonsterCard>(), null, null);

        Path firstFile = tempDir.resolve("base_rules.yaml");
        Files.writeString(firstFile, "moveExclusions: []\n");

        Path secondFile = tempDir.resolve("extra_rules.yaml");
        Files.writeString(secondFile, "moveAssignments: []\n");

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
                moveExclusions: []
                moveAssignments:
                  - to_card: SomeMonster lvl76
                    to_move_slot: 1
                    from_card: OtherMonster lvl40
                    move: TestMove
                """;
        addYamlFromString(io, yaml, "test.yaml", RulesLoadOptions.exclusionsOnly());

        assertTrue(assignments.getAllAssignments().isEmpty());
    }

    @Test
    void rejectsCardWithoutDisambiguator() throws IOException {
        RulesWarningCollector warnings = new RulesWarningCollector(null);
        MoveExclusions exclusions = new MoveExclusions();
        MoveAssignments assignments = new MoveAssignments();
        RulesIO io = createIo(exclusions, assignments, warnings);

        String yaml = """
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    card: SomeMonster
                    move: TestMove
                """;
        addYamlFromString(io, yaml, "test.yaml", RulesLoadOptions.all());

        assertTrue(warnings.hasWarnings());
        assertTrue(exclusions.getAllExclusions().isEmpty());
    }

    @Test
    void rejectsPrintNumberCardSpecifier() throws IOException {
        RulesWarningCollector warnings = new RulesWarningCollector(null);
        MoveExclusions exclusions = new MoveExclusions();
        MoveAssignments assignments = new MoveAssignments();
        RulesIO io = createIo(exclusions, assignments, warnings);

        String yaml = """
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    card: SomeMonster_1
                    move: TestMove
                """;
        addYamlFromString(io, yaml, "test.yaml", RulesLoadOptions.all());

        assertTrue(warnings.hasWarnings());
        assertTrue(exclusions.getAllExclusions().isEmpty());
    }

    @Test
    void resolvesCardByLevelSpecifier() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));
        cards.add(someMonster(37, CardId.MONSTER_146_2, "OtherMove"));

        RulesWarningCollector warnings = new RulesWarningCollector(null);
        MoveExclusions exclusions = new MoveExclusions();
        MoveAssignments assignments = new MoveAssignments();
        RulesIO io = new RulesIO(cards, exclusions, assignments, warnings);

        String yaml = """
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    card: SomeMonster lvl35
                    move: TestMove
                """;
        addYamlFromString(io, yaml, "test.yaml", RulesLoadOptions.all());

        assertEquals(1, exclusions.getAllExclusions().size());
        assertEquals(CardId.MONSTER_146_1, exclusions.getAllExclusions().get(0).getCardId());
    }
}
