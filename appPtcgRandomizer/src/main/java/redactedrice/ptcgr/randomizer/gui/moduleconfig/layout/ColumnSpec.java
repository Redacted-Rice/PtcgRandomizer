package redactedrice.ptcgr.randomizer.gui.moduleconfig.layout;

public record ColumnSpec(int minWidth, int maxWidth, double weight) {
    public ColumnSpec {
        if (minWidth < 0 || maxWidth < minWidth || weight < 0) {
            throw new IllegalArgumentException("Invalid column spec");
        }
    }

    public static ColumnSpec bounded(int minWidth, int maxWidth, double weight) {
        return new ColumnSpec(minWidth, maxWidth, weight);
    }

    public static ColumnSpec minOnly(int minWidth, double weight) {
        return new ColumnSpec(minWidth, Integer.MAX_VALUE, weight);
    }
}
