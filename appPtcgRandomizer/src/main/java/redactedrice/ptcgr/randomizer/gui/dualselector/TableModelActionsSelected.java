package redactedrice.ptcgr.randomizer.gui.dualselector;


public class TableModelActionsSelected extends TableModelAction {
	private static final long serialVersionUID = 1L;

	TableModelActionsSelected() {
        super();
	}
	
    @Override
    public boolean isCellEditable(int row, int column) {
        return column == CONFIG_COLUMN; // Enable editing for the button column
    }
}
    