package redactedrice.ptcgr.randomizer.gui.dualselector;


import javax.swing.*;

import redactedrice.ptcgr.randomizer.gui.dualselector.TableModelActions.Columns;

public class JTableActionsList extends JTableActionsHoverToolTip {

	private static final long serialVersionUID = 1L;

	public JTableActionsList(TableModelActions listModel, TableModelActions selectedModel) {
		super(listModel);

        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        addMouseListener(new MouseAdapterDoubleClickCopy(this, listModel, selectedModel));

        setRowHeight(18);
		getColumnModel().getColumn(Columns.NAME.getValue()).setPreferredWidth(200);
		getColumnModel().getColumn(Columns.NAME.getValue()).setMinWidth(100);
		getColumnModel().getColumn(Columns.CONFIG.getValue()).setPreferredWidth(70);
		getColumnModel().getColumn(Columns.CONFIG.getValue()).setMinWidth(60);
		getColumnModel().getColumn(Columns.CONFIG.getValue()).setMaxWidth(80);
		
	    ButtonCellRenderer renderer = new ButtonCellRenderer("View");
        getColumnModel().getColumn(Columns.CONFIG.getValue()).setCellRenderer(renderer);
        getColumnModel().getColumn(Columns.CONFIG.getValue()).setCellEditor(new ButtonCellClickHandler(renderer));
	}
}
