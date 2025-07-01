package redactedrice.ptcgr.randomizer.gui.dualselector;


import java.util.Collection;

import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;

public class ActionsListTableModel extends ActionsTableModel {
    private static final long serialVersionUID = 1L;
    private final ActionBank actionBank;

    ActionsListTableModel(ActionBank actionBank) {
        super();
        this.actionBank = actionBank;
    }

    public ActionBank getActionBank() {
        return actionBank;
    }

    public void insertRowById(int index, int id) {
        insertRow(index, actionBank.get(id));
    }

    public void setRows(Collection<Action> actionBank) {
        data.clear();
        data.addAll(actionBank);
        fireTableDataChanged();
    }

    public void setRowsByCategory(String category) {
        setRows(actionBank.get(category));
    }
}
