package redactedrice.ptcgr.rules;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import redactedrice.ptcgr.constants.romenums.CardId;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.data.Move;
import redactedrice.randomizer.utils.IssueTracker;

public class MoveExclusions {
    private final Map<CardId, List<MoveExclusion>> exclByCardId;
    private final Map<String, List<MoveExclusion>> exclByMoveName;

    public MoveExclusions() {
        exclByCardId = new EnumMap<>(CardId.class);
        exclByMoveName = new HashMap<>();
    }

    public boolean isMoveRemovedFromPool(CardId id, Move move) {
        return anyExclusionMatches(id, move, true, exclByCardId.get(id))
                || anyExclusionMatches(id, move, true, exclByMoveName.get(move.name.toString()));
    }

    public boolean isMoveExcludedFromRandomization(CardId id, Move move) {
        return anyExclusionMatches(id, move, false, exclByCardId.get(id))
                || anyExclusionMatches(id, move, false, exclByMoveName.get(move.name.toString()));
    }

    public List<MoveExclusion> getAllExclusions() {
        List<MoveExclusion> all = new ArrayList<>();
        exclByCardId.values().forEach(all::addAll);
        exclByMoveName.values().forEach(all::addAll);
        return List.copyOf(all);
    }

    public void clear() {
        exclByCardId.clear();
        exclByMoveName.clear();
    }

    private boolean anyExclusionMatches(CardId id, Move move,
            boolean checkAgainstRemovedFromPoolListInsteadOfExludedFromRandList,
            List<MoveExclusion> foundExcl) {
        if (foundExcl != null) {
            for (MoveExclusion excl : foundExcl) {
                if (checkAgainstRemovedFromPoolListInsteadOfExludedFromRandList) {
                    return excl.isRemoveFromPool() && excl.matchesMove(id, move);
                } else {
                    return excl.isExcludeFromRandomization() && excl.matchesMove(id, move);
                }
            }
        }

        return false;
    }

    public void add(MoveExclusion exclusion, CardGroup<MonsterCard> cards,
            MoveAssignments assignments) {
        addExclusionOnly(exclusion);
        if (cards != null) {
            addAssignmentsForExclusion(exclusion, cards, assignments);
        }
    }

    public void addExclusionOnly(MoveExclusion exclusion) {
        List<MoveExclusion> bucket;
        if (exclusion.isCardIdSet()) {
            bucket = exclByCardId.computeIfAbsent(exclusion.getCardId(), ll -> new LinkedList<>());
        } else if (exclusion.isMoveNameSet()) {
            bucket = exclByMoveName.computeIfAbsent(exclusion.getMoveName(),
                    ll -> new LinkedList<>());
        } else {
            return;
        }
        bucket.add(exclusion);
    }

    public boolean remove(MoveExclusion exclusion) {
        if (exclusion.isCardIdSet()) {
            List<MoveExclusion> bucket = exclByCardId.get(exclusion.getCardId());
            if (removeFromBucket(bucket, exclusion)) {
                if (bucket.isEmpty()) {
                    exclByCardId.remove(exclusion.getCardId());
                }
                return true;
            }
        } else if (exclusion.isMoveNameSet()) {
            List<MoveExclusion> bucket = exclByMoveName.get(exclusion.getMoveName());
            if (removeFromBucket(bucket, exclusion)) {
                if (bucket.isEmpty()) {
                    exclByMoveName.remove(exclusion.getMoveName());
                }
                return true;
            }
        }
        return false;
    }

    private static boolean removeFromBucket(List<MoveExclusion> bucket, MoveExclusion exclusion) {
        if (bucket == null) {
            return false;
        }
        return bucket.removeIf(existing -> existing.hasSameTarget(exclusion)
                && existing.hasSameSettings(exclusion)
                && existing.getSourceFileName().equals(exclusion.getSourceFileName()));
    }

    private void addAssignmentsForExclusion(MoveExclusion exclusion, CardGroup<MonsterCard> cards,
            MoveAssignments assignments) {
        if (!exclusion.isExcludeFromRandomization()) {
            return;
        }

        for (MonsterCard card : cards.iterable()) {
            for (int moveIndex = 0; moveIndex < card.getNumMoves(); moveIndex++) {
                Move move = card.getMove(moveIndex);
                if (!move.isEmpty() && exclusion.matchesMove(card.id, move)) {
                    assignments.addMoveAssignment(card, moveIndex, move, MoveAssignments
                            .exclusionSourceForAssignment(exclusion.getSourceFileName()));
                }
            }
        }
    }

    public void generateAssignmentsFor(MoveExclusion exclusion, CardGroup<MonsterCard> cards,
            MoveAssignments assignments) {
        addAssignmentsForExclusion(exclusion, cards, assignments);
    }

    public void addMoveExclusion(CardId cardId, String moveName, boolean removeFromPool,
            boolean excludeFromRandomization, String sourceFileName, CardGroup<MonsterCard> cards,
            MoveAssignments assignments) {
        MoveExclusion excl = new MoveExclusion(cardId, moveName, removeFromPool,
                excludeFromRandomization, sourceFileName);
        List<MoveExclusion> bucket;
        if (excl.isCardIdSet()) {
            bucket = exclByCardId.computeIfAbsent(excl.getCardId(), ll -> new LinkedList<>());
        } else if (excl.isMoveNameSet()) {
            bucket = exclByMoveName.computeIfAbsent(excl.getMoveName(), ll -> new LinkedList<>());
        } else {
            return;
        }

        for (MoveExclusion existing : bucket) {
            if (!existing.hasSameTarget(excl)) {
                continue;
            }
            if (existing.hasSameSettings(excl)) {
                return;
            }
            String targetLabel = excl.isCardIdSet() ? "card \"" + cardId + "\"" : "move";
            IssueTracker.addWarning("Conflicting exclusion for " + targetLabel + " \"" + moveName
                    + "\" in " + sourceFileName
                    + "; keeping the first entry and ignoring the duplicate.");
            return;
        }
        add(excl, cards, assignments);
    }
}
