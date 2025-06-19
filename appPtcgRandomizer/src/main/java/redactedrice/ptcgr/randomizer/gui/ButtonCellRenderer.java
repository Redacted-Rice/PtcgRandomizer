package redactedrice.ptcgr.randomizer.gui;


import javax.swing.*;
import javax.swing.table.TableCellRenderer;

import redactedrice.ptcgr.randomizer.gui.dualselector.TableModelAction;

import java.awt.*;

public class ButtonCellRenderer implements TableCellRenderer {
	private final String text;
	
	public ButtonCellRenderer(String text) {
		this.text = text;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Number && ((Number) value).intValue() > 0) {
            JButton button = new JButton(value != null ? text : "");
            button.addActionListener(e -> new ButtonCellWindow(((TableModelAction)table.getModel()).getRow(row)));
            return button;
        }
        return new JPanel(); // Empty cell if value <= 0 or null
    }

}
