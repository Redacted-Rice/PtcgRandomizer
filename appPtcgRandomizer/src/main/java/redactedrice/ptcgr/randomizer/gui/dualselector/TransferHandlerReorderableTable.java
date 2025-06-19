package redactedrice.ptcgr.randomizer.gui.dualselector;

import java.awt.datatransfer.Transferable;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

public class TransferHandlerReorderableTable extends TransferHandler {
	private static final long serialVersionUID = 1L;
	private final JTable table;

    public TransferHandlerReorderableTable(JTable table) {
        this.table = table;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        int[] selected = table.getSelectedRows();
        List<Integer> indices = Arrays.stream(selected).boxed().toList();
        return new TransferableRows(indices);
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDrop() && support.isDataFlavorSupported(TransferableRows.FLAVOR);
    }   
    
    @Override
    public boolean importData(TransferSupport support) {
        try {
            JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
            int dropIndex = dl.getRow();

            @SuppressWarnings("unchecked")
            List<Integer> fromIndices = (List<Integer>) support.getTransferable().getTransferData(TransferableRows.FLAVOR);

            ((TableModelAction) table.getModel()).reorderRows(fromIndices, dropIndex);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
