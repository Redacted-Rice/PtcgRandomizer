package redactedrice.ptcgr.constants.romenums;

import java.util.EnumSet;
import java.util.Set;
import redactedrice.gbcframework.utils.ByteUtils;


public enum MoveEffect2 {
    // TODO: bits 5, 6 and 7 cover a wide variety of effects. See if we can figure
    // something out for them
    // @formatter:off
    SWITCH_OPPONENT_MONSTER  (1 << 0),
    HEAL_SELF                (1 << 1),
    NULLIFY_OR_WEAKEN_ATTACK (1 << 2),
    DISCARD_ENERGY           (1 << 3),
    ATTACHED_ENERGY_BOOST    (1 << 4),
    FLAG_2_BIT_5             (1 << 5),
    FLAG_2_BIT_6             (1 << 6),
    FLAG_2_BIT_7             (1 << 7);
    // @formatter:on

    private byte value;

    private MoveEffect2(int inValue) {
        if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for " + "MoveEffect2 enum: " + inValue);
        }
        value = (byte) inValue;
    }

    public byte getValue() {
        return value;
    }

    public static Set<MoveEffect2> readFromByte(byte b) {
        EnumSet<MoveEffect2> readInEffects = EnumSet.noneOf(MoveEffect2.class);
        for (MoveEffect2 num : MoveEffect2.values()) {
            if ((num.getValue() & b) != 0) {
                readInEffects.add(num);
            }
        }
        return readInEffects;
    }

    public static byte storeAsByte(Set<MoveEffect2> set) {
        byte combinedValue = 0;
        for (MoveEffect2 num : MoveEffect2.values()) {
            if (set.contains(num)) {
                combinedValue += num.getValue();
            }
        }
        return combinedValue;
    }
}
