package redactedrice.ptcgr.constants.romenums;

import redactedrice.gbcframework.utils.ByteUtils;

public enum EvolutionStage {
    // @formatter:off
    BASIC   (0x00, "Basic", "B"),
    STAGE_1 (0x01, "Stage 1", "1"),
    STAGE_2 (0x02, "Stage 2", "2"),
    // Used dynamically by game when a stage is skipped (e.g breeder trainer card)
    // Not sure what would happen if you set it in the rom...
    STAGE_2_WITHOUT_STAGE_1(0x03, "Stage 2 Without Stage 1", "2-");
    // @formatter:on

    private byte value;
    private String name;
    private String abbrev;

    private EvolutionStage(int inValue, String name, String abbrev) {
        if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for " + "EvolutionStage enum: " + inValue);
        }
        value = (byte) inValue;
        this.name = name;
        this.abbrev = abbrev;
    }

    public byte getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public String getAbbreviation() {
        return abbrev;
    }

    public EvolutionStage getNextStage() {
        if (this.getValue() < STAGE_2.getValue()) {
            return readFromByte((byte) (getValue() + 1));
        }
        return null;
    }

    public EvolutionStage getPreviousStage() {
        if (this.getValue() > BASIC.getValue() && this.getValue() <= STAGE_2.getValue()) {
            return readFromByte((byte) (getValue() - 1));
        }
        return null;
    }

    public static EvolutionStage readFromByte(byte b) {
        for (EvolutionStage num : EvolutionStage.values()) {
            if (b == num.getValue()) {
                return num;
            }
        }
        throw new IllegalArgumentException("Invalid EvolutionStage value " + b + " was passed");
    }
}
