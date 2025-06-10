package redactedrice.ptcgr.randomizer.gui;

import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class JTableHoveredItemToolTip extends JTable {
    public JTableHoveredItemToolTip(DefaultTableModel model) {
        super(model);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int row = rowAtPoint(event.getPoint());
        if (row >= 0) {
            return "Details: " + getValueAt(row, 1);  // Shows details from column 1
        }
        return null;
    }
}