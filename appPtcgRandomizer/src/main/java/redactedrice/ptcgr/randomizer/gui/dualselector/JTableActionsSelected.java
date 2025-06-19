package redactedrice.ptcgr.randomizer.gui.dualselector;


public class JTableActionsSelected extends JTableHoverToolTip {

	private static final long serialVersionUID = 1L;
	private final TableModelAction model;

	public JTableActionsSelected(TableModelAction model) {
		super(model, TableModelAction.Columns.DESCRIPTION.getValue());
		this.model = model;

        setTransferHandler(new TransferHandlerReorderableTable(this));
        setDragEnabled(true);
        addMouseListener(new DoubleClickListenerRemove(this, model));
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
