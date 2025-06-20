package redactedrice.ptcgr.randomizer.gui.dualselector;

import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.border.BevelBorder;

public abstract class ActionsHoverToolTipTable extends JTable {
	private static final long serialVersionUID = 1L;
	private final ActionsTableModel model;
	
    public ActionsHoverToolTipTable(ActionsTableModel model) {
        super(model);
        this.model = model;
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int row = rowAtPoint(event.getPoint());
        if (row >= 0) {
            return "Details: " + model.getRowDescription(row); 
        }
        return null;
    }
}