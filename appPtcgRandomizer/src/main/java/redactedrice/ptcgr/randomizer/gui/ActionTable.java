package redactedrice.ptcgr.randomizer.gui;

import javax.swing.DropMode;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;

public class ActionTable extends JTable {
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
		
        getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(this));
	}
}
