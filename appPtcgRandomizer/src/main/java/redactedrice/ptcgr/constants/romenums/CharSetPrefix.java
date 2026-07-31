package redactedrice.ptcgr.constants.romenums;

import redactedrice.gbcframework.utils.ByteUtils;

public enum CharSetPrefix {
    // @formatter:off
    EMPTY        (0x00),
    FULL_WIDTH_1 (0x01),
    FULL_WIDTH_2 (0x02),
    FULL_WIDTH_3 (0x03),
    FULL_WIDTH_4 (0x04),
    HALF_WIDTH   (0x06),
    HIRAGANA     (0x0e),
    KATAKANA     (0x0f),
    FULL_WIDTH_0 (0xff);
    // @formatter:on

    private char value;
    private String valueAsString;

    private CharSetPrefix(int inChar) {
        if (inChar > ByteUtils.MAX_BYTE_VALUE || inChar < ByteUtils.MIN_BYTE_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for " + "CharSetPrefix enum: " + inChar);
        }
        value = (char) inChar;
        valueAsString = "" + value;
    }

    public char getChar() {
        return (char) value;
    }

    public String getCharAsString() {
        return valueAsString;
    }

    public static CharSetPrefix readFromByte(byte b) {
        for (CharSetPrefix num : CharSetPrefix.values()) {
            if (b == num.getChar()) {
                return num;
            }
        }
        // Full width 0 is the default and does not have a specific
        // byte so if its not one of the others, its full width 0
        return FULL_WIDTH_0;
    }
}
