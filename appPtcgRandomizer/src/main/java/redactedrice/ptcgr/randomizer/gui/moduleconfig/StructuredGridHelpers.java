package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

// Shared helpers for StructuredGridPanel cells - field wrappers, remove/add buttons, and labels.
final class StructuredGridHelpers {
    static final int FIELD_MIN_WIDTH = 140;
    static final int ROW_HGAP = 6;
    static final int ROW_VGAP = 2;

    private StructuredGridHelpers() {}

    static JComponent wrapExpandableField(JComponent editorComponent) {
        return new MinWidthPanel(editorComponent, FIELD_MIN_WIDTH, true);
    }

    static JLabel createArrowLabel() {
        return new JLabel(StructuredTypeText.ARROW);
    }

    static JButton createRemoveButton(boolean visible) {
        JButton removeButton = new JButton("\u00D7");
        removeButton.setToolTipText("Remove");
        removeButton.setFocusable(false);
        removeButton.setMargin(new Insets(0, 6, 0, 6));
        removeButton.setVisible(visible);
        return constrainToPreferredSize(removeButton);
    }

    static JButton createAddButton(boolean visible) {
        JButton addButton = new JButton("+ Add");
        addButton.setFocusable(false);
        addButton.setVisible(visible);
        return constrainToPreferredSize(addButton);
    }

    private static <T extends JComponent> T constrainToPreferredSize(T component) {
        Dimension preferred = component.getPreferredSize();
        component.setMinimumSize(preferred);
        component.setMaximumSize(preferred);
        return component;
    }
}
