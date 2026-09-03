package redactedrice.ptcgr.rom;


import java.util.Collections;
import java.util.List;

import redactedrice.ptcgr.data.Card;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.data.Move;
import redactedrice.ptcgr.rules.MoveAssignments;
import redactedrice.ptcgr.rules.Rules;

public class Cards {
    private CardGroup<Card> allCards;
    private Rules rules;

    public Cards() {
        allCards = new CardGroup<>();
    }

    public Cards copy() {
        Cards copy = new Cards();
        for (Card card : allCards.iterable()) {
            copy.allCards.add(card.copy());
        }
        copy.rules = rules;
        return copy;
    }

    public CardGroup<Card> cards() {
        return allCards;
    }

    public void bindRules(Rules rules) {
        this.rules = rules;
    }

    public List<Card> getRandomizableCards() {
        return allCards.listOrderedByCardId();
    }

    public List<MonsterCard> getRandomizableMonsterCards() {
        return allCards.monsterCards().listOrderedByCardId();
    }

    /**
     * Returns move slots for randomization.
     *
     * @param includeAssigned when true, assigned slots are included (pool mode); when false they
     *        are excluded (target mode)
     * @param includeEmpty when true, empty move slots are included
     */
    public List<Move> getRandomizableMoves(boolean includeAssigned, boolean includeEmpty) {
        if (rules == null) {
            return Collections.emptyList();
        }
        MoveAssignments moveAssignments = includeAssigned ? null : rules.getMoveAssignments();
        return allCards.getRandomizableMoves(rules.getMoveExclusions(), moveAssignments,
                includeEmpty);
    }
}
