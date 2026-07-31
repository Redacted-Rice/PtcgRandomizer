package redactedrice.ptcgr.constants.romenums;

import redactedrice.gbcframework.utils.ByteUtils;


public enum MoveCategory {
    // @formatter:off
    DAMAGE_NORMAL (0x00),
    DAMAGE_PLUS   (0x01),
    DAMAGE_MINUS  (0x02),
    DAMAGE_X      (0x03),
    POWER         (0x04),
    RESIDUAL      (1 << 7);
    // @formatter:on

    private byte value;

    private MoveCategory(int inValue) {
        if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for " + "MoveCategory enum: " + inValue);
        }
        value = (byte) inValue;
    }

    public byte getValue() {
        return value;
    }

    public static MoveCategory readFromByte(byte b) {
        for (MoveCategory num : MoveCategory.values()) {
            if (b == num.getValue()) {
                return num;
            }
        }
        throw new IllegalArgumentException("Invalid MoveCategory value " + b + " was passed");
    }
}
