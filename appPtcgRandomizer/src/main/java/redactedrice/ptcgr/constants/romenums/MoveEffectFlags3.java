package redactedrice.ptcgr.constants.romenums;

import java.util.EnumSet;
import java.util.Set;
import redactedrice.gbcframework.utils.ByteUtils;


public enum MoveEffectFlags3 {
    // @formatter:off
    BOOST_IF_TAKEN_DAMAGE (1 << 0),
    UNKNOWN_BIT_1         (1 << 1),
    UNKNOWN_BIT_2         (1 << 2),
    UNKNOWN_BIT_3         (1 << 3),
    UNKNOWN_BIT_4         (1 << 4),
    UNKNOWN_BIT_5         (1 << 5),
    UNKNOWN_BIT_6         (1 << 6),
    UNKNOWN_BIT_7         (1 << 7);
    // @formatter:on

    private byte value;

    private MoveEffectFlags3(int inValue) {
        if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for MoveEffectFlags3 enum: " + inValue);
        }
        value = (byte) inValue;
    }

    public byte getValue() {
        return value;
    }

    public static Set<MoveEffectFlags3> readFromByte(byte b) {
        EnumSet<MoveEffectFlags3> readInEffects = EnumSet.noneOf(MoveEffectFlags3.class);
        for (MoveEffectFlags3 num : MoveEffectFlags3.values()) {
            if ((num.getValue() & b) != 0) {
                readInEffects.add(num);
            }
        }
        return readInEffects;
    }

    public static byte storeAsByte(Set<MoveEffectFlags3> set) {
        byte combinedValue = 0;
        for (MoveEffectFlags3 num : MoveEffectFlags3.values()) {
            if (set.contains(num)) {
                combinedValue += num.getValue();
            }
        }
        return combinedValue;
    }
}
