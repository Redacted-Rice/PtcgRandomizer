package redactedrice.ptcgr.rules;

import java.util.ArrayList;
import java.util.List;
import redactedrice.ptcgr.configs.rules.RulesConfig;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.randomizer.utils.IssueTracker;

/**
 * Runtime move exclusion and assignment state. Card data is updated only via
 * applyTo
 */
public class Rules {
    private final MoveExclusions moveExclusions;
    private final MoveAssignments moveAssignments;

    public Rules() {
        moveAssignments = new MoveAssignments();
        moveExclusions = new MoveExclusions();
    }

    public void clear() {
        moveExclusions.clear();
        moveAssignments.clear();
    }

    public MoveExclusions getMoveExclusions() {
        return moveExclusions;
    }

    public MoveAssignments getMoveAssignments() {
        return moveAssignments;
    }

    public boolean addMoveExclusion(MoveExclusion exclusion, CardGroup<MonsterCard> cards) {
        return addMoveExclusion(exclusion, cards, true);
    }

    public boolean addMoveExclusion(MoveExclusion exclusion, CardGroup<MonsterCard> cards,
            boolean warnOnEquivalentDuplicate) {
        return moveExclusions.tryAdd(exclusion, cards, moveAssignments, warnOnEquivalentDuplicate);
    }

    public boolean addMoveAssignment(MoveAssignment assignment, CardGroup<MonsterCard> cards) {
        return addMoveAssignment(assignment, cards, true);
    }

    public boolean addMoveAssignment(MoveAssignment assignment, CardGroup<MonsterCard> cards,
            boolean warnOnEquivalentDuplicate) {
        return moveAssignments.add(assignment, cards, warnOnEquivalentDuplicate);
    }

    public boolean addMoveAssignment(MoveAssignment assignment) {
        return moveAssignments.add(assignment);
    }

    public void removeMoveExclusion(MoveExclusion exclusion) {
        moveExclusions.remove(exclusion);
        moveAssignments.removeDerivedFromExclusion(exclusion);
    }

    public void updateMoveExclusion(MoveExclusion current, MoveExclusion replacement,
            CardGroup<MonsterCard> cards) {
        boolean excludeChanged = current.isExcludeFromRandomization() != replacement.isExcludeFromRandomization();

        moveExclusions.remove(current);
        moveExclusions.addExclusionOnly(replacement);

        if (!excludeChanged) {
            return;
        }

        moveAssignments.removeDerivedFromExclusion(current);
        if (replacement.isExcludeFromRandomization() && cards != null) {
            moveExclusions.generateAssignmentsFor(replacement, cards, moveAssignments);
        }
    }

    /**
     * Applies assignment locks to card data. Call only when building a
     * randomization workspace.
     */
    public void applyTo(CardGroup<MonsterCard> cards) {
        moveAssignments.assignSpecifiedMoves(cards);
    }

    /** Resolves exclusions loaded before a ROM was available. */
    public void resolvePendingExclusions(CardGroup<MonsterCard> cards) {
        if (cards == null) {
            return;
        }

        List<MoveExclusion> pending = new ArrayList<>();
        for (MoveExclusion exclusion : moveExclusions.getAllExclusions()) {
            if (!exclusion.isCardIdSet() && exclusion.hasCardSpecifier()) {
                pending.add(exclusion);
            }
        }

        for (MoveExclusion exclusion : pending) {
            String entryContext = exclusion.getSourceFileName() + ": pending card";
            MonsterCard card = cards.resolveCard(exclusion.getCardSpecifier(), entryContext);
            if (card == null) {
                moveExclusions.remove(exclusion);
                continue;
            }
            if (!cards.cardHasMove(card, exclusion.getMoveName())) {
                IssueTracker.addWarning(entryContext + ": failed to find move \""
                        + exclusion.getMoveName() + "\" on card \""
                        + exclusion.getCardSpecifier() + "\"; entry skipped.");
                moveExclusions.remove(exclusion);
                continue;
            }

            MoveExclusion resolved = new MoveExclusion(card.id, exclusion.getMoveName(),
                    exclusion.isRemoveFromPool(), exclusion.isExcludeFromRandomization(),
                    exclusion.getSourceFileName(), exclusion.getCardSpecifier());
            updateMoveExclusion(exclusion, resolved, cards);
        }
    }

    /** Rebuilds derived assignments from current exclusions. */
    public void refreshDerivedAssignments(CardGroup<MonsterCard> cards) {
        if (cards == null) {
            return;
        }
        moveAssignments.clearDerivedAssignments();
        for (MoveExclusion exclusion : moveExclusions.getAllExclusions()) {
            if (exclusion.isExcludeFromRandomization()) {
                moveExclusions.generateAssignmentsFor(exclusion, cards, moveAssignments);
            }
        }
    }

    /**
     * Replaces all rules from config and reconciles against the reference card
     * pool.
     */
    public void replaceFrom(RulesConfig config, CardGroup<MonsterCard> cards) {
        clear();
        if (config != null) {
            config.applyTo(this, cards);
        }
        syncWithCards(cards);
    }

    /**
     * Resolves pending exclusions and rebuilds derived assignments when a ROM is
     * available.
     */
    public void syncWithCards(CardGroup<MonsterCard> cards) {
        if (cards == null) {
            return;
        }
        resolvePendingExclusions(cards);
        refreshDerivedAssignments(cards);
    }
}
