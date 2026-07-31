package redactedrice.ptcgr.data.support;

import java.util.Comparator;

import redactedrice.ptcgr.data.Move;

public final class MoveBasicSorter implements Comparator<Move> {
    @Override
    public int compare(Move m1, Move m2) {
        int compareVal = m1.name.toString().compareTo(m2.name.toString());

        if (compareVal == 0) {
            compareVal = m1.getDamageString().compareTo(m2.getDamageString());
        }

        if (compareVal == 0) {
            compareVal =
                    m1.getEnergyCostString(true, "").compareTo(m2.getEnergyCostString(true, ""));
        }

        if (compareVal == 0) {
            return m1.getEffectSortKey().compareTo(m2.getEffectSortKey());
        }

        return compareVal;
    }
}
