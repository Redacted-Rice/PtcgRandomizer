package redactedrice.ptcgr.randomizer.gui.dualselector;


public class JTableActionsSelected extends JTableHoverToolTip {

	private static final long serialVersionUID = 1L;

	public JTableActionsSelected(TableModelAction model) {
		super(model, TableModelAction.Columns.DESCRIPTION.getValue());

        setTransferHandler(new TransferHandlerReorderableTable(this));
        setDragEnabled(true);
        addMouseListener(new DoubleClickListenerRemove(this, model));
	}

}
