package redactedrice.ptcgr.randomizer.gui.dualselector;


import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import redactedrice.ptcgr.randomizer.actions.Action;

public class DoubleClickCopy extends MouseAdapter {
    private final JTable fromTable;
    private final ActionsTableModel fromModel;
    private final ActionsTableModel toModel;

    public DoubleClickCopy(JTable fromTable, ActionsTableModel fromModel,
            ActionsTableModel toModel) {
        this.fromTable = fromTable;
        this.fromModel = fromModel;
        this.toModel = toModel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) { // Double-click detection
            int row = fromTable.rowAtPoint(e.getPoint());
            if (row >= 0) {
                Action action = fromModel.getRow(row);
                toModel.appendRow(action.copy());
            }
        }
    }
}
