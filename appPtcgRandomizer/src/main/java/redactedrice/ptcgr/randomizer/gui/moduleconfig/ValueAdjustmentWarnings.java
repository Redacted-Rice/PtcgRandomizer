package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.awt.Component;
import java.awt.GraphicsEnvironment;

import javax.swing.JOptionPane;

// Warning popups when a free entry numeric field clamps or snaps the users typed value to fit
// min/max or step
final class ValueAdjustmentWarnings {
    @FunctionalInterface
    interface Notifier {
        void show(Component parent, String message);
    }

    private static Notifier notifier = ValueAdjustmentWarnings::showDialog;

    private ValueAdjustmentWarnings() {}

    static void showNumericAdjustment(Component parent, double entered, double adjusted,
            boolean integer, Double min, Double max, Double step) {
        if (Double.compare(entered, adjusted) == 0) {
            return;
        }
        notifier.show(parent,
                formatAdjustmentMessage(entered, adjusted, integer, min, max, step));
    }

    private static String formatAdjustmentMessage(double entered, double adjusted, boolean integer,
            Double min, Double max, Double step) {
        StringBuilder message = new StringBuilder();
        message.append("The entered value ");
        message.append(NumericDisplay.format(entered, integer));
        message.append(" was adjusted to ");
        message.append(NumericDisplay.format(adjusted, integer));
        message.append('.');

        StringBuilder details = new StringBuilder();
        if (min != null && max != null) {
            appendDetail(details, "allowed range " + NumericDisplay.format(min, integer) + "\u2013"
                    + NumericDisplay.format(max, integer));
        } else if (min != null) {
            appendDetail(details, "minimum " + NumericDisplay.format(min, integer));
        } else if (max != null) {
            appendDetail(details, "maximum " + NumericDisplay.format(max, integer));
        }
        if (step != null && step > 0) {
            appendDetail(details, "step " + NumericDisplay.format(step, integer));
        }
        if (details.length() > 0) {
            message.append(" (");
            message.append(details);
            message.append(')');
        }
        return message.toString();
    }

    static void setNotifierForTests(Notifier testNotifier) {
        notifier = testNotifier;
    }

    static void resetNotifierForTests() {
        notifier = ValueAdjustmentWarnings::showDialog;
    }

    private static void showDialog(Component parent, String message) {
        // Headless test runs have no display to attach a dialog to
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
}
