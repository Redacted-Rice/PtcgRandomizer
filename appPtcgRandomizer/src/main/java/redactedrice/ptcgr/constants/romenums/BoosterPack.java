package redactedrice.ptcgr.constants.romenums;

import redactedrice.gbcframework.utils.ByteUtils;


public enum BoosterPack {
    // @formatter:off
    PACK_C      (0x0, "Colo"),
    PACK_E      (0x1, "Evol"),
    PACK_M      (0x2, "Myst"),
    PACK_L      (0x3, "Lab"),
    PACK_PROMO  (0x4, "Prmo"),
    PACK_ENERGY (0x5, "Engy");
    // @formatter:on

    private byte value;
    private String abbrev;

    private BoosterPack(int inValue, String abbrev) {
        // stored in upper half of byte with set in the lower half but we treat it as the lower
        // half to make things make more sense in this code
        if (inValue > ByteUtils.MAX_HEX_CHAR_VALUE || inValue < ByteUtils.MIN_HEX_CHAR_VALUE) {
            throw new IllegalArgumentException("Invalid constant input for " + "BoosterPack enum "
                    + inValue + " (" + (inValue << 4) + " )");
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

    public static BoosterPack readFromHexChar(byte hexChar) {
        for (BoosterPack num : BoosterPack.values()) {
            if (hexChar == num.getValue()) {
                return num;
            }
        }
        throw new IllegalArgumentException("Invalid BoosterPack value " + hexChar + " was passed");
    }
}
