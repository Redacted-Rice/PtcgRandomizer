package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.awt.Component;
import java.awt.GraphicsEnvironment;

import javax.swing.JOptionPane;

// User-facing "Invalid Input" dialogs from module config editors. Injectable for automated tests
// so structural edits with bad field values do not block on a modal JOptionPane.
public final class InvalidInputDialogs {
    @FunctionalInterface
    public interface Notifier {
        void show(Component parent, String message);
    }

    private static Notifier notifier = InvalidInputDialogs::showDialog;

    private InvalidInputDialogs() {}

    public static void show(Component parent, String message) {
        notifier.show(parent, message);
    }

    public static void setNotifierForTests(Notifier testNotifier) {
        notifier = testNotifier;
    }

    public static void resetNotifierForTests() {
        notifier = InvalidInputDialogs::showDialog;
    }

    private static void showDialog(Component parent, String message) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        JOptionPane.showMessageDialog(parent, message, "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
    }
}
