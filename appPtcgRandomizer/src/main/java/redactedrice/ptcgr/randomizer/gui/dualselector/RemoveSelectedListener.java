package redactedrice.ptcgr.randomizer.gui.dualselector;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;

public class RemoveSelectedListener implements ActionListener {
    private final JTable table;
    private final ActionsTableModel model;

    public RemoveSelectedListener(JTable rightTable, ActionsTableModel rightModel) {
        this.table = rightTable;
        this.model = rightModel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length > 0) {
            int removedCount = selectedRows.length;
            // Compute the original count before removals.
            int origCount = model.getRowCount() + removedCount;
            int newSelection;
            int highestRemoved = selectedRows[selectedRows.length - 1];

            if (highestRemoved < origCount - 1) {
                // An item exists below the removed block.
                newSelection = highestRemoved - removedCount + 1;
            } else {
                // No item below; select the item above the removed block.
                newSelection = selectedRows[0] - 1;
                if (newSelection < 0) {
                    newSelection = 0;
                }
            }

            // Remove the selected rows in descending order to avoid shifting issues.
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                model.removeRow(selectedRows[i]);
            }

            // If the table isn’t empty, set the selection.
            if (model.getDataRowCount() > 0) {
                newSelection = Math.min(newSelection, model.getDataRowCount() - 1);
                table.setRowSelectionInterval(newSelection, newSelection);
            }
        }
    }
}
