package redactedrice.ptcgr.constants.romenums;

import java.util.EnumSet;
import java.util.Set;

import redactedrice.gbcframework.utils.ByteUtils;

public enum MoveEffect4 {
    // bytes $02 and $03 mean different things depending on effect2 flags
    // @formatter:off
    HEALING_EQUALS_10_HP             (0x01),
    HEALING_EQUALS_HALF_DAMAGE_DEALT (0x02),
    HEALING_EQUALS_DAMAGE_DEALT      (0x03),
    MAX_ENERGY_BOOST_IS_LIMITED      (0x02),
    MAX_ENERGY_BOOST_IS_NOT_LIMITED  (0x03);
    // @formatter:on

    private byte value;

    private MoveEffect4(int inValue) {
        if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for " + "MoveEffect4 enum: " + inValue);
        }
        value = (byte) inValue;
    }

    public byte getValue() {
        return value;
    }

    public static Set<MoveEffect4> readFromByte(byte b, Set<MoveEffect2> effect2) {
        EnumSet<MoveEffect4> readInEffects = EnumSet.noneOf(MoveEffect4.class);
        if (b == 0) {
            return readInEffects;
        }
        if (b == HEALING_EQUALS_10_HP.value) {
            readInEffects.add(HEALING_EQUALS_10_HP);
            return readInEffects;
        }

        boolean healUser = effect2.contains(MoveEffect2.HEAL_SELF);
        boolean energyBoost = effect2.contains(MoveEffect2.ATTACHED_ENERGY_BOOST);

        if (b == HEALING_EQUALS_HALF_DAMAGE_DEALT.value) {
            if (energyBoost) {
                readInEffects.add(MAX_ENERGY_BOOST_IS_LIMITED);
            } else if (healUser) {
                readInEffects.add(HEALING_EQUALS_HALF_DAMAGE_DEALT);
            }
        } else if (b == HEALING_EQUALS_DAMAGE_DEALT.value) {
            if (energyBoost) {
                readInEffects.add(MAX_ENERGY_BOOST_IS_NOT_LIMITED);
            } else if (healUser) {
                readInEffects.add(HEALING_EQUALS_DAMAGE_DEALT);
            }
        }

        return readInEffects;
    }

    public static byte storeAsByte(Set<MoveEffect4> set) {
        byte combinedValue = 0;
        for (MoveEffect4 num : MoveEffect4.values()) {
            if (set.contains(num)) {
                combinedValue += num.getValue();
            }
        }
        return combinedValue;
    }
}
