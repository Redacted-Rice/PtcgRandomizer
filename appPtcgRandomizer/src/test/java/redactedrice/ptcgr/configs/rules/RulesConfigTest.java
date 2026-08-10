package redactedrice.ptcgr.configs.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import redactedrice.ptcgr.constants.romenums.CardId;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.data.Move;
import redactedrice.ptcgr.resources.PtcgBundledResources;
import redactedrice.ptcgr.rules.MoveAssignment;
import redactedrice.ptcgr.rules.MoveExclusion;
import redactedrice.ptcgr.rules.Rules;
import redactedrice.randomizer.utils.IssueTracker;

class RulesConfigTest {
    @TempDir
    Path tempDir;

    private void writeYaml(String yaml, String sourceFileName) throws IOException {
        Path rulesFile = tempDir.resolve(sourceFileName);
        Files.writeString(rulesFile, yaml);
    }

    private RulesConfig readYaml(String yaml, String sourceFileName)
            throws IOException {
        writeYaml(yaml, sourceFileName);
        Map<String, Object> node =
                YamlIO.load(tempDir.resolve(sourceFileName).toFile());
        return RulesConfig.readFromLoadedYamlMap(node, sourceFileName);
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
        IssueTracker.clear();
        readYaml("[]", "test.yaml");

        assertTrue(IssueTracker.getWarnings().stream().anyMatch(w -> w.contains("must be a mapping")));
    }

    @Test
    void rejectsExclusionWithoutMove() throws IOException {
        IssueTracker.clear();
        String yaml = """
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                """;
        RulesConfig config = readYaml(yaml, "test.yaml");

        assertTrue(IssueTracker.getWarnings().stream()
                .anyMatch(w -> w.contains("missing required field \"move\"")));
        assertTrue(config.getMoveExclusionConfigs().isEmpty());
    }

    @Test
    void rejectsUnknownMoveOnEmptyCardPool() throws IOException {
        IssueTracker.clear();
        String yaml = """
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    move: Ember
                """;
        RulesConfig config = readYaml(yaml, "sources.yaml");

        Rules rules = new Rules();
        config.applyTo(rules, new CardGroup<>());

        assertTrue(IssueTracker.getWarnings().stream()
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

        IssueTracker.clear();
        RulesConfig config = RulesConfig.readFromLoadedYamlMap(
                YamlIO.load(defaultFile.toFile()), defaultFile.getFileName().toString());
        Rules rules = new Rules();
        config.applyTo(rules, new CardGroup<>());

        assertTrue(IssueTracker.getWarnings().stream().anyMatch(w -> !w.isBlank()));
    }

    @Test
    void removingExclusionRemovesDerivedAssignments() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "Ember"));
        cards.add(someMonster(37, CardId.MONSTER_146_2, "OtherMove"));

        IssueTracker.clear();
        RulesConfig config = readYaml("""
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    move: Ember
                """, "test.yaml");
        Rules rules = new Rules();
        config.applyTo(rules, cards);
        assertEquals(1, rules.getMoveAssignments().getAllAssignments().size());

        rules.removeMoveExclusion(rules.getMoveExclusions().getAllExclusions().get(0));
        assertTrue(rules.getMoveAssignments().getAllAssignments().isEmpty());
    }

    @Test
    void removingOneExclusionKeepsAssignmentsFromOtherExclusionsInSameFile() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "Ember"));
        cards.add(someMonster(37, CardId.MONSTER_146_2, "OtherMove"));

        IssueTracker.clear();
        RulesConfig config = readYaml("""
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    move: Ember
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    move: OtherMove
                """, "test.yaml");
        Rules rules = new Rules();
        config.applyTo(rules, cards);
        assertEquals(2, rules.getMoveAssignments().getAllAssignments().size());

        MoveExclusion emberExclusion = rules.getMoveExclusions().getAllExclusions().stream()
                .filter(exclusion -> "Ember".equals(exclusion.getMoveName()))
                .findFirst()
                .orElseThrow();
        rules.removeMoveExclusion(emberExclusion);

        assertEquals(1, rules.getMoveExclusions().getAllExclusions().size());
        assertEquals(1, rules.getMoveAssignments().getAllAssignments().size());
        assertEquals("OtherMove",
                rules.getMoveAssignments().getAllAssignments().get(0).getMove().name.toString());
    }

    @Test
    void togglingGenerateAssignmentsUpdatesDerivedAssignments() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "Ember"));
        cards.add(someMonster(37, CardId.MONSTER_146_2, "Ember"));

        IssueTracker.clear();
        RulesConfig config = readYaml("""
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    move: Ember
                """, "test.yaml");
        Rules rules = new Rules();
        config.applyTo(rules, cards);
        assertEquals(2, rules.getMoveAssignments().getAllAssignments().size());

        MoveExclusion runtime = rules.getMoveExclusions().getAllExclusions().get(0);
        MoveExclusion withoutAssignments = new MoveExclusion(runtime.getCardId(),
                runtime.getMoveName(), runtime.isRemoveFromPool(), false,
                runtime.getSourceFileName());
        rules.updateMoveExclusion(runtime, withoutAssignments, cards);
        assertTrue(rules.getMoveAssignments().getAllAssignments().isEmpty());

        rules.updateMoveExclusion(withoutAssignments, runtime, cards);
        assertEquals(2, rules.getMoveAssignments().getAllAssignments().size());
    }

    @Test
    void disablingGenerateAssignmentsRemovesDerivedAssignments() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "Ember"));

        IssueTracker.clear();
        RulesConfig config = readYaml("""
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    move: Ember
                """, "test.yaml");
        Rules rules = new Rules();
        config.applyTo(rules, cards);
        MoveExclusion runtime = rules.getMoveExclusions().getAllExclusions().get(0);
        MoveExclusion withoutAssignments = new MoveExclusion(runtime.getCardId(),
                runtime.getMoveName(), runtime.isRemoveFromPool(), false,
                runtime.getSourceFileName());

        rules.updateMoveExclusion(runtime, withoutAssignments, cards);

        assertTrue(rules.getMoveAssignments().getAllAssignments().isEmpty());
        assertTrue(rules.getMoveExclusions().getAllExclusions().get(0).isRemoveFromPool());
    }

    @Test
    void removingOneExclusionKeepsDerivedAssignmentsFromOtherExclusionsInSameFile() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "Ember"));
        cards.add(someMonster(37, CardId.MONSTER_146_2, "OtherMove"));

        IssueTracker.clear();
        RulesConfig config = readYaml("""
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    move: Ember
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    move: OtherMove
                """, "shared.yaml");
        Rules rules = new Rules();
        config.applyTo(rules, cards);
        assertEquals(2, rules.getMoveAssignments().getAllAssignments().size());

        MoveExclusion emberExclusion = rules.getMoveExclusions().getAllExclusions().stream()
                .filter(exclusion -> "Ember".equals(exclusion.getMoveName()))
                .findFirst()
                .orElseThrow();
        rules.removeMoveExclusion(emberExclusion);

        assertEquals(1, rules.getMoveExclusions().getAllExclusions().size());
        assertEquals(1, rules.getMoveAssignments().getAllAssignments().size());
        assertEquals("OtherMove",
                rules.getMoveAssignments().getAllAssignments().get(0).getMove().name.toString());
    }

    @Test
    void togglingGenerateAssignmentsOnOneExclusionKeepsOthersInSameFile() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "Ember"));
        cards.add(someMonster(37, CardId.MONSTER_146_2, "OtherMove"));

        IssueTracker.clear();
        RulesConfig config = readYaml("""
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    move: Ember
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    move: OtherMove
                """, "shared.yaml");
        Rules rules = new Rules();
        config.applyTo(rules, cards);

        MoveExclusion emberExclusion = rules.getMoveExclusions().getAllExclusions().stream()
                .filter(exclusion -> "Ember".equals(exclusion.getMoveName()))
                .findFirst()
                .orElseThrow();
        MoveExclusion emberWithoutAssignments = new MoveExclusion(emberExclusion.getCardId(),
                emberExclusion.getMoveName(), emberExclusion.isRemoveFromPool(), false,
                emberExclusion.getSourceFileName());
        rules.updateMoveExclusion(emberExclusion, emberWithoutAssignments, cards);

        assertEquals(2, rules.getMoveExclusions().getAllExclusions().size());
        assertEquals(1, rules.getMoveAssignments().getAllAssignments().size());
        assertEquals("OtherMove",
                rules.getMoveAssignments().getAllAssignments().get(0).getMove().name.toString());
    }

    @Test
    void rejectsCardWithoutNameAndLevel() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();

        for (String cardSpecifier : List.of("SomeMonster", "SomeMonster_1")) {
        IssueTracker.clear();
            RulesConfig config = readYaml("""
                    moveExclusions:
                      - remove_from_pool: true
                        exclude_from_randomization: true
                        card: %s
                        move: TestMove
                    """.formatted(cardSpecifier), "test.yaml");
            Rules rules = new Rules();
            config.applyTo(rules, cards);

            assertTrue(IssueTracker.getWarnings().stream()
                    .anyMatch(w -> w.contains("must use name and level")));
            assertTrue(rules.getMoveExclusions().getAllExclusions().isEmpty());
        }
    }

    @Test
    void resolvesCardByLevelSpecifier() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));
        cards.add(someMonster(37, CardId.MONSTER_146_2, "OtherMove"));

        IssueTracker.clear();
        String yaml = """
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    card: SomeMonster lvl35
                    move: TestMove
                """;
        RulesConfig config = readYaml(yaml, "test.yaml");
        Rules rules = new Rules();
        config.applyTo(rules, cards);

        assertEquals(1, rules.getMoveExclusions().getAllExclusions().size());
        assertEquals(CardId.MONSTER_146_1,
                rules.getMoveExclusions().getAllExclusions().get(0).getCardId());
        Map<String, Object> exclusionEntry = config.getMoveExclusionConfigs().get(0).convertToYamlMap();
        assertEquals("SomeMonster lvl35", exclusionEntry.get("card"));
        assertEquals("TestMove", exclusionEntry.get("move"));
    }

    @Test
    void convertToYamlMapRoundTripsRulesNode() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));

        IssueTracker.clear();
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
        RulesConfig config = readYaml(yaml, "rules.yaml");
        RulesConfig saved = RulesConfig.readFromLoadedYamlMap(config.convertToYamlMap(),
                "config.yaml");

        Rules rules = new Rules();
        saved.applyTo(rules, cards);

        assertEquals(1, rules.getMoveExclusions().getAllExclusions().size());
        assertEquals(1, rules.getMoveAssignments().getAllAssignments().size());
        assertEquals(0, rules.getMoveAssignments().getAllAssignments().get(0).getMoveSlot());
    }

    @Test
    void moveAssignmentSlotUsesOneBasedYamlAndZeroBasedStorage() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));
        cards.add(someMonster(37, CardId.MONSTER_146_2, "OtherMove"));

        IssueTracker.clear();
        RulesConfig slotOne = readYaml("""
                moveAssignments:
                  - to_card: SomeMonster lvl35
                    to_move_slot: 1
                    move: TestMove
                """, "slot_one.yaml");
        Rules rules = new Rules();
        slotOne.applyTo(rules, cards);

        assertEquals(0, rules.getMoveAssignments().getAllAssignments().get(0).getMoveSlot());
        assertEquals(1, slotOne.getMoveAssignmentConfigs().get(0).convertToYamlMap()
                .get("to_move_slot"));

        RulesConfig slotTwo = readYaml("""
                moveAssignments:
                  - to_card: SomeMonster lvl37
                    to_move_slot: 2
                    move: OtherMove
                """, "slot_two.yaml");
        slotTwo.applyTo(rules, cards);

        assertEquals(1, rules.getMoveAssignments().getAllAssignments().get(1).getMoveSlot());
        assertEquals(2, slotTwo.getMoveAssignmentConfigs().get(0).convertToYamlMap()
                .get("to_move_slot"));
    }

    @Test
    void rejectsZeroBasedMoveSlotInYaml() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));

        IssueTracker.clear();
        RulesConfig config = readYaml("""
                moveAssignments:
                  - to_card: SomeMonster lvl35
                    to_move_slot: 0
                    move: TestMove
                """, "bad_slot.yaml");
        Rules rules = new Rules();
        config.applyTo(rules, cards);

        assertTrue(IssueTracker.getWarnings().stream().anyMatch(w -> w.contains("out of range")));
        assertTrue(rules.getMoveAssignments().getAllAssignments().isEmpty());
    }

    @Test
    void convertToYamlMapIncludesEmptyAssignments() throws IOException {
        IssueTracker.clear();
        RulesConfig config = readYaml("""
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    move: TestMove
                """, "rules.yaml");

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

        IssueTracker.clear();
        RulesConfig config = readYaml("""
                moveAssignments:
                  - to_card: SomeMonster lvl35
                    to_move_slot: 1
                    move: TestMove
                  - to_card: SomeMonster lvl35
                    to_move_slot: 1
                    move: OtherMove
                """, "conflict.yaml");
        Rules rules = new Rules();
        config.applyTo(rules, cards);

        assertEquals(1, rules.getMoveAssignments().getAllAssignments().size());
        assertTrue(IssueTracker.getWarnings().stream()
                .anyMatch(w -> w.contains("Conflicting assignment")));
    }

    @Test
    void applyToReplacesExistingRulesWhenCalledOnClearedRules() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));

        IssueTracker.clear();
        Rules rules = new Rules();
        rules.getMoveExclusions().addMoveExclusion(CardId.NO_CARD, "OldMove", true, true,
                "old.yaml", cards, rules.getMoveAssignments());
        rules.getMoveAssignments().addMoveAssignment(cards.withId(CardId.MONSTER_146_1), 0,
                cards.withId(CardId.MONSTER_146_1).getMoveWithName("TestMove"), "old.yaml");

        rules.replaceFrom(RulesConfig.empty(), cards);

        assertTrue(rules.getMoveExclusions().getAllExclusions().isEmpty());
        assertTrue(rules.getMoveAssignments().getAllAssignments().isEmpty());
    }

    @Test
    void saveToFileRoundTripsConfig() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));

        IssueTracker.clear();
        RulesConfig config = readYaml("""
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    card: SomeMonster lvl35
                    move: TestMove
                moveAssignments: []
                """, "rules.yaml");

        Path output = tempDir.resolve("saved_rules.yaml");
        YamlIO.save(output.toFile(), config.convertToYamlMap());

        RulesConfig reloaded = RulesConfig.readFromLoadedYamlMap(
                YamlIO.load(output.toFile()), output.getFileName().toString());
        Rules rules = new Rules();
        reloaded.applyTo(rules, cards);

        assertEquals(1, rules.getMoveExclusions().getAllExclusions().size());
    }
}
