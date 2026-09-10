package redactedrice.ptcgr.randomizer.scripttests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import redactedrice.ptcgr.constants.romenums.CardAiFlags;
import redactedrice.ptcgr.constants.romenums.CardId;
import redactedrice.ptcgr.constants.romenums.CardType;
import redactedrice.ptcgr.constants.romenums.EvolutionStage;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.rom.Cards;
import redactedrice.ptcgr.rules.Rules;
import redactedrice.ptcgr.randomizer.RandomizerCore;
import redactedrice.randomizer.context.JavaContext;
import redactedrice.randomizer.scripttests.ScriptTestCase;
import redactedrice.randomizer.scripttests.ScriptTestFields;
import redactedrice.randomizer.scripttests.ScriptTestFixtures;
import redactedrice.randomizer.scripttests.ScriptTestValues;

// Builds in-memory PTCG cards from the case table and checks expect by card id.
// Every card spec and expect row needs id. name is only for game fields like prevEvoName.
final class PtcgScriptTestFixtures implements ScriptTestFixtures {
    private static final List<CardId> MONSTER_IDS = monsterIds();
    private static final List<FlagSetField<?>> FLAG_SET_FIELDS = List.of(
            flagSet("aiFlags", card -> card.aiFlags, CardAiFlags.class));

    @Override
    public void populateContext(JavaContext context, ScriptTestCase testCase) {
        Map<String, Object> data = testCase.data();
        List<Map<String, Object>> shared = ScriptTestValues.optionalTables(data, "cards");
        List<Map<String, Object>> originalSpecs = ScriptTestValues.optionalTables(data, "original");
        List<Map<String, Object>> modifiedSpecs = ScriptTestValues.optionalTables(data, "modified");

        if (originalSpecs == null) {
            originalSpecs = shared;
        }
        if (modifiedSpecs == null) {
            modifiedSpecs = shared != null ? shared : originalSpecs;
        }
        if (originalSpecs == null || modifiedSpecs == null) {
            throw new IllegalArgumentException(
                    "Case needs a cards list, or original (and optionally modified)");
        }

        Rules rules = new Rules();
        RandomizerCore.bindRandomizeContext(context, buildData(context, originalSpecs, rules),
                buildData(context, modifiedSpecs, rules), rules);
    }

    @Override
    public void assertExpect(ScriptTestCase testCase, JavaContext context) {
        String label = testCase.displayName();
        List<Map<String, Object>> expect =
                ScriptTestValues.listOfMaps(testCase.data().get("expect"), "expect");
        Cards modified = (Cards) context.get("modified");
        List<MonsterCard> cards = modified.getRandomizableMonsterCards();
        List<String> mismatches = new ArrayList<>();
        Set<MonsterCard> claimed = Collections.newSetFromMap(new IdentityHashMap<>());

        for (Map<String, Object> expected : expect) {
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

        ScriptTestFields.failIfMismatches(label, mismatches);
    }

    private static Cards buildData(JavaContext context, List<Map<String, Object>> cardSpecs,
            Rules rules) {
        if (cardSpecs.size() > MONSTER_IDS.size()) {
            throw new IllegalArgumentException(
                    "Too many cards in case. Have " + MONSTER_IDS.size() + " monster ids");
        }

        Cards cards = new Cards();
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
        return FLAG_SET_FIELDS.stream().map(FlagSetField::name).toList();
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

    private record FlagSetField<E extends Enum<E>>(String name, Function<MonsterCard, Set<E>> accessor,
            Class<E> enumClass) {

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
        try {
            CardId id = CardId.valueOf(idText);
            if (!id.name().startsWith("MONSTER_")) {
                throw new IllegalArgumentException("Card id must be a monster id: " + idText);
            }
            return id;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown card id '" + idText + "'", e);
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

    private static List<CardId> monsterIds() {
        List<CardId> ids = new ArrayList<>();
        for (CardId id : CardId.values()) {
            if (id.name().startsWith("MONSTER_")) {
                ids.add(id);
            }
        }
        return List.copyOf(ids);
    }
}
