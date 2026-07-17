package redactedrice.ptcgr.rules;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import redactedrice.ptcgr.constants.CardConstants.CardId;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.data.Move;
import redactedrice.ptcgr.utils.WarningCollector;

public class MoveAssignments {
    private static final String ASSIGNMENT_EXCLUSION_SOURCE_SUFFIX = ":assignment";

    private final Map<CardId, List<MoveAssignment>> assignmentsByCardId;

    public MoveAssignments() {
        assignmentsByCardId = new EnumMap<>(CardId.class);
    }

    public List<MoveAssignment> getAllAssignments() {
        List<MoveAssignment> all = new ArrayList<>();
        assignmentsByCardId.values().forEach(all::addAll);
        return List.copyOf(all);
    }

    public void clear() {
        assignmentsByCardId.clear();
    }

    public void assignSpecifiedMoves(CardGroup<MonsterCard> cards) {
        CardGroup<MonsterCard> foundCards = cards.withIds(assignmentsByCardId.keySet());
        for (MonsterCard card : foundCards.iterable()) {
            List<MoveAssignment> assigns = assignmentsByCardId.get(card.id);
            for (MoveAssignment assign : assigns) {
                card.setMove(assign.getMove(), assign.getMoveSlot());
                card.setMoveLockedViaAssignment(assign.getMoveSlot(), true);
            }
        }
    }

    public boolean hasAssignmentForSlot(CardId cardId, int moveSlot) {
        List<MoveAssignment> cardAssignments = assignmentsByCardId.get(cardId);
        if (cardAssignments == null) {
            return false;
        }
        for (MoveAssignment assignment : cardAssignments) {
            if (assignment.getMoveSlot() == moveSlot) {
                return true;
            }
        }
        return false;
    }

    public static String exclusionSourceForAssignment(MoveAssignment assign) {
        return exclusionSourceForAssignment(assign.getSourceFileName());
    }

    public static String exclusionSourceForAssignment(String sourceFileName) {
        return sourceFileName + ASSIGNMENT_EXCLUSION_SOURCE_SUFFIX;
    }

    public static boolean isAssignmentDerivedExclusionSource(String sourceFileName) {
        return sourceFileName != null
                && sourceFileName.endsWith(ASSIGNMENT_EXCLUSION_SOURCE_SUFFIX);
    }

    public void add(MoveAssignment assignment) {
        List<MoveAssignment> cardAssignments = assignmentsByCardId
                .computeIfAbsent(assignment.getCardId(), ll -> new LinkedList<>());
        for (MoveAssignment existing : cardAssignments) {
            if (!existing.hasSameTarget(assignment)) {
                continue;
            }
            if (existing.hasSameSettings(assignment)) {
                return;
            }
            return;
        }
        cardAssignments.add(assignment);
    }

    public void addMoveAssignment(MonsterCard targetCard, int moveSlot0Based, Move move,
            String sourceFileName) {
        addMoveAssignment(targetCard, moveSlot0Based, move, sourceFileName, null);
    }

    public void addMoveAssignment(MonsterCard targetCard, int moveSlot0Based, Move move,
            String sourceFileName, WarningCollector warnings) {
        MoveAssignment assign =
                new MoveAssignment(targetCard.id, moveSlot0Based, move, sourceFileName);
        List<MoveAssignment> cardAssignments =
                assignmentsByCardId.computeIfAbsent(assign.getCardId(), ll -> new LinkedList<>());

        for (MoveAssignment existing : cardAssignments) {
            if (!existing.hasSameTarget(assign)) {
                continue;
            }
            if (existing.hasSameSettings(assign)) {
                return;
            }
            if (warnings != null) {
                warnings.addWarning("Conflicting assignment for card \"" + targetCard.name
                        + "\" at slot " + (moveSlot0Based + 1) + " in " + sourceFileName
                        + "; keeping the first entry and ignoring the duplicate.");
            }
            return;
        }
        cardAssignments.add(assign);
    }
}
