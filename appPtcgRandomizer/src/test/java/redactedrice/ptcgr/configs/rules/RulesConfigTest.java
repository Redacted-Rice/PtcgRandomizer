package redactedrice.ptcgr.configs.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import redactedrice.ptcgr.configs.ParserHelpers;
import redactedrice.ptcgr.configs.YamlIO;
import redactedrice.ptcgr.constants.CardConstants.CardId;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.data.Move;
import redactedrice.ptcgr.resources.PtcgBundledResources;
import redactedrice.ptcgr.rules.Rules;
import redactedrice.ptcgr.utils.WarningCollector;

class RulesConfigTest {
    @TempDir
    Path tempDir;

    private void writeYaml(String yaml, String sourceFileName) throws IOException {
        Path rulesFile = tempDir.resolve(sourceFileName);
        Files.writeString(rulesFile, yaml);
    }

    private RulesConfig readYaml(String yaml, String sourceFileName, WarningCollector warnings)
            throws IOException {
        writeYaml(yaml, sourceFileName);
        Map<String, Object> node =
                YamlIO.load(tempDir.resolve(sourceFileName).toFile(), warnings);
        return RulesConfig.readFromLoadedYamlMap(node, sourceFileName, warnings);
    }

    private MonsterCard someMonster(int level, CardId id, String moveName) {
        MonsterCard card = new MonsterCard();
        card.id = id;
        card.name.setText("SomeMonster");
        card.level = (byte) level;
        Move move = card.getMove(0);
        move.name.setText(moveName);
        card.setMoves(List.of(move, card.getMove(1)));
        return card;
    }

    @Test
    void entryContextIncludesFileNameAndEntryPath() {
        assertEquals("sources.yaml:moveExclusions[0]",
                ParserHelpers.entryContext("sources.yaml", "moveExclusions[0]"));
    }

    @Test
    void rejectsNonMappingRoot() throws IOException {
        WarningCollector warnings = new WarningCollector(null);
        readYaml("[]", "test.yaml", warnings);

        assertTrue(warnings.getWarnings().stream().anyMatch(w -> w.contains("must be a mapping")));
    }

    @Test
    void rejectsExclusionWithoutMove() throws IOException {
        WarningCollector warnings = new WarningCollector(null);
        String yaml = """
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                """;
        RulesConfig config = readYaml(yaml, "test.yaml", warnings);

        assertTrue(warnings.getWarnings().stream()
                .anyMatch(w -> w.contains("missing required field \"move\"")));
        assertTrue(config.getMoveExclusionConfigs().isEmpty());
    }

    @Test
    void rejectsUnknownMoveOnEmptyCardPool() throws IOException {
        WarningCollector warnings = new WarningCollector(null);
        String yaml = """
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    move: Ember
                """;
        RulesConfig config = readYaml(yaml, "sources.yaml", warnings);

        Rules rules = new Rules();
        config.applyTo(rules, new CardGroup<>(), warnings);

        assertTrue(warnings.getWarnings().stream()
                .anyMatch(w -> w.contains("failed to find any card")));
        assertTrue(rules.getMoveExclusions().getAllExclusions().isEmpty());
    }

    @Test
    void loadsUnsupportedMovesResource() throws IOException {
        Path defaultFile = tempDir.resolve(PtcgBundledResources.UNSUPPORTED_MOVES_FILE_NAME);
        try (InputStream in =
                getClass().getResourceAsStream(PtcgBundledResources.UNSUPPORTED_MOVES_CLASSPATH)) {
            Files.copy(in, defaultFile);
        }

        WarningCollector warnings = new WarningCollector(null);
        RulesConfig config = RulesConfig.readFromLoadedYamlMap(
                YamlIO.load(defaultFile.toFile(), warnings), defaultFile.getFileName().toString(),
                warnings);
        Rules rules = new Rules();
        config.applyTo(rules, new CardGroup<>(), warnings);

        assertTrue(warnings.getWarnings().stream().anyMatch(w -> !w.isBlank()));
    }

    @Test
    void mergedConfigsCombineRuleFiles() throws IOException {
        WarningCollector warnings = new WarningCollector(null);
        RulesConfig first = readYaml("moveExclusions: []\n", "base_rules.yaml", warnings);
        RulesConfig second = readYaml("moveAssignments: []\n", "extra_rules.yaml", warnings);
        RulesConfig combined = first.mergedWith(second);

        assertTrue(combined.getMoveExclusionConfigs().isEmpty());
        assertTrue(combined.getMoveAssignmentConfigs().isEmpty());
    }

    @Test
    void rejectsCardWithoutDisambiguator() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        WarningCollector warnings = new WarningCollector(null);
        String yaml = """
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    card: SomeMonster
                    move: TestMove
                """;
        RulesConfig config = readYaml(yaml, "test.yaml", warnings);
        Rules rules = new Rules();
        config.applyTo(rules, cards, warnings);

        assertTrue(warnings.getWarnings().stream()
                .anyMatch(w -> w.contains("must use name and level")));
        assertTrue(rules.getMoveExclusions().getAllExclusions().isEmpty());
    }

    @Test
    void rejectsPrintNumberCardSpecifier() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        WarningCollector warnings = new WarningCollector(null);
        String yaml = """
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    card: SomeMonster_1
                    move: TestMove
                """;
        RulesConfig config = readYaml(yaml, "test.yaml", warnings);
        Rules rules = new Rules();
        config.applyTo(rules, cards, warnings);

        assertTrue(warnings.getWarnings().stream()
                .anyMatch(w -> w.contains("must use name and level")));
        assertTrue(rules.getMoveExclusions().getAllExclusions().isEmpty());
    }

    @Test
    void resolvesCardByLevelSpecifier() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));
        cards.add(someMonster(37, CardId.MONSTER_146_2, "OtherMove"));

        WarningCollector warnings = new WarningCollector(null);
        String yaml = """
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    card: SomeMonster lvl35
                    move: TestMove
                """;
        RulesConfig config = readYaml(yaml, "test.yaml", warnings);
        Rules rules = new Rules();
        config.applyTo(rules, cards, warnings);

        assertEquals(1, rules.getMoveExclusions().getAllExclusions().size());
        assertEquals(CardId.MONSTER_146_1,
                rules.getMoveExclusions().getAllExclusions().get(0).getCardId());
    }

    @Test
    void convertToYamlMapRoundTripsRulesNode() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));

        WarningCollector warnings = new WarningCollector(null);
        String yaml = """
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    card: SomeMonster lvl35
                    move: TestMove
                moveAssignments:
                  - to_card: SomeMonster lvl35
                    to_move_slot: 1
                    move: TestMove
                """;
        RulesConfig config = readYaml(yaml, "rules.yaml", warnings);
        RulesConfig saved = RulesConfig.readFromLoadedYamlMap(config.convertToYamlMap(),
                "config.yaml", warnings);

        Rules rules = new Rules();
        saved.applyTo(rules, cards, warnings);

        assertEquals(1, rules.getMoveExclusions().getAllExclusions().size());
        assertEquals(1, rules.getMoveAssignments().getAllAssignments().size());
        assertEquals(0, rules.getMoveAssignments().getAllAssignments().get(0).getMoveSlot());
    }

    @Test
    void moveAssignmentSlotUsesOneBasedYamlAndZeroBasedStorage() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));
        cards.add(someMonster(37, CardId.MONSTER_146_2, "OtherMove"));

        WarningCollector warnings = new WarningCollector(null);
        RulesConfig slotOne = readYaml("""
                moveAssignments:
                  - to_card: SomeMonster lvl35
                    to_move_slot: 1
                    move: TestMove
                """, "slot_one.yaml", warnings);
        Rules rules = new Rules();
        slotOne.applyTo(rules, cards, warnings);

        assertEquals(0, rules.getMoveAssignments().getAllAssignments().get(0).getMoveSlot());
        assertEquals("1", slotOne.getMoveAssignmentConfigs().get(0).getToMoveSlot());

        RulesConfig slotTwo = readYaml("""
                moveAssignments:
                  - to_card: SomeMonster lvl37
                    to_move_slot: 2
                    move: OtherMove
                """, "slot_two.yaml", warnings);
        slotTwo.applyTo(rules, cards, warnings);

        assertEquals(1, rules.getMoveAssignments().getAllAssignments().get(1).getMoveSlot());
        MoveAssignmentConfig serialized = MoveAssignmentConfig.fromMoveAssignment(
                rules.getMoveAssignments().getAllAssignments().get(1), cards);
        assertEquals("2", serialized.getToMoveSlot());
        assertEquals(2, serialized.convertToYamlMap().get("to_move_slot"));
    }

    @Test
    void rejectsZeroBasedMoveSlotInYaml() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));

        WarningCollector warnings = new WarningCollector(null);
        RulesConfig config = readYaml("""
                moveAssignments:
                  - to_card: SomeMonster lvl35
                    to_move_slot: 0
                    move: TestMove
                """, "bad_slot.yaml", warnings);
        Rules rules = new Rules();
        config.applyTo(rules, cards, warnings);

        assertTrue(warnings.getWarnings().stream().anyMatch(w -> w.contains("out of range")));
        assertTrue(rules.getMoveAssignments().getAllAssignments().isEmpty());
    }

    @Test
    void convertToYamlMapIncludesEmptyAssignments() throws IOException {
        WarningCollector warnings = new WarningCollector(null);
        RulesConfig config = readYaml("""
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    move: TestMove
                """, "rules.yaml", warnings);

        assertEquals(1, config.getMoveExclusionConfigs().size());
        assertTrue(config.getMoveAssignmentConfigs().isEmpty());
        assertEquals(List.of(), config.convertToYamlMap().get("moveAssignments"));
    }

    @Test
    void rejectsConflictingMoveAssignmentsFromYaml() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        MonsterCard card = someMonster(35, CardId.MONSTER_146_1, "TestMove");
        Move otherMove = card.getMove(1);
        otherMove.name.setText("OtherMove");
        card.setMoves(List.of(card.getMove(0), otherMove));
        cards.add(card);

        WarningCollector warnings = new WarningCollector(null);
        RulesConfig config = readYaml("""
                moveAssignments:
                  - to_card: SomeMonster lvl35
                    to_move_slot: 1
                    move: TestMove
                  - to_card: SomeMonster lvl35
                    to_move_slot: 1
                    move: OtherMove
                """, "conflict.yaml", warnings);
        Rules rules = new Rules();
        config.applyTo(rules, cards, warnings);

        assertEquals(1, rules.getMoveAssignments().getAllAssignments().size());
        assertTrue(warnings.getWarnings().stream()
                .anyMatch(w -> w.contains("Conflicting assignment")));
    }

    @Test
    void recreateRulesClearsExistingRules() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));

        WarningCollector warnings = new WarningCollector(null);
        Rules rules = new Rules();
        rules.getMoveExclusions().addMoveExclusion(CardId.NO_CARD, "OldMove", true, true,
                "old.yaml", cards, rules.getMoveAssignments());
        rules.getMoveAssignments().addMoveAssignment(cards.withId(CardId.MONSTER_146_1), 0,
                cards.withId(CardId.MONSTER_146_1).getMoveWithName("TestMove"), "old.yaml");

        RulesConfig replacement = RulesConfig.empty();
        replacement.recreateRules(rules, cards, warnings);

        assertTrue(rules.getMoveExclusions().getAllExclusions().isEmpty());
        assertTrue(rules.getMoveAssignments().getAllAssignments().isEmpty());
    }

    @Test
    void saveToFileRoundTripsConfig() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));

        WarningCollector warnings = new WarningCollector(null);
        RulesConfig config = readYaml("""
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    card: SomeMonster lvl35
                    move: TestMove
                moveAssignments: []
                """, "rules.yaml", warnings);

        Path output = tempDir.resolve("saved_rules.yaml");
        YamlIO.save(output.toFile(), config.convertToYamlMap());

        RulesConfig reloaded = RulesConfig.readFromLoadedYamlMap(
                YamlIO.load(output.toFile(), warnings), output.getFileName().toString(), warnings);
        Rules rules = new Rules();
        reloaded.applyTo(rules, cards, warnings);

        assertEquals(1, rules.getMoveExclusions().getAllExclusions().size());
    }
}
