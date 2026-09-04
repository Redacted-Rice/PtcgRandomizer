package redactedrice.ptcgr.constants.romenums;

import java.util.Set;

import redactedrice.gbcframework.utils.ByteUtils;

public enum MoveEffectParam {
    // CARD_DATA_ATTACK*_EFFECT_PARAM from pret card_data_constants.asm
    // scalar param, not a bitfield. bytes $02 and $03 depend on effect flags 2
    // @formatter:off
    NONE                             (0x00),
    HEALING_EQUALS_10_HP             (0x01),
    HEALING_EQUALS_HALF_DAMAGE_DEALT (0x02),
    HEALING_EQUALS_DAMAGE_DEALT      (0x03),
    MAX_ENERGY_BOOST_IS_LIMITED      (0x02),
    MAX_ENERGY_BOOST_IS_NOT_LIMITED  (0x03);
    // @formatter:on

    private final byte value;

    private MoveEffectParam(int inValue) {
        if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for MoveEffectParam enum: " + inValue);
        }
        value = (byte) inValue;
    }

    public byte getValue() {
        return value;
    }

    /** Maps a ROM effect param byte plus effect flags 2 to a semantic label, if known. */
    public static MoveEffectParam interpret(byte b, Set<MoveEffectFlags2> effectFlags2) {
        if (b == 0) {
            return NONE;
        }
        if (b == HEALING_EQUALS_10_HP.value) {
            return HEALING_EQUALS_10_HP;
        }

        boolean healUser = effectFlags2.contains(MoveEffectFlags2.HEAL_SELF);
        boolean energyBoost = effectFlags2.contains(MoveEffectFlags2.ATTACHED_ENERGY_BOOST);

        if (b == HEALING_EQUALS_HALF_DAMAGE_DEALT.value) {
            if (energyBoost) {
                return MAX_ENERGY_BOOST_IS_LIMITED;
            }
            if (healUser) {
                return HEALING_EQUALS_HALF_DAMAGE_DEALT;
            }
        } else if (b == HEALING_EQUALS_DAMAGE_DEALT.value) {
            if (energyBoost) {
                return MAX_ENERGY_BOOST_IS_NOT_LIMITED;
            }
            if (healUser) {
                return HEALING_EQUALS_DAMAGE_DEALT;
            }
        }

        return null;
    }
}
