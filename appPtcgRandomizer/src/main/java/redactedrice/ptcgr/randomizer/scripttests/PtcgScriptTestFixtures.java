package redactedrice.ptcgr.randomizer.scripttests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import redactedrice.ptcgr.constants.romenums.CardId;
import redactedrice.ptcgr.constants.romenums.CardType;
import redactedrice.ptcgr.constants.romenums.EvolutionStage;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.rom.Cards;
import redactedrice.ptcgr.rom.RandomizationData;
import redactedrice.ptcgr.rules.Rules;
import redactedrice.randomizer.context.JavaContext;
import redactedrice.randomizer.scripttests.ScriptTestCase;
import redactedrice.randomizer.scripttests.ScriptTestFixtures;
import redactedrice.randomizer.scripttests.ScriptTestValues;

// Builds in-memory PTCG cards from the case table and checks expect by card name.
// cards fills both decks. original and modified override a side when present.
final class PtcgScriptTestFixtures implements ScriptTestFixtures {
    private static final List<CardId> MONSTER_IDS = monsterIds();

    @Override
    public void populateContext(JavaContext context, ScriptTestCase testCase) {
        Map<String, Object> data = testCase.data();
        List<Map<String, Object>> shared = optionalCards(data, "cards");
        List<Map<String, Object>> originalSpecs = optionalCards(data, "original");
        List<Map<String, Object>> modifiedSpecs = optionalCards(data, "modified");

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
        context.register("original", buildData(originalSpecs, rules));
        context.register("modified", buildData(modifiedSpecs, rules));
        context.register("rules", rules);
    }

    @Override
    public void assertExpect(ScriptTestCase testCase, JavaContext context) {
        String label = testCase.displayName();
        List<Map<String, Object>> expect =
                ScriptTestValues.listOfMaps(testCase.data().get("expect"), "expect");
        RandomizationData modified = (RandomizationData) context.get("modified");
        List<MonsterCard> cards = modified.getRandomizableMonsterCards();
        List<String> mismatches = new ArrayList<>();

        for (Map<String, Object> expected : expect) {
            String name = ScriptTestValues.requiredString(expected, "name");
            MonsterCard card = findCard(cards, name);
            if (card == null) {
                mismatches.add("expected card '" + name + "' but it was not in the deck");
                continue;
            }
            if (expected.containsKey("numMoves")) {
                int wanted = ScriptTestValues.toInt(expected.get("numMoves"), 0);
                if (card.getNumMoves() != wanted) {
                    mismatches.add("card '" + name + "' numMoves expected " + wanted + " but was "
                            + card.getNumMoves());
                }
            }
            if (expected.containsKey("hp")) {
                int wanted = ScriptTestValues.toInt(expected.get("hp"), 0);
                if (card.getHp() != wanted) {
                    mismatches.add(
                            "card '" + name + "' hp expected " + wanted + " but was " + card.getHp());
                }
            }
        }

        if (!mismatches.isEmpty()) {
            throw new IllegalStateException(label + " " + String.join(". ", mismatches));
        }
    }

    private static List<Map<String, Object>> optionalCards(Map<String, Object> data, String field) {
        Object value = data.get(field);
        if (value == null) {
            return null;
        }
        return ScriptTestValues.listOfMaps(value, field);
    }

    private static RandomizationData buildData(List<Map<String, Object>> cardSpecs, Rules rules) {
        if (cardSpecs.size() > MONSTER_IDS.size()) {
            throw new IllegalArgumentException(
                    "Too many cards in case. Have " + MONSTER_IDS.size() + " monster ids");
        }

        Cards cards = new Cards();
        for (int i = 0; i < cardSpecs.size(); i++) {
            cards.cards().add(buildCard(cardSpecs.get(i), MONSTER_IDS.get(i)));
        }

        RandomizationData data = new RandomizationData();
        data.allCards = cards;
        data.bindRules(rules);
        return data;
    }

    private static MonsterCard buildCard(Map<String, Object> spec, CardId id) {
        MonsterCard card = new MonsterCard();
        card.id = id;
        card.type = CardType.MONSTER_COLORLESS;
        card.stage = EvolutionStage.BASIC;
        card.name.setText(ScriptTestValues.requiredString(spec, "name"));

        if (spec.containsKey("stage")) {
            card.stage = EvolutionStage.valueOf(ScriptTestValues.requiredString(spec, "stage"));
        }

        if (spec.containsKey("hp")) {
            card.setHp(ScriptTestValues.toInt(spec.get("hp"), 0));
        }
        if (spec.containsKey("numMoves")) {
            card.setNumMoves(ScriptTestValues.toInt(spec.get("numMoves"), 0));
        }
        return card;
    }

    private static MonsterCard findCard(List<MonsterCard> cards, String name) {
        for (MonsterCard card : cards) {
            if (name.equals(card.name.toString())) {
                return card;
            }
        }
        return null;
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
