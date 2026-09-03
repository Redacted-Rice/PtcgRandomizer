package redactedrice.ptcgr.constants.romenums;

import java.util.EnumSet;
import java.util.Set;

import redactedrice.gbcframework.utils.ByteUtils;

public enum CardAiInfo {
    // @formatter:off
    BENCH_UTILITY (1 << 0),
    ENCOURAGE_EVO (1 << 1),
    BIT_4         (1 << 2),
    BIT_8         (1 << 3),
    HAS_EVOLUTION (1 << 4);
    // @formatter:on

    private byte value;

    private CardAiInfo(int inValue) {
        if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for " + "CardAiInfo enum: " + inValue);
        }
        value = (byte) inValue;
    }

    public byte getValue() {
        return value;
    }

    public static Set<CardAiInfo> readFromByte(byte b) {
        EnumSet<CardAiInfo> readInEffects = EnumSet.noneOf(CardAiInfo.class);
        for (CardAiInfo num : CardAiInfo.values()) {
            if ((num.getValue() & b) != 0) {
                readInEffects.add(num);
            }
        }
        return readInEffects;
    }

    public static byte storeAsByte(Set<CardAiInfo> set) {
        byte combinedValue = 0;
        for (CardAiInfo num : CardAiInfo.values()) {
            if (set.contains(num)) {
                combinedValue += num.getValue();
            }
        }
        return combinedValue;
    }
}
