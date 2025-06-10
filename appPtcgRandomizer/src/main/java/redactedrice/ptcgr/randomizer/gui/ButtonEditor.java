package redactedrice.ptcgr.randomizer.gui;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
    private final JButton button;
    private Object cellValue;

    public ButtonEditor(JTable table) {
        button = new JButton("Edit");
        button.addActionListener(e -> openNewWindow());
    }

    private void openNewWindow() {
        JFrame frame = new JFrame("New Window");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new JLabel("Hello, World!", SwingConstants.CENTER));
        frame.setVisible(true);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        cellValue = value;
        if (cellValue instanceof Number && ((Number) cellValue).intValue() > 0) {
            return button; // Show button only if value > 0
        }
        return new JLabel(""); // Empty label otherwise
    }

    @Override
    public Object getCellEditorValue() {
        return cellValue;
    }
}
