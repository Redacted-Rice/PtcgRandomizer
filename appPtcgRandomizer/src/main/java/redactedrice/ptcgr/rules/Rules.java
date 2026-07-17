package redactedrice.ptcgr.rules;

import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;

/** Runtime move exclusion and assignment state for an opened ROM. */
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

    public void addMoveExclusion(MoveExclusion exclusion, CardGroup<MonsterCard> cards) {
        moveExclusions.add(exclusion, cards, moveAssignments);
    }

    public void addMoveAssignment(MoveAssignment assignment) {
        moveAssignments.add(assignment);
    }

    public void applyTo(CardGroup<MonsterCard> cards) {
        moveAssignments.assignSpecifiedMoves(cards);
    }
}
