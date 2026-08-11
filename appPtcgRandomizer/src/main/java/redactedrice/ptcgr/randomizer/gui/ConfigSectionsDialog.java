package redactedrice.ptcgr.randomizer.gui;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import redactedrice.ptcgr.configs.Config;

/** Pick rules, actions, or general settings when importing or resetting config. */
final class ConfigSectionsDialog {
    private ConfigSectionsDialog() {
    }

    record Selection(boolean rules, boolean actions, boolean generalSettings) {
        boolean isEmpty() {
            return !rules && !actions && !generalSettings;
        }
    }

    static Optional<Selection> showImport(Component parent, Config loaded) {
        return show(parent, "Add Config", "Choose which sections to add from the file:",
                loaded.hasRules(), loaded.hasActionsSection(), loaded.hasSeed());
    }

    static Optional<Selection> showReset(Component parent) {
        return show(parent, "Reset Config", "Choose which sections to reset to defaults:",
                true, true, true);
    }

    private static Optional<Selection> show(Component parent, String title, String message,
            boolean rulesAvailable, boolean actionsAvailable, boolean generalSettingsAvailable) {
        JCheckBox rulesBox = new JCheckBox("Rules", rulesAvailable);
        JCheckBox actionsBox = new JCheckBox("Actions", actionsAvailable);
        JCheckBox generalSettingsBox = new JCheckBox("General settings", generalSettingsAvailable);

        configureAvailability(rulesBox, rulesAvailable);
        configureAvailability(actionsBox, actionsAvailable);
        configureAvailability(generalSettingsBox, generalSettingsAvailable);

        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        panel.add(new JLabel(message));
        panel.add(rulesBox);
        panel.add(actionsBox);
        panel.add(generalSettingsBox);

        int choice = JOptionPane.showConfirmDialog(parent, panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return Optional.empty();
        }

        Selection selection = new Selection(rulesBox.isSelected(), actionsBox.isSelected(),
                generalSettingsBox.isSelected());
        if (selection.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Select at least one section.", title,
                    JOptionPane.INFORMATION_MESSAGE);
            return Optional.empty();
        }
        return Optional.of(selection);
    }

    private static void configureAvailability(JCheckBox box, boolean available) {
        box.setEnabled(available);
        if (!available) {
            box.setSelected(false);
        }
    }
}
