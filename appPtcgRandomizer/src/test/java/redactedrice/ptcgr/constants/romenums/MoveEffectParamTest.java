package redactedrice.ptcgr.constants.romenums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class MoveEffectParamTest {

    @Test
    void interpretMapsZeroToNone() {
        assertEquals(MoveEffectParam.NONE,
                MoveEffectParam.interpret((byte) 0, EnumSet.noneOf(MoveEffectFlags2.class)));
    }

    @Test
    void interpretMapsTenHpHealWithoutFlagContext() {
        assertEquals(MoveEffectParam.HEALING_EQUALS_10_HP,
                MoveEffectParam.interpret(MoveEffectParam.HEALING_EQUALS_10_HP.getValue(),
                        EnumSet.noneOf(MoveEffectFlags2.class)));
    }

    @Test
    void interpretUsesHealSelfForHalfDamageParam() {
        Set<MoveEffectFlags2> flags = EnumSet.of(MoveEffectFlags2.HEAL_SELF);

        assertEquals(MoveEffectParam.HEALING_EQUALS_HALF_DAMAGE_DEALT,
                MoveEffectParam.interpret(MoveEffectParam.HEALING_EQUALS_HALF_DAMAGE_DEALT.getValue(),
                        flags));
    }

    @Test
    void interpretUsesEnergyBoostForLimitedBoostParam() {
        Set<MoveEffectFlags2> flags = EnumSet.of(MoveEffectFlags2.ATTACHED_ENERGY_BOOST);

        assertEquals(MoveEffectParam.MAX_ENERGY_BOOST_IS_LIMITED,
                MoveEffectParam.interpret(MoveEffectParam.MAX_ENERGY_BOOST_IS_LIMITED.getValue(),
                        flags));
    }

    @Test
    void interpretUsesEnergyBoostForUnlimitedBoostParam() {
        Set<MoveEffectFlags2> flags = EnumSet.of(MoveEffectFlags2.ATTACHED_ENERGY_BOOST);

        assertEquals(MoveEffectParam.MAX_ENERGY_BOOST_IS_NOT_LIMITED,
                MoveEffectParam.interpret(
                        MoveEffectParam.MAX_ENERGY_BOOST_IS_NOT_LIMITED.getValue(), flags));
    }

    @Test
    void interpretReturnsNullWhenParamNeedsMissingFlags() {
        assertNull(MoveEffectParam.interpret(MoveEffectParam.HEALING_EQUALS_DAMAGE_DEALT.getValue(),
                EnumSet.noneOf(MoveEffectFlags2.class)));
        assertNull(MoveEffectParam.interpret(MoveEffectParam.HEALING_EQUALS_HALF_DAMAGE_DEALT.getValue(),
                EnumSet.noneOf(MoveEffectFlags2.class)));
    }
}
