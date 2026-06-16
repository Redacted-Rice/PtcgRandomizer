package redactedrice.ptcgr.rules.support;

import java.awt.Component;

import javax.swing.JOptionPane;

import redactedrice.gbcframework.utils.IOUtils;

public class RulesWarningCollector {
    private final Component toCenterPopupsOn;
    private final StringBuilder warnings = new StringBuilder();

    public RulesWarningCollector(Component toCenterPopupsOn) {
        this.toCenterPopupsOn = toCenterPopupsOn;
    }

    public void appendWarning(String message) {
        warnings.append(message);
    }

    public void appendWarningLine(String message) {
        warnings.append(IOUtils.NEWLINE);
        warnings.append(message);
    }

    public boolean hasWarnings() {
        return warnings.length() > 0;
    }

    public void displayWarningsIfPresent(String handlingContext) {
        if (warnings.length() == 0) {
            return;
        }

        StringBuilder dialogMessage = new StringBuilder();
        dialogMessage.append("The following issue(s) were encountered while handling ");
        dialogMessage.append(handlingContext);
        dialogMessage.append(":");
        dialogMessage.append(IOUtils.NEWLINE);
        dialogMessage.append(warnings);

        IOUtils.showScrollingMessageDialog(toCenterPopupsOn, dialogMessage.toString(),
                "Issue(s) encountered while handling " + handlingContext,
                JOptionPane.WARNING_MESSAGE);

        warnings.setLength(0);
    }
}
