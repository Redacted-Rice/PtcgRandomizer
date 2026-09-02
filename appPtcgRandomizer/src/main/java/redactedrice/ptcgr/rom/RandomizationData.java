package redactedrice.ptcgr.rom;

import java.util.Collections;
import java.util.List;
import redactedrice.ptcgr.data.Card;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.data.Move;
import redactedrice.ptcgr.rules.MoveAssignments;
import redactedrice.ptcgr.rules.Rules;
import redactedrice.rompacker.Blocks;

public class RandomizationData {
    // TODO Now: consider making these private
    public Cards allCards;
    public Texts idsToText;
    public Blocks blocks;
    private Rules rules;

    public RandomizationData() {
        this.blocks = new Blocks();
    }

    /** Deep copies cards, texts, and blanked ROM ranges. Shares the Rules reference. */
    public RandomizationData copy() {
        RandomizationData copy = new RandomizationData();
        copy.idsToText = idsToText.copy();
        copy.allCards = allCards.copy();
        copy.blocks = blocks.copy();
        copy.rules = rules;
        return copy;
    }

    /** Associates rules for move pool queries without applying assignments. */
    public void bindRules(Rules rules) {
        this.rules = rules;
    }

    public List<Card> getRandomizableCards() {
        return allCards.cards().listOrderedByCardId();
    }

    public List<MonsterCard> getRandomizableMonsterCards() {
        return allCards.cards().monsterCards().listOrderedByCardId();
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
        return allCards.cards().getRandomizableMoves(rules.getMoveExclusions(), moveAssignments,
                includeEmpty);
    }
}
