package redactedrice.ptcgr.randomizer.gui.dualselector;


import java.util.List;

import redactedrice.ptcgr.randomizer.actions.Action;

public class ActionSelectedTableModel extends ActionsTableModel {
    private static final long serialVersionUID = 1L;

    public ActionSelectedTableModel() {
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
