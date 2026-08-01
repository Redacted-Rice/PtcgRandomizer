package redactedrice.ptcgr.randomizer.gui.dualselector.table;

import javax.swing.ListSelectionModel;

import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.ptcgr.randomizer.gui.dualselector.cell.ButtonCellClickedEditor;
import redactedrice.ptcgr.randomizer.gui.dualselector.cell.ButtonCellRenderer;
import redactedrice.ptcgr.randomizer.gui.dualselector.listener.DoubleClickCopy;
import redactedrice.ptcgr.randomizer.gui.dualselector.model.ActionsTableColumn;
import redactedrice.ptcgr.randomizer.gui.dualselector.model.ActionsTableModel;

public class ActionsListTable extends ActionsHoverToolTipTable {

    private static final long serialVersionUID = 1L;

    public ActionsListTable(ActionsTableModel listModel, ActionsTableModel selectedModel,
            ActionBank actionBank) {
        super(listModel);

        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        addMouseListener(new DoubleClickCopy(this, listModel, selectedModel));

        setRowHeight(18);
        getColumnModel().getColumn(ActionsTableColumn.NAME.getValue()).setPreferredWidth(200);
        getColumnModel().getColumn(ActionsTableColumn.NAME.getValue()).setMinWidth(100);
        getColumnModel().getColumn(ActionsTableColumn.CONFIG.getValue()).setPreferredWidth(70);
        getColumnModel().getColumn(ActionsTableColumn.CONFIG.getValue()).setMinWidth(60);
        getColumnModel().getColumn(ActionsTableColumn.CONFIG.getValue()).setMaxWidth(80);

        ButtonCellRenderer renderer = new ButtonCellRenderer("View", false);
        getColumnModel().getColumn(ActionsTableColumn.CONFIG.getValue()).setCellRenderer(renderer);
        getColumnModel().getColumn(ActionsTableColumn.CONFIG.getValue())
                .setCellEditor(new ButtonCellClickedEditor(renderer, actionBank::getEnumValues));
    }
}
