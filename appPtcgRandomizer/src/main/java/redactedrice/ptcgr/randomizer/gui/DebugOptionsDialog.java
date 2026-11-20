package redactedrice.ptcgr.randomizer.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import redactedrice.ptcgr.configs.AppPreferences;
import redactedrice.ptcgr.utils.IssuePresenter;
import redactedrice.randomizer.utils.LogLevel;
import redactedrice.randomizer.utils.Logger;

/** Log level and warning popup options (persisted in AppPreferences). */
final class DebugOptionsDialog {
    private DebugOptionsDialog() {
    }

    static void show(Component parent, AppPreferences preferences, Runnable onSaved) {
        if (parent == null || preferences == null) {
            return;
        }
        Window owner = javax.swing.SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Debug options", JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JLabel logLevelLbl = new JLabel("Log level:");
        JComboBox<LogLevel> logLevelCombo = new JComboBox<>(LogLevel.values());
        logLevelCombo.setSelectedItem(preferences.getLogLevel());
        logLevelCombo.setToolTipText(
                "Minimum level for console and detail log file. DEBUG includes verbose module output.");

        JCheckBox popupWarningsBox = new JCheckBox("Pop up warnings", preferences.isPopupWarnings());
        popupWarningsBox.setToolTipText(
                "When enabled, warnings collected during a phase are shown in a dialog as well as the log.");

        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 8));
        fields.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        JPanel logRow = new JPanel(new BorderLayout(8, 0));
        logRow.add(logLevelLbl, BorderLayout.WEST);
        logRow.add(logLevelCombo, BorderLayout.CENTER);
        fields.add(logRow);
        fields.add(popupWarningsBox);

        JPanel buttons = new JPanel();
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        buttons.add(ok);
        buttons.add(cancel);

        ok.addActionListener(e -> {
            LogLevel level = logLevelCombo.getSelectedItem() instanceof LogLevel selected
                    ? selected
                    : LogLevel.INFO;
            preferences.setLogLevel(level);
            preferences.setPopupWarnings(popupWarningsBox.isSelected());
            Logger.setMinLogLevel(level);
            IssuePresenter.setPopupWarningsEnabled(preferences.isPopupWarnings());
            if (onSaved != null) {
                onSaved.run();
            }
            dialog.dispose();
        });
        cancel.addActionListener(e -> dialog.dispose());

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(fields, BorderLayout.CENTER);
        dialog.getContentPane().add(buttons, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}