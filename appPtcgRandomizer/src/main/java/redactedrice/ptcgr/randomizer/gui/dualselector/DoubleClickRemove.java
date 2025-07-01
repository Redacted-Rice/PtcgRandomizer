package redactedrice.ptcgr.randomizer.gui.dualselector;


import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

public class DoubleClickRemove extends MouseAdapter {
    private final JTable table;
    private final ActionsTableModel model;

    public DoubleClickRemove(JTable table, ActionsTableModel model) {
        this.table = table;
        this.model = model;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) { // Double-click detection
            int row = table.rowAtPoint(e.getPoint());
            if (row >= 0) {
                model.removeRow(row);
            }
        }
    }
}
