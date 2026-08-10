package redactedrice.ptcgr.randomizer.gui.rules;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.ColumnSizing;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.StructuredGridHelpers;

/** Standard Swing helpers for editable rules table columns. */
final class RulesTableColumns {
    private static final String[] YES_NO_CHOICES = { "No", "Yes" };

    private RulesTableColumns() {
    }

    static void installYesNoColumn(JTable table, int columnIndex) {
        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        YesNoRenderer renderer = new YesNoRenderer();
        JComboBox<String> editCombo = new JComboBox<>(YES_NO_CHOICES);
        DefaultCellEditor editor = new DefaultCellEditor(editCombo) {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                    boolean isSelected, int row, int column) {
                String label = Boolean.TRUE.equals(value) ? "Yes" : "No";
                Component component =
                        super.getTableCellEditorComponent(table, label, isSelected, row, column);
                SwingUtilities.invokeLater(editCombo::showPopup);
                return component;
            }

            @Override
            public Object getCellEditorValue() {
                return "Yes".equals(super.getCellEditorValue());
            }
        };
        editor.setClickCountToStart(1);
        column.setCellEditor(editor);
        column.setCellRenderer(renderer);
        column.setPreferredWidth(108);
        ensureRowHeightFitsCombo(table, renderer.getDisplayCombo());
    }

    private static void ensureRowHeightFitsCombo(JTable table, JComboBox<String> comboBox) {
        int comboHeight = comboBox.getPreferredSize().height;
        if (comboHeight > table.getRowHeight()) {
            table.setRowHeight(comboHeight);
        }
    }

    private static void styleCombo(JComboBox<String> comboBox, JTable table, boolean isSelected) {
        if (isSelected) {
            comboBox.setBackground(table.getSelectionBackground());
            comboBox.setForeground(table.getSelectionForeground());
        } else {
            comboBox.setBackground(table.getBackground());
            comboBox.setForeground(table.getForeground());
        }
    }

    static void installRemoveColumn(JTable table, int modelColumnIndex, IntPredicate isRemovable,
            IntConsumer onRemove) {
        int viewColumn = table.convertColumnIndexToView(modelColumnIndex);
        TableColumn column = table.getColumnModel().getColumn(viewColumn);
        column.setCellRenderer(new RemoveButtonRenderer(isRemovable));
        column.setMinWidth(ColumnSizing.REMOVE_BUTTON_WIDTH);
        column.setMaxWidth(ColumnSizing.REMOVE_BUTTON_WIDTH);
        column.setPreferredWidth(ColumnSizing.REMOVE_BUTTON_WIDTH);
        table.addMouseListener(new RemoveClickListener(table, modelColumnIndex, isRemovable,
                onRemove));
    }

    private static final class YesNoRenderer implements TableCellRenderer {
        private final JComboBox<String> displayCombo = new JComboBox<>(YES_NO_CHOICES);

        private YesNoRenderer() {
            displayCombo.setEnabled(true);
        }

        private JComboBox<String> getDisplayCombo() {
            return displayCombo;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            displayCombo.setSelectedItem(Boolean.TRUE.equals(value) ? "Yes" : "No");
            styleCombo(displayCombo, table, isSelected);
            return displayCombo;
        }
    }

    private static final class RemoveButtonRenderer implements TableCellRenderer {
        private final JButton button = StructuredGridHelpers.createRemoveButton(true);
        private final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        private final JPanel emptyPanel = new JPanel();
        private final IntPredicate isRemovable;

        private RemoveButtonRenderer(IntPredicate isRemovable) {
            this.isRemovable = isRemovable;
            buttonPanel.setOpaque(true);
            buttonPanel.add(button);
            emptyPanel.setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = isRemovable.test(table.convertRowIndexToModel(row)) ? buttonPanel
                    : emptyPanel;
            panel.setBackground(
                    isSelected ? table.getSelectionBackground() : table.getBackground());
            return panel;
        }
    }

    private static final class RemoveClickListener extends MouseAdapter {
        private final JTable table;
        private final int modelColumnIndex;
        private final IntPredicate isRemovable;
        private final IntConsumer onRemove;
        private int pressedModelRow = -1;

        private RemoveClickListener(JTable table, int modelColumnIndex, IntPredicate isRemovable,
                IntConsumer onRemove) {
            this.table = table;
            this.modelColumnIndex = modelColumnIndex;
            this.isRemovable = isRemovable;
            this.onRemove = onRemove;
        }

        @Override
        public void mousePressed(MouseEvent event) {
            if (!SwingUtilities.isLeftMouseButton(event) || event.isPopupTrigger()) {
                return;
            }
            int viewRow = table.rowAtPoint(event.getPoint());
            int viewColumn = table.columnAtPoint(event.getPoint());
            if (!isRemoveCell(viewRow, viewColumn)) {
                return;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);
            if (!isRemovable.test(modelRow)) {
                return;
            }
            if (table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            pressedModelRow = modelRow;
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            if (!SwingUtilities.isLeftMouseButton(event) || event.isPopupTrigger()
                    || pressedModelRow < 0) {
                pressedModelRow = -1;
                return;
            }
            int viewRow = table.rowAtPoint(event.getPoint());
            int viewColumn = table.columnAtPoint(event.getPoint());
            if (isRemoveCell(viewRow, viewColumn)
                    && table.convertRowIndexToModel(viewRow) == pressedModelRow) {
                onRemove.accept(pressedModelRow);
            }
            pressedModelRow = -1;
        }

        private boolean isRemoveCell(int viewRow, int viewColumn) {
            return viewRow >= 0 && viewColumn >= 0
                    && table.convertColumnIndexToModel(viewColumn) == modelColumnIndex;
        }
    }
}
