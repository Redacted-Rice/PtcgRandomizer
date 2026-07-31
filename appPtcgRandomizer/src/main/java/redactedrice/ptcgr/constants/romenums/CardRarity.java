package redactedrice.ptcgr.constants.romenums;

import redactedrice.gbcframework.utils.ByteUtils;


public enum CardRarity {
    // @formatter:off
    CIRCLE    (0x0,  "C"),
    DIAMOND   (0x1,  "D"),
    STAR      (0x2,  "S"),
    PROMOSTAR (0xff, "P");
    // @formatter:on

    private byte value;
    private String abbrev;

    private CardRarity(int inValue, String abbrev) {
        if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for " + "CardRarity enum: " + inValue);
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

    public static CardRarity readFromByte(byte b) {
        for (CardRarity num : CardRarity.values()) {
            if (b == num.getValue()) {
                return num;
            }
        }
        throw new IllegalArgumentException("Invalid CardType value " + b + " was passed");
    }
}
