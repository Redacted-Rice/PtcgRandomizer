package redactedrice.ptcgr.randomizer.gui.dualselector;


import redactedrice.ptcgr.randomizer.gui.ButtonCellClickHandler;
import redactedrice.ptcgr.randomizer.gui.ButtonCellRenderer;
import redactedrice.ptcgr.randomizer.gui.dualselector.TableModelAction.Columns;

public class JTableActionsSelected extends JTableHoverToolTip {

	private static final long serialVersionUID = 1L;
	private final TableModelAction model;

	public JTableActionsSelected(TableModelAction model) {
		super(model);
		this.model = model;

        setTransferHandler(new TransferHandlerReorderableTable(this));
        setDragEnabled(true);
        addMouseListener(new DoubleClickListenerRemove(this, model));

        setRowHeight(20);
		getColumnModel().getColumn(Columns.NAME.getValue()).setPreferredWidth(200);
		getColumnModel().getColumn(Columns.NAME.getValue()).setMinWidth(100);
		getColumnModel().getColumn(Columns.CONFIG.getValue()).setPreferredWidth(70);
		getColumnModel().getColumn(Columns.CONFIG.getValue()).setMinWidth(60);
		getColumnModel().getColumn(Columns.CONFIG.getValue()).setMaxWidth(80);
		
	    ButtonCellRenderer renderer = new ButtonCellRenderer("Edit");
        getColumnModel().getColumn(Columns.CONFIG.getValue()).setCellRenderer(renderer);
        getColumnModel().getColumn(Columns.CONFIG.getValue()).setCellEditor(new ButtonCellClickHandler(renderer));
	}
	
    public void moveSelectedRow(int direction) {
        int selectedRow = getSelectedRow();
        if (selectedRow != -1 && selectedRow + direction >= 0 && selectedRow + direction < model.getRowCount()) {
            model.reorderRow(selectedRow, selectedRow + direction);
            // If it would try to select the null value at the end, set it to the last actual value
            if (selectedRow + direction != model.getDataRowCount()) {
            	setRowSelectionInterval(selectedRow + direction, selectedRow + direction);
            } else {
            	setRowSelectionInterval(selectedRow, selectedRow);
            }
        }
    }
}
