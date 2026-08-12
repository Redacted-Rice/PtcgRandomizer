package redactedrice.ptcgr.randomizer.gui.dualselector.listener;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import redactedrice.ptcgr.randomizer.gui.dualselector.model.ActionsListTableModel;

public class ActionsFilterChangedListener implements ActionListener {
    private final ActionsListTableModel toUpdate;
    private final JComboBox<String> groupFilter;
    private final JComboBox<String> fieldFilter;

    public ActionsFilterChangedListener(ActionsListTableModel toUpdate,
            JComboBox<String> groupFilter, JComboBox<String> fieldFilter) {
        this.toUpdate = toUpdate;
        this.groupFilter = groupFilter;
        this.fieldFilter = fieldFilter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        toUpdate.setRowsByFilters((String) groupFilter.getSelectedItem(),
                (String) fieldFilter.getSelectedItem());
    }
}
