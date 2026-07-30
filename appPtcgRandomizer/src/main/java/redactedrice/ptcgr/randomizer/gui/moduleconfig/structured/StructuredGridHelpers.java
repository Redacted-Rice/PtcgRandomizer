package redactedrice.ptcgr.randomizer.gui.moduleconfig.structured;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.ColumnSizing;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.MinWidthPanel;

// Shared helpers for StructuredGridPanel cells - field wrappers, remove/add buttons, and labels.
public final class StructuredGridHelpers {
    public static final int FIELD_MIN_WIDTH = ColumnSizing.ENTRY_BOX_WIDTH;
    public static final int ROW_HGAP = 6;
    public static final int ROW_VGAP = 2;

    private StructuredGridHelpers() {}

    public static JComponent wrapExpandableField(JComponent editorComponent) {
        return new MinWidthPanel(editorComponent, FIELD_MIN_WIDTH, true);
    }

    public static JLabel createArrowLabel() {
        return new JLabel(StructuredText.ARROW);
    }

    public static JButton createRemoveButton(boolean visible) {
        JButton removeButton = new JButton("\u00D7");
        removeButton.setToolTipText("Remove");
        removeButton.setFocusable(false);
        removeButton.setMargin(new Insets(0, 6, 0, 6));
        removeButton.setVisible(visible);
        return constrainToPreferredSize(removeButton);
    }

    public static JButton createAddButton(boolean visible) {
        JButton addButton = new JButton("+ Add");
        addButton.setFocusable(false);
        addButton.setVisible(visible);
        return addButton;
    }

    public static Insets cellInsets(int gridx, int bandStartCol) {
        return new Insets(ROW_VGAP, gridx == bandStartCol ? 0 : ROW_HGAP, ROW_VGAP, ROW_HGAP);
    }

    public static Insets paddedCellInsets() {
        return new Insets(ROW_VGAP, ROW_HGAP, ROW_VGAP, ROW_HGAP);
    }

    public static JComponent wrapFixedSizeControl(JComponent component) {
        Dimension controlSize = component.getPreferredSize();
        component.setMinimumSize(controlSize);
        // +2px fudge for L&F border paint outside preferred bounds. cellInsets() supply row gaps.
        Dimension wrapperSize = new Dimension(controlSize.width + 2, controlSize.height);
        JPanel wrapper = new JPanel(new BorderLayout()) {
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getMinimumSize() {
                return wrapperSize;
            }

            @Override
            public Dimension getPreferredSize() {
                return wrapperSize;
            }

            @Override
            public Dimension getMaximumSize() {
                return wrapperSize;
            }
        };
        wrapper.setOpaque(false);
        wrapper.add(component, BorderLayout.WEST);
        return wrapper;
    }

    private static <T extends JComponent> T constrainToPreferredSize(T component) {
        Dimension preferred = component.getPreferredSize();
        component.setMinimumSize(preferred);
        component.setMaximumSize(preferred);
        return component;
    }
}
