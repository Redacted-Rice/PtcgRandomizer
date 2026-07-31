package redactedrice.ptcgr.constants.romenums;

import java.util.ArrayList;
import java.util.List;
import redactedrice.gbcframework.utils.ByteUtils;


public enum CardType {
    // @formatter:off
    MONSTER_FIRE            (EnergyType.FIRE.getValue()),
    MONSTER_GRASS           (EnergyType.GRASS.getValue()),
    MONSTER_LIGHTNING       (EnergyType.LIGHTNING.getValue()),
    MONSTER_WATER           (EnergyType.WATER.getValue()),
    MONSTER_FIGHTING        (EnergyType.FIGHTING.getValue()),
    MONSTER_PSYCHIC         (EnergyType.PSYCHIC.getValue()),
    MONSTER_COLORLESS       (EnergyType.COLORLESS.getValue()),
    MONSTER_UNUSED          (EnergyType.UNUSED_TYPE.getValue()),
    ENERGY_FIRE             (0x08),
    ENERGY_GRASS            (0x09),
    ENERGY_LIGHTNING        (0x0a),
    ENERGY_WATER            (0x0b),
    ENERGY_FIGHTING         (0x0c),
    ENERGY_PSYCHIC          (0x0d),
    ENERGY_DOUBLE_COLORLESS (0x0e),
    ENERGY_UNUSED           (0x0f),
    TRAINER                 (0x10),
    TRAINER_UNUSED          (0x11);
    // @formatter:on

    private static List<CardType> mons = new ArrayList<>();
    private static List<CardType> energies = new ArrayList<>();
    private static List<CardType> trainers = new ArrayList<>();

    private byte value;

    private CardType(int inValue) {
        if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for " + "CardType enum: " + inValue);
        }
        value = (byte) inValue;
    }

    public byte getValue() {
        return value;
    }

    public static CardType readFromByte(byte b) {
        for (CardType num : CardType.values()) {
            if (b == num.getValue()) {
                return num;
            }
        }
        throw new IllegalArgumentException("Invalid CardType value " + b + " was passed");
    }

    public static final List<CardType> monsterValues() {
        if (mons.isEmpty()) {
            mons.add(MONSTER_FIRE);
            mons.add(MONSTER_GRASS);
            mons.add(MONSTER_LIGHTNING);
            mons.add(MONSTER_WATER);
            mons.add(MONSTER_FIGHTING);
            mons.add(MONSTER_PSYCHIC);
            mons.add(MONSTER_COLORLESS);
        }
        return mons;
    }

    public static final List<CardType> monsterValuesWithUnused() {
        if (mons.isEmpty()) {
            mons.add(MONSTER_FIRE);
            mons.add(MONSTER_GRASS);
            mons.add(MONSTER_LIGHTNING);
            mons.add(MONSTER_WATER);
            mons.add(MONSTER_FIGHTING);
            mons.add(MONSTER_PSYCHIC);
            mons.add(MONSTER_COLORLESS);
            mons.add(MONSTER_UNUSED);
        }
        return mons;
    }

    public static final List<CardType> energyValues() {
        if (energies.isEmpty()) {
            energies.add(ENERGY_FIRE);
            energies.add(ENERGY_GRASS);
            energies.add(ENERGY_LIGHTNING);
            energies.add(ENERGY_WATER);
            energies.add(ENERGY_FIGHTING);
            energies.add(ENERGY_PSYCHIC);
            energies.add(ENERGY_DOUBLE_COLORLESS);
            energies.add(ENERGY_UNUSED);
        }
        return energies;
    }

    public static final List<CardType> trainerValues() {
        if (trainers.isEmpty()) {
            trainers.add(TRAINER);
            trainers.add(TRAINER_UNUSED);
        }
        return trainers;
    }

    public boolean isMonsterCard() {
        return monsterValues().contains(this);
    }

    public boolean isEnergyCard() {
        return energyValues().contains(this);
    }

    public boolean isTrainerCard() {
        return trainerValues().contains(this);
    }

    public EnergyType convertToEnergyType() {
        if (!isMonsterCard()) {
            return EnergyType.UNUSED_TYPE;
        }
        return EnergyType.readFromByte(getValue());
    }
}
