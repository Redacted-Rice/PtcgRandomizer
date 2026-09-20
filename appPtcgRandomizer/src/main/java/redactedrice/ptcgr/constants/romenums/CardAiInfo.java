package redactedrice.ptcgr.constants.romenums;

import redactedrice.gbcframework.utils.ByteUtils;

// This is a nibble - shared with CardAiFlags
public enum CardAiInfo {
    // @formatter:off
    NONE           (0x0),
    BENCH_UTILITY  (0x1),
    ENCOURAGE_EVO  (0x2),
    UNK_03         (0x3),
    UNK_05         (0x5),
    UNK_08         (0x8);
    // @formatter:on

    private final byte value;

    private CardAiInfo(int inValue) {
        if (inValue > ByteUtils.MAX_HEX_CHAR_VALUE || inValue < ByteUtils.MIN_HEX_CHAR_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for CardAiInfo enum: " + inValue);
        }
        value = (byte) inValue;
    }

    public byte getValue() {
        return value;
    }

    public static CardAiInfo readFromHexChar(byte hexChar) {
        for (CardAiInfo num : CardAiInfo.values()) {
            if (hexChar == num.getValue()) {
                return num;
            }
        }
        throw new IllegalArgumentException("Invalid CardAiInfo value " + hexChar + " was passed");
    }
}
