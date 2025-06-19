package redactedrice.ptcgr.randomizer.gui;

import javax.swing.DropMode;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;

import redactedrice.ptcgr.randomizer.gui.dualselector.TableModelAction.Columns;

public class ActionTable extends JTable {
	private static final long serialVersionUID = 1L;
	
	public ActionTable(ActionTableModel actionsModel) {
		setDropMode(DropMode.INSERT_ROWS);
		setDragEnabled(true);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setTransferHandler(new ActionTransferHandler());
		setModel(actionsModel);
		getColumnModel().getColumn(0).setPreferredWidth(25);
		getColumnModel().getColumn(0).setMinWidth(20);
		getColumnModel().getColumn(0).setMaxWidth(30);
		getColumnModel().getColumn(1).setPreferredWidth(120);
		getColumnModel().getColumn(2).setPreferredWidth(300);
		
	    ButtonCellRenderer renderer = new ButtonCellRenderer("Edit");
        getColumnModel().getColumn(Columns.CONFIG.getValue()).setCellRenderer(renderer);
        getColumnModel().getColumn(Columns.CONFIG.getValue()).setCellEditor(new ButtonCellClickHandler(renderer));
	}
}
