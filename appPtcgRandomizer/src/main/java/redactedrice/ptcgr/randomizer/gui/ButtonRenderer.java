package redactedrice.ptcgr.randomizer.gui;


import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer() {
        setText("Edit");
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Number && ((Number) value).intValue() > 0) {
            return this; // Show button if value > 0
        }
        return new JLabel(""); // Empty cell if value <= 0 or null
    }

}
