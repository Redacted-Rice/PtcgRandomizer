package redactedrice.ptcgr.constants.romenums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class RomFlagRoundTripTest {

    @Test
    void weaknessResistanceFlagsRoundTripKnownAndUnknownBits() {
        byte raw = (byte) (WeaknessResistanceFlags.FIRE.getValue()
                | WeaknessResistanceFlags.UNKNOWN_BIT_0.getValue());

        Set<WeaknessResistanceFlags> flags = WeaknessResistanceFlags.readFromByte(raw);
        assertTrue(flags.contains(WeaknessResistanceFlags.FIRE));
        assertTrue(flags.contains(WeaknessResistanceFlags.UNKNOWN_BIT_0));
        assertEquals(raw, WeaknessResistanceFlags.storeAsByte(flags));
    }

    @Test
    void moveEffectFlagsRoundTripKnownAndUnknownBits() {
        byte raw1 = MoveEffectFlags1.POISON.getValue();
        Set<MoveEffectFlags1> flags1 = MoveEffectFlags1.readFromByte(raw1);
        assertEquals(EnumSet.of(MoveEffectFlags1.POISON), flags1);
        assertEquals(raw1, MoveEffectFlags1.storeAsByte(flags1));

        byte raw2 = MoveEffectFlags2.UNKNOWN_BIT_5.getValue();
        Set<MoveEffectFlags2> flags2 = MoveEffectFlags2.readFromByte(raw2);
        assertEquals(EnumSet.of(MoveEffectFlags2.UNKNOWN_BIT_5), flags2);
        assertEquals(raw2, MoveEffectFlags2.storeAsByte(flags2));

        byte raw3 = MoveEffectFlags3.UNKNOWN_BIT_3.getValue();
        Set<MoveEffectFlags3> flags3 = MoveEffectFlags3.readFromByte(raw3);
        assertEquals(EnumSet.of(MoveEffectFlags3.UNKNOWN_BIT_3), flags3);
        assertEquals(raw3, MoveEffectFlags3.storeAsByte(flags3));
    }

    @Test
    void cardAiFlagsRoundTripUnknownBits() {
        byte raw = CardAiFlags.UNKNOWN_BIT_7.getValue();
        Set<CardAiFlags> flags = CardAiFlags.readFromByte(raw);
        assertEquals(EnumSet.of(CardAiFlags.UNKNOWN_BIT_7), flags);
        assertEquals(raw, CardAiFlags.storeAsByte(flags));
    }

    @Test
    void energyTypeMapsToWeaknessResistanceFlag() {
        assertEquals(WeaknessResistanceFlags.WATER, EnergyType.WATER.toWeaknessResistanceFlag());
        assertEquals(null, EnergyType.COLORLESS.toWeaknessResistanceFlag());
    }
}
