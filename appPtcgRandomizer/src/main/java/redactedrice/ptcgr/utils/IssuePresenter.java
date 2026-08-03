package redactedrice.ptcgr.utils;

import java.awt.Component;
import java.util.List;

import javax.swing.JOptionPane;

import redactedrice.gbcframework.utils.IOUtils;
import redactedrice.randomizer.utils.IssueTracker;

// Swing presentation for issues already logged into IssueTracker.
// Does not log — IssueTracker logs immediately on add. After display, the phase store is cleared.
public final class IssuePresenter {
    private IssuePresenter() {}

    // Show collected warnings (if any), then clear warnings.
    public static void displayWarnings(Component parent, String handlingContext) {
        List<String> warnings = IssueTracker.getWarnings();
        if (!warnings.isEmpty()) {
            showDialog(parent, handlingContext, warnings, JOptionPane.WARNING_MESSAGE);
        }
        IssueTracker.clearWarnings();
    }

    // Show collected errors (if any), then clear errors.
    public static void displayErrors(Component parent, String handlingContext) {
        List<String> errors = IssueTracker.getErrors();
        if (!errors.isEmpty()) {
            showDialog(parent, handlingContext, errors, JOptionPane.ERROR_MESSAGE);
        }
        IssueTracker.clearErrors();
    }

    // Show errors then warnings for a finished phase, then clear the whole store.
    public static void finishPhase(Component parent, String handlingContext) {
        List<String> errors = IssueTracker.getErrors();
        List<String> warnings = IssueTracker.getWarnings();
        if (!errors.isEmpty()) {
            showDialog(parent, handlingContext, errors, JOptionPane.ERROR_MESSAGE);
        }
        if (!warnings.isEmpty()) {
            showDialog(parent, handlingContext, warnings, JOptionPane.WARNING_MESSAGE);
        }
        IssueTracker.clear();
    }

    private static void showDialog(Component parent, String handlingContext, List<String> messages,
            int messageType) {
        // No parent (tests / headless consumers): skip UI; caller still clears the store
        if (parent == null) {
            return;
        }

        StringBuilder dialogMessage = new StringBuilder();
        dialogMessage.append("The following issue(s) were encountered while handling ");
        dialogMessage.append(handlingContext);
        dialogMessage.append(":");
        for (String message : messages) {
            dialogMessage.append(IOUtils.NEWLINE);
            dialogMessage.append(message);
        }

        IOUtils.showScrollingMessageDialog(parent, dialogMessage.toString(),
                "Issue(s) encountered while handling " + handlingContext, messageType);
    }
}
