package redactedrice.ptcgr.rom;


import java.util.ArrayList;
import java.util.List;

import redactedrice.ptcgr.data.Card;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.rompacker.Blocks;

public class RandomizationData {
    // Just a holder for the data. Keep public just for convenience
    public Cards allCards;
    public Texts idsToText;
    public Blocks blocks;

    public RandomizationData() {
        this.blocks = new Blocks();
    }

    // Lists will be autowrapped into lua tables
    public List<MonsterCard> getMonsterCards() {
        List<MonsterCard> result = new ArrayList<>();
        for (MonsterCard card : allCards.cards().monsterCards().iterable()) {
            result.add(card);
        }
        return result;
    }

    public List<Card> getAllCards() {
        List<Card> result = new ArrayList<>();
        for (Card card : allCards.cards().iterable()) {
            result.add(card);
        }
        return result;
    }
}
