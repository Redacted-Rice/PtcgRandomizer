package redactedrice.ptcgr.randomizer.gui.dualselector;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class ButtonCellClickHandler extends AbstractCellEditor implements TableCellEditor {
	private static final long serialVersionUID = 1L;
	private final ButtonCellRenderer renderer;
    private Object cellValue;

    public ButtonCellClickHandler(ButtonCellRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        cellValue = value;
        return renderer.getTableCellRendererComponent(table, value, isSelected, true, row, column);
    }

    @Override
    public Object getCellEditorValue() {
        return cellValue;
    }
}
