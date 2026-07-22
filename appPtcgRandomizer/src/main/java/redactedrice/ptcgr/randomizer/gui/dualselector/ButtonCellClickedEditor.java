package redactedrice.ptcgr.randomizer.gui.dualselector;


import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.ModuleConfigDialog;

public class ButtonCellClickedEditor extends AbstractCellEditor implements TableCellEditor {
    private static final long serialVersionUID = 1L;
    private final ButtonCellRenderer renderer;
    private Object cellValue;

    public ButtonCellClickedEditor(ButtonCellRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
            int row, int column) {
        cellValue = value;
        Action action = ((ActionsTableModel) table.getModel()).getRow(row);
        if (action != null && action.numConfigs() > 0) {
            Window owner = SwingUtilities.getWindowAncestor(table);
            SwingUtilities.invokeLater(
                    () -> ModuleConfigDialog.show(owner, action, renderer.isEditable()));
        }
        SwingUtilities.invokeLater(this::fireEditingCanceled);
        return renderer.getTableCellRendererComponent(table, value, isSelected, true, row, column);
    }

    @Override
    public Object getCellEditorValue() {
        return cellValue;
    }
}
