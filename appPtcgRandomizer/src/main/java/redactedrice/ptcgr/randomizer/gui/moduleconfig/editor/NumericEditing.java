package redactedrice.ptcgr.randomizer.gui.moduleconfig.editor;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import javax.swing.JOptionPane;

// Bounds, step snapping, display formatting, and user-facing adjustment warnings for numeric
// argument editors.
public final class NumericEditing {
    @FunctionalInterface
    public interface Notifier {
        void show(Component parent, String message);
    }

    private static Notifier notifier = NumericEditing::showDialog;

    private NumericEditing() {}

    public static double applyBoundsAndStep(double value, Double min, Double max, Double step) {
        Double exceededBound = exceededBound(value, min, max);
        if (exceededBound != null) {
            value = exceededBound;
        }
        return snapToStep(value, min, max, step);
    }

    public static Number nearestChoice(Number value, boolean integer, List<Number> choices) {
        if (choices.isEmpty()) {
            return normalize(integer, value);
        }

        Number target = normalize(integer, value);
        Number nearest = choices.get(0);
        double bestDistance = Math.abs(nearest.doubleValue() - target.doubleValue());
        for (int i = 1; i < choices.size(); i++) {
            Number choice = choices.get(i);
            double distance = Math.abs(choice.doubleValue() - target.doubleValue());
            if (distance < bestDistance || distance == bestDistance
                    && choice.doubleValue() > nearest.doubleValue()) {
                nearest = choice;
                bestDistance = distance;
            }
        }
        return nearest;
    }

    public static Number normalize(boolean integer, Number value) {
        return integer ? (Number) value.intValue() : (Number) value.doubleValue();
    }

    public static String format(double value, boolean integer) {
        if (integer) {
            return String.valueOf((long) value);
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return String.valueOf(value);
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    public static Number toTypedNumber(double value, boolean integer) {
        if (integer) {
            return Integer.valueOf((int) value);
        }
        return value;
    }

    public static void showAdjustmentWarning(Component parent, double entered, double adjusted,
            boolean integer, Double min, Double max, Double step) {
        if (Double.compare(entered, adjusted) == 0) {
            return;
        }
        notifier.show(parent, formatAdjustmentMessage(entered, adjusted, integer, min, max, step));
    }

    public static void setNotifierForTests(Notifier testNotifier) {
        notifier = testNotifier;
    }

    public static void resetNotifierForTests() {
        notifier = NumericEditing::showDialog;
    }

    private static String formatAdjustmentMessage(double entered, double adjusted, boolean integer,
            Double min, Double max, Double step) {
        StringBuilder message = new StringBuilder();
        message.append("The entered value ");
        message.append(format(entered, integer));
        message.append(" was adjusted to ");
        message.append(format(adjusted, integer));
        message.append('.');

        StringBuilder details = new StringBuilder();
        if (min != null && max != null) {
            appendDetail(details, "allowed range " + format(min, integer) + "\u2013"
                    + format(max, integer));
        } else if (min != null) {
            appendDetail(details, "minimum " + format(min, integer));
        } else if (max != null) {
            appendDetail(details, "maximum " + format(max, integer));
        }
        if (step != null && step > 0) {
            appendDetail(details, "step " + format(step, integer));
        }
        if (details.length() > 0) {
            message.append(" (");
            message.append(details);
            message.append(')');
        }
        return message.toString();
    }

    private static void showDialog(Component parent, String message) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        JOptionPane.showMessageDialog(parent, message, "Value Adjusted",
                JOptionPane.WARNING_MESSAGE);
    }

    private static void appendDetail(StringBuilder details, String detail) {
        if (details.length() > 0) {
            details.append(", ");
        }
        details.append(detail);
    }

    private static double snapToStep(double value, Double min, Double max, Double step) {
        if (step == null || step <= 0 || min == null) {
            return value;
        }

        BigDecimal minBd = BigDecimal.valueOf(min);
        BigDecimal stepBd = BigDecimal.valueOf(step);
        BigDecimal snapped = minBd.add(offsetSteps(value, minBd, stepBd));

        if (max != null) {
            BigDecimal maxBd = BigDecimal.valueOf(max);
            if (snapped.compareTo(maxBd) > 0) {
                snapped = maxBd;
            }
        }
        if (snapped.compareTo(minBd) < 0) {
            snapped = minBd;
        }
        return snapped.doubleValue();
    }

    private static BigDecimal offsetSteps(double value, BigDecimal minBd, BigDecimal stepBd) {
        BigDecimal offset = BigDecimal.valueOf(value).subtract(minBd);
        long stepIndex = offset.divide(stepBd, 0, RoundingMode.HALF_UP).longValue();
        return stepBd.multiply(BigDecimal.valueOf(stepIndex));
    }

    private static Double exceededBound(double number, Double min, Double max) {
        if (Double.isNaN(number)) {
            return min != null ? min : max;
        }
        if (Double.isInfinite(number)) {
            if (number > 0) {
                return max;
            }
            return min;
        }
        if (min != null && number < min) {
            return min;
        }
        if (max != null && number > max) {
            return max;
        }
        return null;
    }
}
