package redactedrice.ptcgr.randomizer.gui.dualselector;


import javax.swing.ListSelectionModel;

public class JTableActionList extends JTableHoverToolTip {

	private static final long serialVersionUID = 1L;

	public JTableActionList(TableModelAction listModel, TableModelAction selectedModel) {
		super(listModel, TableModelAction.Columns.DESCRIPTION.getValue());

        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        addMouseListener(new DoubleClickListenerCopy(this, listModel, selectedModel));
	}
}
