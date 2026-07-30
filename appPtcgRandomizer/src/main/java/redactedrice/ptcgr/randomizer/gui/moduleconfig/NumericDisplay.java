package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.math.BigDecimal;

// Shared int/double formatting for numeric editors and adjustment warning text.
final class NumericDisplay {
    private NumericDisplay() {}

    static String format(double value, boolean integer) {
        if (integer) {
            return String.valueOf((long) value);
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return String.valueOf(value);
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    static Number toTypedNumber(double value, boolean integer) {
        if (integer) {
            return Integer.valueOf((int) value);
        }
        return value;
    }
}
