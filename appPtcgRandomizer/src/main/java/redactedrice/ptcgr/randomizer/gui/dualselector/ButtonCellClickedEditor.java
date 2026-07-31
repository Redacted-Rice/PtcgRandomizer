package redactedrice.ptcgr.randomizer.gui.dualselector;

import java.awt.Component;
import java.awt.Window;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.gui.dualselector.model.ActionsTableModel;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.EnumValuesProvider;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.dialog.ModuleConfigDialog;

public class ButtonCellClickedEditor extends AbstractCellEditor implements TableCellEditor {
    private static final long serialVersionUID = 1L;
    private final ButtonCellRenderer renderer;
    private final EnumValuesProvider enumValuesProvider;
    private Object cellValue;

    public ButtonCellClickedEditor(ButtonCellRenderer renderer,
            EnumValuesProvider enumValuesProvider) {
        this.renderer = renderer;
        this.enumValuesProvider = enumValuesProvider;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
            int row, int column) {
        cellValue = value;
        Action action = ((ActionsTableModel) table.getModel()).getRow(row);
        if (action != null && action.numConfigs() > 0) {
            Window owner = SwingUtilities.getWindowAncestor(table);
            SwingUtilities.invokeLater(() -> ModuleConfigDialog.show(owner, action,
                    renderer.isEditable(), enumValuesProvider));
        }
        SwingUtilities.invokeLater(this::fireEditingCanceled);
        return renderer.getTableCellRendererComponent(table, value, isSelected, true, row, column);
    }

    @Override
    public Object getCellEditorValue() {
        return cellValue;
    }
}
