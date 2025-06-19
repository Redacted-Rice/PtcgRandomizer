package redactedrice.ptcgr.randomizer.gui.dualselector;

import java.util.Collection;

import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;

public class TableModelActionsList extends TableModelAction {
	private static final long serialVersionUID = 1L;
	private final ActionBank actionBank;

	TableModelActionsList(ActionBank actionBank) {
        super();
		this.actionBank = actionBank;
    }
	
	public ActionBank getActionBank()
	{
		return actionBank;
	}
	
	public void insertRowById(int index, int id)
	{
		insertRow(index, actionBank.get(id));
	}
	
	public void setRows(Collection<Action> actionBank)
	{
		data.clear();
		data.addAll(actionBank);
		fireTableDataChanged();
	}

	public void setRowsByCategory(String category) 
	{
		setRows(actionBank.get(category));
	}
}
