package redactedrice.ptcgr.constants.romenums;

import java.util.Set;
import redactedrice.gbcframework.utils.ByteUtils;


public enum CardSet {
    // @formatter:off
    SET_N     (0x0, "None"),
    SET_J     (0x1, "Jngl"),
    SET_F     (0x2, "Fosl"),
    SET_G     (0x7, "GB"),
    SET_PROMO (0x8, "Prmo");
    // @formatter:on

    private byte value;
    private String abbrev;

    private CardSet(int inValue, String abbrev) {
        // stored in lower half of byte with pack in the upper half
        if (inValue > ByteUtils.MAX_HEX_CHAR_VALUE || inValue < ByteUtils.MIN_HEX_CHAR_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for " + "CardSet enum: " + inValue);
        }
        value = (byte) inValue;
        this.abbrev = abbrev;
    }

    public byte getValue() {
        return value;
    }

    public String getAbbreviation() {
        return abbrev;
    }

    public static CardSet readFromHexChar(byte hexChar) {
        for (CardSet num : CardSet.values()) {
            if (hexChar == num.getValue()) {
                return num;
            }
        }
        throw new IllegalArgumentException("Invalid CardSet value " + hexChar + " was passed");
    }
}
