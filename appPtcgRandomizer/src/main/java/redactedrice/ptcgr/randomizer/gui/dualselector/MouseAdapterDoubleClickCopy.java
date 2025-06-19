package redactedrice.ptcgr.randomizer.gui.dualselector;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import redactedrice.ptcgr.randomizer.actions.Action;

public class MouseAdapterDoubleClickCopy extends MouseAdapter {
    private final JTable fromTable;
    private final TableModelAction fromModel;
    private final TableModelAction toModel;

    public MouseAdapterDoubleClickCopy(JTable fromTable, TableModelAction fromModel, TableModelAction toModel) {
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
