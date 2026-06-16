package redactedrice.ptcgr.config;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import redactedrice.ptcgr.config.support.ConfigWarningCollector;
import redactedrice.ptcgr.config.support.MoveExclusionData;
import redactedrice.ptcgr.constants.CardConstants.CardId;
import redactedrice.ptcgr.data.Move;

public class MoveExclusions {
    private final Map<CardId, List<MoveExclusionData>> exclByCardId;
    private final Map<String, List<MoveExclusionData>> exclByMoveName;

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

    public List<MoveExclusionData> getAllExclusions() {
        List<MoveExclusionData> all = new ArrayList<>();
        exclByCardId.values().forEach(all::addAll);
        exclByMoveName.values().forEach(all::addAll);
        return List.copyOf(all);
    }

    private boolean anyExclusionMatches(CardId id, Move move,
            boolean checkAgainstRemovedFromPoolListInsteadOfExludedFromRandList,
            List<MoveExclusionData> foundExcl) {
        if (foundExcl != null) {
            for (MoveExclusionData excl : foundExcl) {
                if (checkAgainstRemovedFromPoolListInsteadOfExludedFromRandList) {
                    return excl.isRemoveFromPool() && excl.matchesMove(id, move);
                } else {
                    return excl.isExcludeFromRandomization() && excl.matchesMove(id, move);
                }
            }
        }

        return false;
    }

    public void addMoveExclusion(CardId cardId, String moveName, boolean removeFromPool,
            boolean excludeFromRandomization, String sourceFileName) {
        addMoveExclusion(cardId, moveName, removeFromPool, excludeFromRandomization, sourceFileName,
                null);
    }

    public void addMoveExclusion(CardId cardId, String moveName, boolean removeFromPool,
            boolean excludeFromRandomization, String sourceFileName,
            ConfigWarningCollector warnings) {
        MoveExclusionData excl = new MoveExclusionData(cardId, moveName, removeFromPool,
                excludeFromRandomization, sourceFileName);
        List<MoveExclusionData> bucket;
        if (excl.isCardIdSet()) {
            bucket = exclByCardId.computeIfAbsent(excl.getCardId(), ll -> new LinkedList<>());
        } else if (excl.isMoveNameSet()) {
            bucket = exclByMoveName.computeIfAbsent(excl.getMoveName(), ll -> new LinkedList<>());
        } else {
            return;
        }

        for (MoveExclusionData existing : bucket) {
            if (!existing.hasSameTarget(excl)) {
                continue;
            }
            if (existing.hasSameSettings(excl)) {
                return;
            }
            if (warnings != null) {
                String targetLabel = excl.isCardIdSet() ? "card \"" + cardId + "\"" : "move";
                warnings.appendWarningLine("Conflicting exclusion for " + targetLabel + " \""
                        + moveName + "\" in " + sourceFileName
                        + "; keeping the first entry and ignoring the duplicate.");
                warnings.appendWarning("\t" + sourceFileName);
            }
            return;
        }

        bucket.add(excl);
    }
}
