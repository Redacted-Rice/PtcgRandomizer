package redactedrice.ptcgr.rules;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import redactedrice.ptcgr.constants.romenums.CardId;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.data.Move;
import redactedrice.randomizer.utils.IssueTracker;

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
                applyAssignmentToCard(assign, card);
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

    public static String assignmentSourceDisplayLabel(String sourceFileName) {
        if (isAssignmentDerivedExclusionSource(sourceFileName)) {
            String exclusionSource = sourceFileName.substring(0,
                    sourceFileName.length() - ASSIGNMENT_EXCLUSION_SOURCE_SUFFIX.length());
            return exclusionSource + " - exclusion";
        }
        return sourceFileName;
    }

    public static String exclusionSourceForAssignment(String sourceFileName) {
        return sourceFileName + ASSIGNMENT_EXCLUSION_SOURCE_SUFFIX;
    }

    public static boolean isAssignmentDerivedExclusionSource(String sourceFileName) {
        return sourceFileName != null
                && sourceFileName.endsWith(ASSIGNMENT_EXCLUSION_SOURCE_SUFFIX);
    }

    // Internal function behind all the adding fns
    private void add(MoveAssignment assignment, String cardLabel) {
        List<MoveAssignment> cardAssignments = assignmentsByCardId
                .computeIfAbsent(assignment.getCardId(), ll -> new LinkedList<>());
        for (MoveAssignment existing : cardAssignments) {
            if (!existing.hasSameTarget(assignment)) {
                continue;
            }
            if (existing.hasSameSettings(assignment)) {
                return;
            }
            warnConflictingAssignment(assignment, cardLabel);
            return;
        }
        cardAssignments.add(assignment);
    }

    public void add(MoveAssignment assignment) {
        add(assignment, assignment.getCardId().toString());
    }

    public void addMoveAssignment(MonsterCard targetCard, int moveSlot0Based, Move move,
            String sourceFileName) {
        MoveAssignment assign =
                new MoveAssignment(targetCard.id, moveSlot0Based, move, sourceFileName);
        add(assign, targetCard.name.toString());
    }

    public boolean removeMatching(MoveAssignment target) {
        List<MoveAssignment> cardAssignments = assignmentsByCardId.get(target.getCardId());
        if (cardAssignments == null) {
            return false;
        }
        boolean removed = cardAssignments
                .removeIf(existing -> existing.hasSameTarget(target) && existing.hasSameSettings(target));
        if (cardAssignments.isEmpty()) {
            assignmentsByCardId.remove(target.getCardId());
        }
        return removed;
    }

    public void clearDerivedAssignments() {
        assignmentsByCardId.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(
                    assignment -> isAssignmentDerivedExclusionSource(assignment.getSourceFileName()));
            return entry.getValue().isEmpty();
        });
    }

    public void removeDerivedFromExclusion(MoveExclusion exclusion) {
        String assignmentSource =
                exclusionSourceForAssignment(exclusion.getSourceFileName());
        assignmentsByCardId.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(assignment -> assignmentSource
                    .equals(assignment.getSourceFileName())
                    && exclusion.matchesMove(assignment.getCardId(), assignment.getMove()));
            return entry.getValue().isEmpty();
        });
    }

    public void applyAssignmentToCard(MoveAssignment assignment, MonsterCard card) {
        int moveSlot = assignment.getMoveSlot();
        if (isAssignmentDerivedExclusionSource(assignment.getSourceFileName())) {
            card.setMoveLockedViaAssignment(moveSlot, true);
        } else {
            card.setMove(assignment.getMove(), moveSlot, true);
            card.setMoveLockedViaAssignment(moveSlot, true);
        }
    }

    private static void warnConflictingAssignment(MoveAssignment assignment, String cardLabel) {
        IssueTracker.addWarning("Conflicting assignment for card \"" + cardLabel + "\" at slot "
                + (assignment.getMoveSlot() + 1) + " in " + assignment.getSourceFileName()
                + "; keeping the first entry and ignoring the duplicate.");
    }
}
