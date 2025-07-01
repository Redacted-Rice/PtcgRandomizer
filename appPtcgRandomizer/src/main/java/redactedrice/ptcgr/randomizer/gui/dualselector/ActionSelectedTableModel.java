package redactedrice.ptcgr.randomizer.gui.dualselector;


public class ActionSelectedTableModel extends ActionsTableModel {
    private static final long serialVersionUID = 1L;

    public ActionSelectedTableModel() {
        data.add(null);
    }

    @Override
    public int getDataRowCount() {
        return data.size() - 1;
    }
}
