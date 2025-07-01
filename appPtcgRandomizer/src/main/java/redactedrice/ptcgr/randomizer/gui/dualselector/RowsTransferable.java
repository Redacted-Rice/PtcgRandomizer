package redactedrice.ptcgr.randomizer.gui.dualselector;


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.List;

public class RowsTransferable implements Transferable {
    public static final DataFlavor FLAVOR = new DataFlavor(List.class, "List of Row Indices");
    private final List<Integer> indices;

    public RowsTransferable(List<Integer> indices) {
        this.indices = indices;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { FLAVOR
        };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return FLAVOR.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor))
            throw new UnsupportedFlavorException(flavor);
        return indices;
    }
}
