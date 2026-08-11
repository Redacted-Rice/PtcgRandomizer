package redactedrice.ptcgr.randomizer.gui.dualselector.table;


import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.border.BevelBorder;

import redactedrice.ptcgr.randomizer.gui.dualselector.model.ActionsTableModel;

public abstract class ActionsHoverToolTipTable extends JTable {
    private static final long serialVersionUID = 1L;
    private final ActionsTableModel model;

    public ActionsHoverToolTipTable(ActionsTableModel model) {
        super(model);
        this.model = model;
        setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int viewRow = rowAtPoint(event.getPoint());
        if (viewRow < 0) {
            return null;
        }
        int modelRow = convertRowIndexToModel(viewRow);
        String description = model.getRowDescription(modelRow);
        if (description == null || description.isBlank()) {
            return null;
        }
        return description;
    }
}