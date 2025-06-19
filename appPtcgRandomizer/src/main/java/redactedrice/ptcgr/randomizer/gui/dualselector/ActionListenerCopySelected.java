package redactedrice.ptcgr.randomizer.gui.dualselector;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;

import redactedrice.ptcgr.randomizer.actions.Action;

public class ActionListenerCopySelected implements ActionListener {
    private final JTable fromTable;
    private final TableModelAction fromModel;
    private final TableModelAction toModel;

    public ActionListenerCopySelected(JTable leftTable, TableModelAction leftModel, TableModelAction rightModel) {
        this.fromTable = leftTable;
        this.fromModel = leftModel;
        this.toModel = rightModel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int[] selectedRows = fromTable.getSelectedRows();
        for (int row : selectedRows) {
            Action action = fromModel.getRow(row);
            toModel.addRow(action.copy());
        }
    }
}
