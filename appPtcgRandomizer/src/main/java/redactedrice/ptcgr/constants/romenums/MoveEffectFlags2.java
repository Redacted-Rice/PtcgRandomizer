package redactedrice.ptcgr.constants.romenums;

import java.util.EnumSet;
import java.util.Set;
import redactedrice.gbcframework.utils.ByteUtils;


public enum MoveEffectFlags2 {
    // @formatter:off
    SWITCH_OPPONENT_MONSTER  (1 << 0),
    HEAL_SELF                (1 << 1),
    NULLIFY_OR_WEAKEN_ATTACK (1 << 2),
    DISCARD_ENERGY           (1 << 3),
    ATTACHED_ENERGY_BOOST    (1 << 4),
    UNKNOWN_BIT_5            (1 << 5),
    UNKNOWN_BIT_6            (1 << 6),
    UNKNOWN_BIT_7            (1 << 7);
    // @formatter:on

    private byte value;

    private MoveEffectFlags2(int inValue) {
        if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for MoveEffectFlags2 enum: " + inValue);
        }
        value = (byte) inValue;
    }

    public byte getValue() {
        return value;
    }

    public static Set<MoveEffectFlags2> readFromByte(byte b) {
        EnumSet<MoveEffectFlags2> readInEffects = EnumSet.noneOf(MoveEffectFlags2.class);
        for (MoveEffectFlags2 num : MoveEffectFlags2.values()) {
            if ((num.getValue() & b) != 0) {
                readInEffects.add(num);
            }
        }
        return readInEffects;
    }

    public static byte storeAsByte(Set<MoveEffectFlags2> set) {
        byte combinedValue = 0;
        for (MoveEffectFlags2 num : MoveEffectFlags2.values()) {
            if (set.contains(num)) {
                combinedValue += num.getValue();
            }
        }
        return combinedValue;
    }
}
