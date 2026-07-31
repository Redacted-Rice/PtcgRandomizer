package redactedrice.ptcgr.constants.romenums;

import redactedrice.gbcframework.utils.ByteUtils;


public enum WeaknessResistanceType {
    // Note this is a flag. We can we have multiple weaknesses
    // TODO later: Is there a limit to the max number we can have?
    // @formatter:off
    FIRE      (0x80),
    GRASS     (0x40),
    LIGHTNING (0x20),
    WATER     (0x10),
    FIGHTING  (0x08),
    PSYCHIC   (0x04),
    // TODO later: Colorless 0x02?
    NONE      (0x00);
    // @formatter:on

    private byte value;

    private WeaknessResistanceType(int inValue) {
        if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for " + "WeaknessResistanceType enum: " + inValue);
        }
        value = (byte) inValue;
    }

    public byte getValue() {
        return value;
    }

    public static WeaknessResistanceType readFromByte(byte b) {
        for (WeaknessResistanceType num : WeaknessResistanceType.values()) {
            if (b == num.getValue()) {
                return num;
            }
        }
        throw new IllegalArgumentException(
                "Invalid WeaknessResistanceType value " + b + " was passed");
    }

    public EnergyType convertToEnergyType() {
        switch (this) {
            case FIRE:
                return EnergyType.FIRE;
            case GRASS:
                return EnergyType.GRASS;
            case LIGHTNING:
                return EnergyType.LIGHTNING;
            case FIGHTING:
                return EnergyType.FIGHTING;
            case PSYCHIC:
                return EnergyType.PSYCHIC;
            case NONE:
                return EnergyType.UNUSED_TYPE;
            default:
                return EnergyType.UNUSED_TYPE;
        }
    }
}
