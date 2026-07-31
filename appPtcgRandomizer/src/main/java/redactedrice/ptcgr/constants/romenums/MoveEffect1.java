package redactedrice.ptcgr.constants.romenums;

import java.util.EnumSet;
import java.util.Set;
import redactedrice.gbcframework.utils.ByteUtils;


public enum MoveEffect1 {
    // @formatter:off
    POISON                   (1 << 0),
    SLEEP                    (1 << 1),
    PARALYSIS                (1 << 2),
    CONFUSION                (1 << 3),
    RECOIL_SMALL             (1 << 4),
    DAMAGE_TO_OPPONENT_BENCH (1 << 5),
    RECOIL_LARGE             (1 << 6),
    DRAW_CARD                (1 << 7);
    // @formatter:on

    private byte value;

    private MoveEffect1(int inValue) {
        if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for " + "MoveEffect1 enum: " + inValue);
        }
        value = (byte) inValue;
    }

    public byte getValue() {
        return value;
    }

    public static Set<MoveEffect1> readFromByte(byte b) {
        EnumSet<MoveEffect1> readInEffects = EnumSet.noneOf(MoveEffect1.class);
        for (MoveEffect1 num : MoveEffect1.values()) {
            if ((num.getValue() & b) != 0) {
                readInEffects.add(num);
            }
        }
        return readInEffects;
    }

    public static byte storeAsByte(Set<MoveEffect1> set) {
        byte combinedValue = 0;
        for (MoveEffect1 num : MoveEffect1.values()) {
            if (set.contains(num)) {
                combinedValue += num.getValue();
            }
        }
        return combinedValue;
    }
}
