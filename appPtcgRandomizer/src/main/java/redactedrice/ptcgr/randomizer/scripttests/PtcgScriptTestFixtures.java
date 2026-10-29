package redactedrice.ptcgr.randomizer.scripttests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import redactedrice.ptcgr.constants.romenums.CardAiFlags;
import redactedrice.ptcgr.constants.romenums.WeaknessResistanceFlags;
import redactedrice.ptcgr.constants.romenums.CardId;
import redactedrice.ptcgr.constants.romenums.CardType;
import redactedrice.ptcgr.constants.romenums.DeckId;
import redactedrice.ptcgr.constants.romenums.EvolutionStage;
import redactedrice.ptcgr.data.Deck;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.rom.Cards;
import redactedrice.ptcgr.rom.Decks;
import redactedrice.ptcgr.rules.Rules;
import redactedrice.ptcgr.randomizer.RandomizationData;
import redactedrice.ptcgr.randomizer.RandomizerCore;
import redactedrice.randomizer.context.JavaContext;
import redactedrice.randomizer.scripttests.ScriptTestCase;
import redactedrice.randomizer.scripttests.ScriptTestFields;
import redactedrice.randomizer.scripttests.ScriptTestFixtures;
import redactedrice.randomizer.scripttests.ScriptTestValues;

// Builds in-memory PTCG cards/decks from the case table and checks expectCards/expectDecks by id.
// Card fixture: originalCards + modifiedCards, or cards for both sides when they match.
// Deck fixture: originalDecks + modifiedDecks, or decks for both sides when they match.
// Every card spec and expect row needs id. name is only for game fields like prevEvoName.
// Deck rows use id (DeckId name) and a cards map of CardId name to count.
public final class PtcgScriptTestFixtures implements ScriptTestFixtures {
    private static final Set<String> ALLOWED_CASE_KEYS = Set.of(
            "name",
            "module",
            "args",
            "seed",
            "cards",
            "originalCards",
            "modifiedCards",
            "decks",
            "originalDecks",
            "modifiedDecks",
            "expectCards",
            "expectDecks");

    private static final List<CardId> MONSTER_IDS = monsterIds();
    private static final List<DeckId> DECK_IDS = deckIds();
    private static final List<FlagSetField<?>> FLAG_SET_FIELDS = List.of(
            flagSet("aiFlags", card -> card.aiFlags, CardAiFlags.class),
            flagSet("weakness", card -> card.weakness, WeaknessResistanceFlags.class),
            flagSet("resistance", card -> card.resistance, WeaknessResistanceFlags.class));

    @Override
    public void populateContext(JavaContext context, ScriptTestCase testCase) {
        Map<String, Object> data = testCase.data();
        rejectUnknownCaseKeys(data);

        List<Map<String, Object>> sharedCards = ScriptTestValues.optionalTables(data, "cards");
        List<Map<String, Object>> originalCards = ScriptTestValues.optionalTables(data, "originalCards");
        List<Map<String, Object>> modifiedCards = ScriptTestValues.optionalTables(data, "modifiedCards");
        if (sharedCards != null) {
            originalCards = sharedCards;
            modifiedCards = sharedCards;
        }

        List<Map<String, Object>> sharedDecks = ScriptTestValues.optionalTables(data, "decks");
        List<Map<String, Object>> originalDecks = ScriptTestValues.optionalTables(data, "originalDecks");
        List<Map<String, Object>> modifiedDecks = ScriptTestValues.optionalTables(data, "modifiedDecks");
        if (sharedDecks != null) {
            originalDecks = sharedDecks;
            modifiedDecks = sharedDecks;
        }

        Rules rules = new Rules();
        RandomizationData original = new RandomizationData(
                buildCards(context, originalCards, rules), buildDecks(originalDecks));
        RandomizationData modified = new RandomizationData(
                buildCards(context, modifiedCards, rules), buildDecks(modifiedDecks));
        RandomizerCore.bindRandomizeContext(context, original, modified, rules);
    }

    private static void rejectUnknownCaseKeys(Map<String, Object> data) {
        for (String key : data.keySet()) {
            if (!ALLOWED_CASE_KEYS.contains(key)) {
                throw new IllegalArgumentException("Unknown case field '" + key + "'");
            }
        }
    }

    @Override
    public void assertExpect(ScriptTestCase testCase, JavaContext context) {
        String label = testCase.displayName();
        Map<String, Object> data = testCase.data();
        List<Map<String, Object>> expectCards = ScriptTestValues.optionalListOfMaps(data.get("expectCards"),
                "expectCards");
        List<Map<String, Object>> expectDecks = ScriptTestValues.optionalListOfMaps(data.get("expectDecks"),
                "expectDecks");
        if (expectCards.isEmpty() && expectDecks.isEmpty()) {
            throw new IllegalArgumentException("Case needs expectCards and/or expectDecks");
        }

        RandomizationData modified = (RandomizationData) context.get("modified");
        List<MonsterCard> cards = modified.cards.getRandomizableMonsterCards();
        List<String> mismatches = new ArrayList<>();
        Set<MonsterCard> claimed = Collections.newSetFromMap(new IdentityHashMap<>());

        for (Map<String, Object> expected : expectCards) {
            String cardLabel = cardLabel(expected);
            MonsterCard card = findCard(cards, expected, claimed);
            if (card == null) {
                mismatches.add("expected " + cardLabel + " but it was not in the deck");
                continue;
            }
            claimed.add(card);
            collectFlagMismatches(card, expected, mismatches, cardLabel);
            ScriptTestFields.collectMismatches(context, card, expected, mismatches, cardLabel,
                    idAndFlagFields());
        }

        for (Map<String, Object> expected : expectDecks) {
            collectDeckExpectMismatches(modified.decks, expected, mismatches);
        }

        ScriptTestFields.failIfMismatches(label, mismatches);
    }

    private static Decks buildDecks(List<Map<String, Object>> deckSpecs) {
        Decks decks = new Decks();
        if (deckSpecs == null) {
            return decks;
        }

        if (deckSpecs.size() > DECK_IDS.size()) {
            throw new IllegalArgumentException(
                    "Too many decks in case. Have " + DECK_IDS.size() + " deck ids");
        }

        Set<DeckId> usedIds = new HashSet<>();
        for (Map<String, Object> spec : deckSpecs) {
            DeckId deckId = requireDeckId(spec, usedIds);
            usedIds.add(deckId);
            decks.put(buildDeck(spec));
        }

        return decks;
    }

    private static Deck buildDeck(Map<String, Object> spec) {
        Deck deck = new Deck(requireDeckId(spec, new HashSet<>()));
        Map<String, Object> cardCounts = ScriptTestValues.optionalMap(spec.get("cards"));
        for (Map.Entry<String, Object> entry : cardCounts.entrySet()) {
            CardId cardId = parseAnyCardId(entry.getKey());
            if (!(entry.getValue() instanceof Number countNumber)) {
                throw new IllegalArgumentException(
                        "Deck card count for " + entry.getKey() + " must be a number");
            }
            deck.setCountOfCard(cardId, countNumber.intValue());
        }
        return deck;
    }

    private static Cards buildCards(JavaContext context, List<Map<String, Object>> cardSpecs,
            Rules rules) {
        Cards cards = new Cards();
        if (cardSpecs == null) {
            return cards;
        }

        if (cardSpecs.size() > MONSTER_IDS.size()) {
            throw new IllegalArgumentException(
                    "Too many cards in case. Have " + MONSTER_IDS.size() + " monster ids");
        }

        cards.bindRules(rules);
        Set<CardId> usedIds = new HashSet<>();
        for (Map<String, Object> spec : cardSpecs) {
            CardId id = requireCardId(spec, usedIds);
            usedIds.add(id);
            cards.cards().add(buildCard(context, spec, id));
        }

        return cards;
    }

    private static MonsterCard buildCard(JavaContext context, Map<String, Object> spec, CardId id) {
        MonsterCard card = new MonsterCard();
        card.id = id;
        card.type = CardType.MONSTER_COLORLESS;
        card.stage = EvolutionStage.BASIC;

        applyFlagFields(card, spec);
        ScriptTestFields.apply(context, card, spec, idAndFlagFields());
        if (card.name.toString().isBlank()) {
            card.name.setText(id.name());
        }
        return card;
    }

    private static List<String> flagFieldNames() {
        return FLAG_SET_FIELDS.stream().map(field -> field.name).toList();
    }

    private static List<String> idAndFlagFields() {
        List<String> fields = new ArrayList<>(FLAG_SET_FIELDS.size() + 1);
        fields.add("id");
        fields.addAll(flagFieldNames());
        return fields;
    }

    private static void applyFlagFields(MonsterCard card, Map<String, Object> spec) {
        for (FlagSetField<?> field : FLAG_SET_FIELDS) {
            field.apply(card, spec);
        }
    }

    private static void collectFlagMismatches(MonsterCard card, Map<String, Object> expected,
            List<String> mismatches, String cardLabel) {
        for (FlagSetField<?> field : FLAG_SET_FIELDS) {
            field.collectMismatch(card, expected, mismatches, cardLabel);
        }
    }

    private static void collectDeckExpectMismatches(Decks decks, Map<String, Object> expected,
            List<String> mismatches) {
        String deckLabel = deckLabel(expected);
        DeckId deckId = parseDeckId(ScriptTestValues.requiredString(expected, "id"));
        Deck deck = decks.getDeck(deckId);
        if (deck == null) {
            mismatches.add("expected " + deckLabel + " but it was not loaded");
            return;
        }
        Map<String, Object> wantedCounts = ScriptTestValues.optionalMap(expected.get("cards"));
        for (Map.Entry<String, Object> entry : wantedCounts.entrySet()) {
            CardId cardId = parseAnyCardId(entry.getKey());
            if (!(entry.getValue() instanceof Number countNumber)) {
                mismatches.add(deckLabel + " " + entry.getKey() + " expected a number");
                continue;
            }
            int wanted = countNumber.intValue();
            int actual = deck.getCountOfCard(cardId);
            if (actual != wanted) {
                mismatches.add(deckLabel + " " + entry.getKey() + " expected " + wanted
                        + " but was " + actual);
            }
        }
    }

    private static <E extends Enum<E>> FlagSetField<E> flagSet(String name,
            Function<MonsterCard, Set<E>> accessor, Class<E> enumClass) {
        return new FlagSetField<>(name, accessor, enumClass);
    }

    // Lua {} for flag sets often becomes an empty Map in Java, not a List.
    private static List<?> flagNamesFromSpecValue(Object value) {
        if (value instanceof List<?> list) {
            return list;
        }
        if (value instanceof Map<?, ?> map && map.isEmpty()) {
            return List.of();
        }
        return null;
    }

    private static final class FlagSetField<E extends Enum<E>> {
        private final String name;
        private final Function<MonsterCard, Set<E>> accessor;
        private final Class<E> enumClass;

        FlagSetField(String name, Function<MonsterCard, Set<E>> accessor, Class<E> enumClass) {
            this.name = name;
            this.accessor = accessor;
            this.enumClass = enumClass;
        }

        void apply(MonsterCard card, Map<String, Object> spec) {
            Object value = spec.get(name);
            if (value == null) {
                return;
            }
            List<?> flagNames = flagNamesFromSpecValue(value);
            if (flagNames == null) {
                throw new IllegalArgumentException(
                        "Expected list for " + name + " but got " + value.getClass().getSimpleName());
            }
            Set<E> flags = accessor.apply(card);
            flags.clear();
            for (Object item : flagNames) {
                flags.add(Enum.valueOf(enumClass, String.valueOf(item)));
            }
        }

        void collectMismatch(MonsterCard card, Map<String, Object> expected, List<String> mismatches,
                String cardLabel) {
            Object wantedFlags = expected.get(name);
            if (wantedFlags == null) {
                return;
            }
            List<?> flagNames = flagNamesFromSpecValue(wantedFlags);
            if (flagNames == null) {
                mismatches.add(cardLabel + " " + name + " expected a flag list");
                return;
            }
            Set<E> wanted = new HashSet<>();
            for (Object item : flagNames) {
                wanted.add(Enum.valueOf(enumClass, String.valueOf(item)));
            }
            Set<E> actual = accessor.apply(card);
            if (!actual.equals(wanted)) {
                mismatches.add(cardLabel + " " + name + " expected " + wanted + " but was " + actual);
            }
        }
    }

    private static CardId requireCardId(Map<String, Object> spec, Set<CardId> usedIds) {
        String idText = ScriptTestValues.requiredString(spec, "id");
        CardId id = parseCardId(idText);
        if (usedIds.contains(id)) {
            throw new IllegalArgumentException("Duplicate card id in case: " + idText);
        }
        return id;
    }

    private static CardId parseCardId(String idText) {
        CardId id = parseAnyCardId(idText);
        if (!id.name().startsWith("MONSTER_")) {
            throw new IllegalArgumentException("Card id must be a monster id: " + idText);
        }
        return id;
    }

    private static CardId parseAnyCardId(String idText) {
        try {
            return CardId.valueOf(idText);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown card id '" + idText + "'", e);
        }
    }

    private static DeckId requireDeckId(Map<String, Object> spec, Set<DeckId> usedIds) {
        String idText = ScriptTestValues.requiredString(spec, "id");
        DeckId deckId = parseDeckId(idText);
        if (usedIds.contains(deckId)) {
            throw new IllegalArgumentException("Duplicate deck id in case: " + deckId.name());
        }
        return deckId;
    }

    private static DeckId parseDeckId(String idText) {
        try {
            return DeckId.valueOf(idText);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown deck id '" + idText + "'", e);
        }
    }

    private static MonsterCard findCard(List<MonsterCard> cards, Map<String, Object> expected,
            Set<MonsterCard> claimed) {
        CardId id = parseCardId(ScriptTestValues.requiredString(expected, "id"));
        for (MonsterCard card : cards) {
            if (!claimed.contains(card) && card.id == id) {
                return card;
            }
        }
        return null;
    }

    private static String cardLabel(Map<String, Object> expected) {
        return "card " + ScriptTestValues.requiredString(expected, "id");
    }

    private static String deckLabel(Map<String, Object> expected) {
        return "deck " + ScriptTestValues.requiredString(expected, "id");
    }

    private static List<CardId> monsterIds() {
        List<CardId> ids = new ArrayList<>();
        for (CardId id : CardId.values()) {
            if (id.name().startsWith("MONSTER_")) {
                ids.add(id);
            }
        }
        return List.copyOf(ids);
    }

    private static List<DeckId> deckIds() {
        return List.copyOf(Arrays.asList(DeckId.values()));
    }
}
