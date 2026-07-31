package redactedrice.ptcgr.randomizer.gui.dualselector.model;

public enum ActionsTableColumn {
    NAME(0), CONFIG(1);

    private final int value;

    ActionsTableColumn(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    // Optional: reverse lookup
    public static ActionsTableColumn fromValue(int value) {
        for (ActionsTableColumn column : values()) {
            if (column.value == value) {
                return column;
            }
        }
        throw new IllegalArgumentException("Unknown Column value: " + value);
    }
}
