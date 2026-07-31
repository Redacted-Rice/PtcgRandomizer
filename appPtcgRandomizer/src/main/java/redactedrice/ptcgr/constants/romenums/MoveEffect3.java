package redactedrice.ptcgr.constants.romenums;

import java.util.EnumSet;
import java.util.Set;
import redactedrice.gbcframework.utils.ByteUtils;


public enum MoveEffect3 {
    // TODO later: bit 1 covers a wide variety of effects. See if we can figure it out
    // bits 2-7 are unused
    // @formatter:off
    BOOST_IF_TAKEN_DAMAGE (1 << 0),
    FLAG_3_BIT_1          (1 << 1);
    // @formatter:on

    private byte value;

    private MoveEffect3(int inValue) {
        if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for " + "MoveEffect3 enum: " + inValue);
        }
        value = (byte) inValue;
    }

    public byte getValue() {
        return value;
    }

    public static Set<MoveEffect3> readFromByte(byte b) {
        EnumSet<MoveEffect3> readInEffects = EnumSet.noneOf(MoveEffect3.class);
        for (MoveEffect3 num : MoveEffect3.values()) {
            if ((num.getValue() & b) != 0) {
                readInEffects.add(num);
            }
        }
        return readInEffects;
    }

    public static byte storeAsByte(Set<MoveEffect3> set) {
        byte combinedValue = 0;
        for (MoveEffect3 num : MoveEffect3.values()) {
            if (set.contains(num)) {
                combinedValue += num.getValue();
            }
        }
        return combinedValue;
    }
}
