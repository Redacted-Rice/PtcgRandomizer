package redactedrice.ptcgr.utils;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import redactedrice.gbcframework.utils.IOUtils;
import redactedrice.randomizer.utils.Logger;

public class WarningCollector {
    private final Component toCenterPopupsOn;
    private final List<String> warnings = new ArrayList<>();

    public WarningCollector(Component toCenterPopupsOn) {
        this.toCenterPopupsOn = toCenterPopupsOn;
    }

    public void addWarning(String message) {
        warnings.add(message);
    }

    public List<String> getWarnings() {
        return List.copyOf(warnings);
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public void logAndDisplay(String handlingContext, boolean clearWarnings) {
        logList(handlingContext, false);
        displayListIfPresent(handlingContext, clearWarnings);
    }

    public void logList(String handlingContext, boolean clearWarnings) {
        if (warnings.isEmpty()) {
            return;
        }

        for (String warning : warnings) {
            Logger.warn(handlingContext + ": " + warning);
        }

        if (clearWarnings) {
            warnings.clear();
        }
    }

    public void displayListIfPresent(String handlingContext, boolean clearWarnings) {
        if (warnings.isEmpty()) {
            return;
        }

        StringBuilder dialogMessage = new StringBuilder();
        dialogMessage.append("The following issue(s) were encountered while handling ");
        dialogMessage.append(handlingContext);
        dialogMessage.append(":");
        for (String warning : warnings) {
            dialogMessage.append(IOUtils.NEWLINE);
            dialogMessage.append(warning);
        }

        IOUtils.showScrollingMessageDialog(toCenterPopupsOn, dialogMessage.toString(),
                "Issue(s) encountered while handling " + handlingContext,
                JOptionPane.WARNING_MESSAGE);

        if (clearWarnings) {
            warnings.clear();
        }
    }
}
