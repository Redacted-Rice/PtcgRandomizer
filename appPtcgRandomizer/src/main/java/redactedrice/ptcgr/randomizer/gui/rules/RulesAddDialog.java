package redactedrice.ptcgr.randomizer.gui.rules;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.dialog.InvalidInputDialogs;
import redactedrice.randomizer.utils.IssueTracker;

/** Shared shell for rules tab add dialogs. */
abstract class RulesAddDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private boolean confirmed;

    protected RulesAddDialog(Window owner, String title) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
    }

    protected static boolean show(Component parent, RulesAddDialog dialog) {
        Window owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        return dialog.confirmed;
    }

    protected final void confirmAndClose() {
        confirmed = true;
        dispose();
    }

    protected final JPanel createFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        return form;
    }

    protected final void addFormRow(JPanel form, GridBagConstraints labelConstraints,
            GridBagConstraints fieldConstraints, int row, String label, Component field) {
        labelConstraints.gridy = row;
        fieldConstraints.gridy = row;
        form.add(new JLabel(label), labelConstraints);
        form.add(field, fieldConstraints);
    }

    protected final GridBagConstraints labelConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(4, 0, 4, 8);
        return constraints;
    }

    protected final GridBagConstraints fieldConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(4, 0, 4, 0);
        return constraints;
    }

    protected final void finishForm(JPanel form, Runnable onAdd) {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> dispose());
        JButton okButton = new JButton("Add");
        okButton.addActionListener(event -> onAdd.run());
        buttons.add(cancelButton);
        buttons.add(okButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okButton);
        RulesDialogLayout.applyOpenSize(this);
    }

    protected final void showIssueTrackerWarningOr(String fallbackMessage) {
        String message = fallbackMessage;
        var warnings = IssueTracker.getWarnings();
        if (!warnings.isEmpty()) {
            message = warnings.get(warnings.size() - 1);
        }
        InvalidInputDialogs.show(this, message);
        IssueTracker.clear();
    }
}
