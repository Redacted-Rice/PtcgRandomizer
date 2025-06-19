package redactedrice.ptcgr.randomizer.gui.dualselector;

import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.table.AbstractTableModel;

public class JTableHoverToolTip extends JTable {
	private static final long serialVersionUID = 1L;
	private final int tipColumn;
	
    public JTableHoverToolTip(AbstractTableModel model, int tipColumn) {
        super(model);
        this.tipColumn = tipColumn;
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int row = rowAtPoint(event.getPoint());
        if (row >= 0) {
            return "Details: " + getValueAt(row, tipColumn); 
        }
        return null;
    }
    
    public void moveRow() {
    	
    }
}