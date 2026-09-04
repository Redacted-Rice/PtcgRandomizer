package redactedrice.ptcgr.constants.romenums;

import java.util.EnumSet;
import java.util.Set;

import redactedrice.gbcframework.utils.ByteUtils;

public enum WeaknessResistanceFlags {
    // @formatter:off
    UNKNOWN_BIT_0 (1 << 0),
    // TODO: Colorless?
    UNKNOWN_BIT_1 (1 << 1),
    PSYCHIC       (1 << 2),
    FIGHTING      (1 << 3),
    WATER         (1 << 4),
    LIGHTNING     (1 << 5),
    GRASS         (1 << 6),
    FIRE          (1 << 7);
    // @formatter:on

    private final byte value;

    private WeaknessResistanceFlags(int inValue) {
        if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for WeaknessResistanceFlags enum: " + inValue);
        }
        value = (byte) inValue;
    }

    public byte getValue() {
        return value;
    }

    // TODO: Colorless?
    public EnergyType toEnergyType() {
        switch (this) {
            case FIRE:
                return EnergyType.FIRE;
            case GRASS:
                return EnergyType.GRASS;
            case LIGHTNING:
                return EnergyType.LIGHTNING;
            case WATER:
                return EnergyType.WATER;
            case FIGHTING:
                return EnergyType.FIGHTING;
            case PSYCHIC:
                return EnergyType.PSYCHIC;
            default:
                return EnergyType.UNUSED_TYPE;
        }
    }

    public static Set<WeaknessResistanceFlags> readFromByte(byte b) {
        EnumSet<WeaknessResistanceFlags> readInFlags =
                EnumSet.noneOf(WeaknessResistanceFlags.class);
        for (WeaknessResistanceFlags flag : WeaknessResistanceFlags.values()) {
            if ((flag.getValue() & b) != 0) {
                readInFlags.add(flag);
            }
        }
        return readInFlags;
    }

    public static byte storeAsByte(Set<WeaknessResistanceFlags> set) {
        byte combinedValue = 0;
        for (WeaknessResistanceFlags flag : WeaknessResistanceFlags.values()) {
            if (set.contains(flag)) {
                combinedValue += flag.getValue();
            }
        }
        return combinedValue;
    }
}
