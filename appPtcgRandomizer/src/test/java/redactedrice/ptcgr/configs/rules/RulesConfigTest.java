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

import javax.swing.JPanel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import redactedrice.ptcgr.configs.Config;
import redactedrice.ptcgr.configs.ParserHelpers;
import redactedrice.ptcgr.configs.YamlIO;
import redactedrice.ptcgr.constants.romenums.CardId;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.data.Move;
import redactedrice.ptcgr.resources.PtcgBundledResources;
import redactedrice.ptcgr.rules.MoveAssignment;
import redactedrice.ptcgr.rules.MoveAssignments;
import redactedrice.ptcgr.rules.MoveExclusion;
import redactedrice.ptcgr.rules.MoveExclusions;
import redactedrice.ptcgr.rules.Rules;
import redactedrice.ptcgr.randomizer.RandomizerCore;
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
    void loadsPendingMoveAssignmentsOnRandomizerCoreRules() {
        RandomizerCore core = new RandomizerCore(new JPanel());
        core.getRules().clear();
        RulesConfig config = new RulesConfig("pending.yaml", List.of(),
                List.of(new MoveAssignmentConfig("SomeMonster lvl35", "1", "TestMove", "")));
        config.applyTo(core.getRules(), null);

        assertEquals(1, core.getRules().getMoveAssignments().getAllAssignments().size());
        assertTrue(core.getRules().getMoveAssignments().getAllAssignments().get(0).isPending());
    }

    @Test
    void loadsPendingMoveAssignmentsFromConstructedConfig() {
        RulesConfig config = new RulesConfig("pending.yaml", List.of(),
                List.of(new MoveAssignmentConfig("SomeMonster lvl35", "1", "TestMove", "")));
        Rules rules = new Rules();
        config.applyTo(rules, null);

        assertEquals(1, rules.getMoveAssignments().getAllAssignments().size());
        assertTrue(rules.getMoveAssignments().getAllAssignments().get(0).isPending());
    }

    @Test
    void loadsPendingMoveAssignmentsBeforeRomIsOpen() throws IOException {
        IssueTracker.clear();
        RulesConfig config = readYaml("""
                moveAssignments:
                  - to_card: SomeMonster lvl35
                    to_move_slot: 1
                    move: TestMove
                """, "test.yaml");
        Rules rules = new Rules();
        config.applyTo(rules, null);

        assertTrue(!IssueTracker.hasWarnings());
        assertEquals(1, rules.getMoveAssignments().getAllAssignments().size());
        assertTrue(rules.getMoveAssignments().getAllAssignments().get(0).isPending());

        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));
        rules.syncWithCards(cards);

        assertTrue(!IssueTracker.hasWarnings());
        assertEquals(1, rules.getMoveAssignments().getAllAssignments().size());
        assertTrue(!rules.getMoveAssignments().getAllAssignments().get(0).isPending());
    }

    @Test
    void pendingMoveAssignmentWarnsAndDropsOnFailedRomResolve() throws IOException {
        IssueTracker.clear();
        RulesConfig config = readYaml("""
                moveAssignments:
                  - to_card: MissingMonster lvl99
                    to_move_slot: 1
                    move: TestMove
                """, "test.yaml");
        Rules rules = new Rules();
        config.applyTo(rules, null);
        assertEquals(1, rules.getMoveAssignments().getAllAssignments().size());

        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));
        IssueTracker.clear();
        rules.syncWithCards(cards);

        assertTrue(rules.getMoveAssignments().getAllAssignments().isEmpty());
        assertTrue(IssueTracker.getWarnings().stream()
                .anyMatch(w -> w.contains("failed to resolve card")));
    }

    @Test
    void pendingCardScopedExclusionDoesNotMatchAsGlobal() throws IOException {
        IssueTracker.clear();
        RulesConfig config = readYaml("""
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    card: SomeMonster lvl35
                    move: TestMove
                """, "pending.yaml");
        Rules rules = new Rules();
        config.applyTo(rules, null);

        MoveExclusion pending = rules.getMoveExclusions().getAllExclusions().get(0);
        assertTrue(pending.isPending());

        MonsterCard otherCard = someMonster(10, CardId.MONSTER_146_2, "TestMove");
        assertFalse(rules.getMoveExclusions().isMoveRemovedFromPool(otherCard.id,
                otherCard.getMove(0)));
        assertFalse(rules.getMoveExclusions().isMoveExcludedFromRandomization(otherCard.id,
                otherCard.getMove(0)));
    }

    @Test
    void pendingCardScopedExclusionDoesNotBlockLaterGlobalMatchInSameBucket() throws IOException {
        IssueTracker.clear();
        Rules rules = new Rules();
        // pending card scoped entry shares the move-name bucket with a later global one
        rules.addMoveExclusion(new MoveExclusion(CardId.NO_CARD, "TestMove", true, true,
                "pending.yaml", "SomeMonster lvl35"), null);
        rules.addMoveExclusion(new MoveExclusion(CardId.NO_CARD, "TestMove", true, true,
                "global.yaml"), null);

        MonsterCard card = someMonster(10, CardId.MONSTER_146_2, "TestMove");
        assertTrue(rules.getMoveExclusions().isMoveRemovedFromPool(card.id, card.getMove(0)));
        assertTrue(rules.getMoveExclusions().isMoveExcludedFromRandomization(card.id,
                card.getMove(0)));
    }

    @Test
    void loadsUnsupportedMovesResource() throws IOException {
        Path defaultFile = tempDir.resolve(PtcgBundledResources.UNSUPPORTED_MOVES_FILE_NAME);
        try (InputStream in =
                getClass().getResourceAsStream(PtcgBundledResources.UNSUPPORTED_MOVES_CLASSPATH)) {
            Files.copy(in, defaultFile);
        }

        IssueTracker.clear();
        Config loaded = Config.readFromLoadedYamlMap(YamlIO.load(defaultFile.toFile()),
                defaultFile.getFileName().toString());
        Rules rules = new Rules();
        loaded.getRulesConfig().applyTo(rules, null);

        assertTrue(loaded.hasRules());
        assertEquals(26, rules.getMoveExclusions().getAllExclusions().size());
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
    void conflictingAssignmentWarningUsesCardNameWithLevel() {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));
        MonsterCard card = cards.withId(CardId.MONSTER_146_1);
        Move otherMove = card.getMove(1);
        otherMove.name.setText("OtherMove");
        card.setMoves(List.of(card.getMove(0), otherMove));

        IssueTracker.clear();
        MoveAssignments assignments = new MoveAssignments();
        assignments.addMoveAssignment(card, 0, card.getMoveWithName("TestMove"), "user added");
        assignments.add(new MoveAssignment(card.id, 0, otherMove, "user added"), cards);

        assertEquals(1, assignments.getAllAssignments().size());
        assertTrue(IssueTracker.getWarnings().stream()
                .anyMatch(w -> w.contains("SomeMonster lvl35")));
    }

    @Test
    void tryAddSilentlyRejectsEquivalentExclusionFromDifferentSource() {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));

        IssueTracker.clear();
        MoveExclusions exclusions = new MoveExclusions();
        MoveAssignments assignments = new MoveAssignments();
        exclusions.addMoveExclusion(CardId.NO_CARD, "TestMove", true, true, "bundled.yaml", cards,
                assignments);
        assertEquals(1, exclusions.getAllExclusions().size());

        MoveExclusion userAdded = new MoveExclusion(CardId.NO_CARD, "TestMove", true, true,
                "user added");
        assertFalse(exclusions.tryAdd(userAdded, cards, assignments));
        assertEquals(1, exclusions.getAllExclusions().size());
        assertTrue(IssueTracker.getWarnings().isEmpty());
    }

    @Test
    void applyToIgnoresEquivalentExclusionFromDifferentSourceWithoutWarning() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "Ember"));

        IssueTracker.clear();
        Rules rules = new Rules();
        rules.getMoveExclusions().addMoveExclusion(CardId.NO_CARD, "Ember", true, true,
                "unsupported_moves.yaml", cards, rules.getMoveAssignments());

        RulesConfig config = readYaml("""
                moveExclusions:
                  - remove_from_pool: true
                    exclude_from_randomization: true
                    move: Ember
                """, "user_config.yaml");
        config.applyTo(rules, cards);

        assertEquals(1, rules.getMoveExclusions().getAllExclusions().size());
        assertTrue(IssueTracker.getWarnings().stream()
                .noneMatch(w -> w.contains("Duplicate exclusion")));
    }

    @Test
    void tryAddRejectsDuplicateExclusionWithWarning() {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));

        IssueTracker.clear();
        MoveExclusions exclusions = new MoveExclusions();
        MoveAssignments assignments = new MoveAssignments();
        MoveExclusion exclusion = new MoveExclusion(CardId.NO_CARD, "TestMove", true, true,
                "user added");
        assertTrue(exclusions.tryAdd(exclusion, cards, assignments));
        assertFalse(exclusions.tryAdd(exclusion, cards, assignments));

        assertEquals(1, exclusions.getAllExclusions().size());
        assertTrue(IssueTracker.getWarnings().stream().anyMatch(w -> w.contains("Duplicate exclusion")));
    }

    @Test
    void duplicateAssignmentSilentlyRejectsAcrossSources() {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));
        MonsterCard card = cards.withId(CardId.MONSTER_146_1);

        IssueTracker.clear();
        MoveAssignments assignments = new MoveAssignments();
        Move move = card.getMoveWithName("TestMove");
        assertTrue(assignments.add(new MoveAssignment(card.id, 0, move, "bundled.yaml"), cards));
        assertFalse(assignments.add(new MoveAssignment(card.id, 0, move, "user added"), cards));

        assertEquals(1, assignments.getAllAssignments().size());
        assertTrue(IssueTracker.getWarnings().isEmpty());
    }

    @Test
    void duplicateAssignmentWarnsAndRejects() {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));
        MonsterCard card = cards.withId(CardId.MONSTER_146_1);

        IssueTracker.clear();
        MoveAssignments assignments = new MoveAssignments();
        Move move = card.getMoveWithName("TestMove");
        assertTrue(assignments.add(new MoveAssignment(card.id, 0, move, "user added"), cards));
        assertFalse(assignments.add(new MoveAssignment(card.id, 0, move, "user added"), cards));

        assertEquals(1, assignments.getAllAssignments().size());
        assertTrue(IssueTracker.getWarnings().stream().anyMatch(w -> w.contains("Duplicate assignment")));
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

    @Test
    void fromRulesWithNullCardsExportsAssignmentsWithoutThrowing() throws IOException {
        CardGroup<MonsterCard> cards = new CardGroup<>();
        cards.add(someMonster(35, CardId.MONSTER_146_1, "TestMove"));

        IssueTracker.clear();
        RulesConfig loaded = readYaml("""
                moveAssignments:
                  - to_card: SomeMonster lvl35
                    to_move_slot: 1
                    move: TestMove
                """, "test.yaml");
        Rules rules = new Rules();
        loaded.applyTo(rules, cards);

        RulesConfig exportConfig = RulesConfig.fromRules(rules, null);

        assertEquals(1, exportConfig.getMoveAssignmentConfigs().size());
        assertEquals("TestMove", exportConfig.getMoveAssignmentConfigs().get(0).getMove());
        assertEquals(CardId.MONSTER_146_1.toString(),
                exportConfig.getMoveAssignmentConfigs().get(0).getToCard());
    }
}
