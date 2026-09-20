package redactedrice.ptcgr.constants.romenums;

import java.util.EnumSet;
import java.util.Set;

import redactedrice.gbcframework.utils.ByteUtils;

// This is a nibble - shared with CardAiInfo
public enum CardAiFlags {
    // @formatter:off
    HAS_EVOLUTION (1 << 0),
    UNKNOWN_BIT_1 (1 << 1),
    UNKNOWN_BIT_2 (1 << 2),
    UNKNOWN_BIT_3 (1 << 3);
    // @formatter:on

    private final byte value;

    private CardAiFlags(int inValue) {
        if (inValue > ByteUtils.MAX_HEX_CHAR_VALUE || inValue < ByteUtils.MIN_HEX_CHAR_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for CardAiFlags enum: " + inValue);
        }
        value = (byte) inValue;
    }

    public byte getValue() {
        return value;
    }

    public static Set<CardAiFlags> readFromByte(byte b) {
        EnumSet<CardAiFlags> readInFlags = EnumSet.noneOf(CardAiFlags.class);
        for (CardAiFlags flag : CardAiFlags.values()) {
            if ((flag.getValue() & b) != 0) {
                readInFlags.add(flag);
            }
        }
        return readInFlags;
    }

    public static byte storeAsByte(Set<CardAiFlags> set) {
        byte combinedValue = 0;
        for (CardAiFlags flag : CardAiFlags.values()) {
            if (set.contains(flag)) {
                combinedValue += flag.getValue();
            }
        }
        return combinedValue;
    }
}
