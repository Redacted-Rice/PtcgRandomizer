package redactedrice.ptcgr.randomizer.gui.rules;

import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JComponent;

/** Shared sizing for rules tab dialogs. */
final class RulesDialogLayout {
    private static final int FIELD_WIDTH = 200;

    private RulesDialogLayout() {
    }

    static void widenField(JComponent field) {
        Dimension preferred = field.getPreferredSize();
        field.setPreferredSize(new Dimension(FIELD_WIDTH, preferred.height));
    }

    static void applyOpenSize(JDialog dialog) {
        dialog.pack();
    }

    static void prepareCombo(JComboBox<String> combo) {
        widenField(combo);
    }
}
