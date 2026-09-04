package redactedrice.ptcgr.constants.romenums;

import redactedrice.gbcframework.utils.ByteUtils;

public enum EnergyType {
    // @formatter:off
    FIRE        (0x00, "Fir", "R"), // "R" for "Red" or "fiRe"
    GRASS       (0x01, "Grs", "G"),
    LIGHTNING   (0x02, "Ltn", "L"),
    WATER       (0x03, "Wtr", "W"),
    FIGHTING    (0x04, "Fgt", "B"), // "B" for "Brown"
    PSYCHIC     (0x05, "Psy", "P"),
    COLORLESS   (0x06, "Col", "C"),
    UNUSED_TYPE (0x07, "Err", "E");
    // @formatter:on

    private byte value;
    private String abbrev;
    private String charAbbrev;

    private EnergyType(int inValue, String inAbbrev, String inCharAbbrev) {
        if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for " + "EnergyTypes enum: " + inValue);
        }
        value = (byte) inValue;
        abbrev = inAbbrev;
        charAbbrev = inCharAbbrev;
    }

    public byte getValue() {
        return value;
    }

    public String getAbbreviation() {
        return abbrev;
    }

    public String getCharAbbreviation() {
        return charAbbrev;
    }

    public static EnergyType readFromByte(byte b) {
        for (EnergyType num : EnergyType.values()) {
            if (b == num.getValue()) {
                return num;
            }
        }
        throw new IllegalArgumentException("Invalid EnergyType value " + b + " was passed");
    }

    public CardType convertToCardType() {
        return CardType.readFromByte(getValue());
    }

    public WeaknessResistanceFlags toWeaknessResistanceFlag() {
        switch (this) {
            case FIRE:
                return WeaknessResistanceFlags.FIRE;
            case GRASS:
                return WeaknessResistanceFlags.GRASS;
            case LIGHTNING:
                return WeaknessResistanceFlags.LIGHTNING;
            case WATER:
                return WeaknessResistanceFlags.WATER;
            case FIGHTING:
                return WeaknessResistanceFlags.FIGHTING;
            case PSYCHIC:
                return WeaknessResistanceFlags.PSYCHIC;
            case COLORLESS:
                // TODO: Colorless maybe has a valid value?
            case UNUSED_TYPE:
            default:
                return null;
        }
    }
}
