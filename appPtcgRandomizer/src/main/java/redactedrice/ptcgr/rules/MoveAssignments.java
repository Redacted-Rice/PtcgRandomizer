package redactedrice.ptcgr.rules;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import redactedrice.ptcgr.rules.support.RulesWarningCollector;
import redactedrice.ptcgr.rules.support.MoveAssignmentData;
import redactedrice.ptcgr.constants.CardConstants.CardId;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.data.Move;

public class MoveAssignments {
    private static final String ASSIGNMENT_EXCLUSION_SOURCE_SUFFIX = ":assignment";

    private final Map<CardId, List<MoveAssignmentData>> assignmentsByCardId;

    public MoveAssignments() {
        assignmentsByCardId = new EnumMap<>(CardId.class);
    }

    public List<MoveAssignmentData> getAllAssignments() {
        List<MoveAssignmentData> all = new ArrayList<>();
        assignmentsByCardId.values().forEach(all::addAll);
        return List.copyOf(all);
    }

    public void assignSpecifiedMoves(CardGroup<MonsterCard> cardsToApplyTo,
            MoveExclusions exclusionsToAddTo) {
        CardGroup<MonsterCard> foundCards = cardsToApplyTo.withIds(assignmentsByCardId.keySet());
        for (MonsterCard card : foundCards.iterable()) {
            List<MoveAssignmentData> assigns = assignmentsByCardId.get(card.id);
            for (MoveAssignmentData assign : assigns) {
                card.setMove(assign.getMove(), assign.getMoveSlot());

                exclusionsToAddTo.addMoveExclusion(card.id, assign.getMove().name.toString(), false,
                        true, exclusionSourceForAssignment(assign));
            }
        }
    }

    public static String exclusionSourceForAssignment(MoveAssignmentData assign) {
        return assign.getSourceFileName() + ASSIGNMENT_EXCLUSION_SOURCE_SUFFIX;
    }

    public void addMoveAssignment(MonsterCard targetCard, int moveSlot0Based, Move move,
            String sourceFileName) {
        addMoveAssignment(targetCard, moveSlot0Based, move, sourceFileName, null);
    }

    public void addMoveAssignment(MonsterCard targetCard, int moveSlot0Based, Move move,
            String sourceFileName, RulesWarningCollector warnings) {
        MoveAssignmentData assign =
                new MoveAssignmentData(targetCard.id, moveSlot0Based, move, sourceFileName);
        List<MoveAssignmentData> cardAssignments =
                assignmentsByCardId.computeIfAbsent(assign.getCardId(), ll -> new LinkedList<>());

        for (MoveAssignmentData existing : cardAssignments) {
            if (!existing.hasSameTarget(assign)) {
                continue;
            }
            if (existing.hasSameSettings(assign)) {
                return;
            }
            if (warnings != null) {
                warnings.appendWarningLine("Conflicting assignment for card \"" + targetCard.name
                        + "\" at slot " + (moveSlot0Based + 1) + " in " + sourceFileName
                        + "; keeping the first entry and ignoring the duplicate.");
                warnings.appendWarning("\t" + sourceFileName);
            }
            return;
        }

        cardAssignments.add(assign);
    }
}
