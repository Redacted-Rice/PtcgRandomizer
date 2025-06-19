package redactedrice.ptcgr.randomizer.gui.dualselector;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

public class TransferHandlerReorderableTable extends TransferHandler {
	private static final long serialVersionUID = 1L;
	private final JTable table;

    public TransferHandlerReorderableTable(JTable table) {
        this.table = table;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        int[] selectedRows = table.getSelectedRows();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedRows.length; i++) {
            if (i > 0) {
                sb.append("\n");
            }
            int row = selectedRows[i];
            // Encode the row index and cell data (assuming a single column table)
            sb.append(row).append(":").append(table.getValueAt(row, 0));
        }
        return new StringSelection(sb.toString());
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        // Allow drop only if it is a drop operation and the String flavor is supported.
        return support.isDrop() && support.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support))
            return false;
        try {
            String data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
            if (data == null || data.isEmpty())
                return false;
            String[] lines = data.split("\n");
            ArrayList<Integer> sourceIndices = new ArrayList<>();
            ArrayList<Object> values = new ArrayList<>();
            for (String line : lines) {
                String[] parts = line.split(":", 2);
                if(parts.length == 2) {
                    sourceIndices.add(Integer.parseInt(parts[0]));
                    values.add(parts[1]);
                }
            }
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            Point dropPoint = support.getDropLocation().getDropPoint();
            int dropIndex = table.rowAtPoint(dropPoint);
            if (dropIndex < 0) {
                dropIndex = model.getRowCount();
            }
            // Adjust dropIndex to account for removed rows that were originally above the drop position.
            int adjustment = 0;
            for (int idx : sourceIndices) {
                if (idx < dropIndex) {
                    adjustment++;
                }
            }
            dropIndex = dropIndex - adjustment;
            
            // Remove dragged rows from the model in descending order to avoid index shifting.
            Collections.sort(sourceIndices, Collections.reverseOrder());
            for (int idx : sourceIndices) {
                model.removeRow(idx);
            }
            // Insert dragged values at the drop location while preserving their original order.
            for (int i = 0; i < values.size(); i++) {
                model.insertRow(dropIndex + i, new Object[]{values.get(i)});
            }
            // Re-select the newly inserted rows.
            table.setRowSelectionInterval(dropIndex, dropIndex + values.size() - 1);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
