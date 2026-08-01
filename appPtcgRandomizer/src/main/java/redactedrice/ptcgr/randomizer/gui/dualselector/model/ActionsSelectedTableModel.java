package redactedrice.ptcgr.randomizer.gui.dualselector.model;


import java.util.List;

import redactedrice.ptcgr.randomizer.actions.Action;

public class ActionsSelectedTableModel extends ActionsTableModel {
    private static final long serialVersionUID = 1L;

    public ActionsSelectedTableModel() {
        data.add(null);
    }

    @Override
    public int getDataRowCount() {
        return data.size() - 1;
    }

    public void setRows(List<Action> actions) {
        data.clear();
        if (actions != null) {
            data.addAll(actions);
        }
        data.add(null);
        fireTableDataChanged();
    }
}
