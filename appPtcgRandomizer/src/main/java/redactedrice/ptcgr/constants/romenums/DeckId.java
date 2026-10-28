package redactedrice.ptcgr.constants.romenums;

public enum DeckId {
    // @formatter:off
    UNUSED_DECK_1            (0x00, false),
    UNUSED_DECK_2            (0x01, false),
    TUTORIAL_DECK            (0x02),
    TUTORIAL_PLAYER_DECK     (0x03),
    TUTORIAL_DECK_DUP_PTR    (0x04),
    FIRE_STARTER_DECK        (0x05),
    FIRE_EXTRA_DECK          (0x06),
    WATER_STARTER_DECK       (0x07),
    WATER_EXTRA_DECK         (0x08),
    GRASS_STARTER_DECK       (0x09),
    GRASS_EXTRA_DECK         (0x0A),
    LIGHTNING_AND_FIRE_DECK  (0x0B),
    WATER_AND_FIGHTING_DECK  (0x0C),
    GRASS_AND_PSYCHIC_DECK   (0x0D),
    LEGENDARY_FIRE_DECK      (0x0E),
    LEGENDARY_LIGHTNING_DECK (0x0F),
    LEGENDARY_WATER_DECK     (0x10),
    LEGENDARY_NORMAL_DECK    (0x11),
    FIGHTING_1               (0x12),
    FIGHTING_2               (0x13),
    WATER_1                  (0x14),
    LIGHTNING_1              (0x15),
    GRASS_1                  (0x16),
    PYSCHIC_1                (0x17),
    GRASS_2                  (0x18),
    FIRE_1                   (0x19),
    RIVAL_DECK_1             (0x1A),
    RIVAL_DECK_2             (0x1B),
    RIVAL_DECK_3             (0x1C),
    RIVAL_DECK_4             (0x1D),
    FIGHTING_3               (0x1E),
    FIGHTING_4               (0x1F),
    FIGHTING_5               (0x20),
    FIGHTING_6               (0x21),
    FIGHTING_7               (0x22),
    FIGHTING_8               (0x23),
    WATER_2                  (0x24),
    WATER_3                  (0x25),
    WATER_4                  (0x26),
    MONSTER_025_DECK         (0x27),
    LIGHTNING_2              (0x28),
    LIGHTNING_3              (0x29),
    GRASS_3                  (0x2A),
    GRASS_4                  (0x2B),
    GRASS_5                  (0x2C),
    PYSCHIC_2                (0x2D),
    PYSCHIC_3                (0x2E),
    PYSCHIC_4                (0x2F),
    GRASS_6                  (0x30),
    GRASS_7                  (0x31),
    GRASS_8                  (0x32),
    FIRE_2                   (0x33),
    FIRE_3                   (0x34),
    FIRE_4                   (0x35),
    PROMO_NPC                (0x36);
    // @formatter:on

    private byte value;
    private boolean isValidDeck;

    DeckId(int inValue) {
        this(inValue, true);
    }

    DeckId(int inValue, boolean inIsValidDeck) {
        if (inValue > Byte.MAX_VALUE || inValue < Byte.MIN_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid constant input for " + "DeckId enum: " + inValue);
        }
        value = (byte) inValue;
        isValidDeck = inIsValidDeck;
    }

    public boolean isValidDeck() {
        return isValidDeck;
    }

    byte getValue() {
        return value;
    }

    public static DeckId getDeckWithValue(int value) {
        for (DeckId num : DeckId.values()) {
            if (value == num.getValue()) {
                return num;
            }
        }
        throw new IllegalArgumentException("Invalid DeckId value " + value + " was passed");
    }

    public static DeckId readFromByte(byte b) {
        return getDeckWithValue(b);
    }
}
