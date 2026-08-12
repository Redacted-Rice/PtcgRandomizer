package redactedrice.ptcgr.randomizer.gui.dualselector.listener;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import redactedrice.ptcgr.randomizer.gui.dualselector.model.ActionsListTableModel;

public class ActionsFilterChangedListener implements ActionListener {
    private final ActionsListTableModel toUpdate;
    private final JComboBox<String> groupFilter;

    public ActionsFilterChangedListener(ActionsListTableModel toUpdate,
            JComboBox<String> groupFilter) {
        this.toUpdate = toUpdate;
        this.groupFilter = groupFilter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        toUpdate.setRowsByGroup((String) groupFilter.getSelectedItem());
    }
}
