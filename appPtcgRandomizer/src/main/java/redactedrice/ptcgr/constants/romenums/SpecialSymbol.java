package redactedrice.ptcgr.constants.romenums;

import static redactedrice.ptcgr.constants.CharMapConstants.SYMBOL_PREFIX_CHAR;

import redactedrice.gbcframework.utils.ByteUtils;


public enum SpecialSymbol {
    // @formatter:off
    SPACE        (0x00),
    FIRE         (0x01),
    GRASS        (0x02),
    LIGHTNING    (0x03),
    WATER        (0x04),
    FIGHTING     (0x05),
    PSYCHIC      (0x06),
    COLORLESS    (0x07),
    POISONED     (0x08),
    ASLEEP       (0x09),
    CONFUSED     (0x0a),
    PARALYZED    (0x0b),
    CURSOR_UP    (0x0c),
    MONSTER      (0x0d),
    ATTACK_DESC  (0x0e),
    CURSOR_RIGHT (0x0f),
    HP           (0x10),
    LEVEL        (0x11),
    E            (0x12),
    NUMBER       (0x13),
    PLUS_POWER   (0x14),
    DEFENDER     (0x15),
    HP_OK        (0x16),
    HP_NOT_OK    (0x17),
    BOX_TOP_L    (0x18),
    BOX_TOP_R    (0x19),
    BOX_BOTTOM_L (0x1a),
    BOX_BOTTOM_R (0x1b),
    BOX_TOP      (0x1c),
    BOX_BOTTOM   (0x1d),
    BOX_LEFT     (0x1e),
    BOX_RIGHT    (0x1f),
    NUMBER_0     (0x20),
    NUMBER_1     (0x21),
    NUMBER_2     (0x22),
    NUMBER_3     (0x23),
    NUMBER_4     (0x24),
    NUMBER_5     (0x25),
    NUMBER_6     (0x26),
    NUMBER_7     (0x27),
    NUMBER_8     (0x28),
    NUMBER_9     (0x29),
    DOT          (0x2a),
    PLUS         (0x2b),
    MINUS        (0x2c),
    CROSS        (0x2d),
    SLASH        (0x2e),
    CURSOR_DOWN  (0x2f),
    PRIZE        (0x30);
    // @formatter:on

    private String value;

    private SpecialSymbol(int byteVal) {
        if (byteVal > ByteUtils.MAX_BYTE_VALUE || byteVal < ByteUtils.MIN_BYTE_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for " + "SpecialSymbol enum: " + byteVal);
        }
        value = SYMBOL_PREFIX_CHAR + "" + byteVal;
    }

    public String getString() {
        return value;
    }
}
