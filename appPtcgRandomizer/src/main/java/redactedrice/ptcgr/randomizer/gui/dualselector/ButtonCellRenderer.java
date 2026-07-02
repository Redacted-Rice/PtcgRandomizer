package redactedrice.ptcgr.randomizer.gui.dualselector;


import javax.swing.*;
import javax.swing.table.TableCellRenderer;

import java.awt.*;

public class ButtonCellRenderer implements TableCellRenderer {
    private final String text;
    private final boolean editable;

    public ButtonCellRenderer(String text, boolean editable) {
        this.text = text;
        this.editable = editable;
    }

    public boolean isEditable() {
        return editable;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        if (value instanceof Number && ((Number) value).intValue() > 0) {
            JButton button = new JButton(text);
            if (isSelected) {
                button.setBackground(table.getSelectionBackground());
                button.setForeground(table.getSelectionForeground());
            }
            return button;
        }

        // Blocked out when there are no config options for this module
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        if (isSelected) {
            panel.setBackground(table.getSelectionBackground());
        } else {
            panel.setBackground(new Color(220, 220, 220));
        }
        return panel;
    }

}
