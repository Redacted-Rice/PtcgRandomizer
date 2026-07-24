package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import redactedrice.randomizer.lua.arguments.TypeDefinition;

// Inline ArgumentValueEditor for LIST arguments. Every entry is its own editable row (the
// element's own editor widget plus a small "x" remove button) stacked directly in the module
// config dialog's value cell, with a "+ Add" row pinned to the end
//
// Any nested list (depth > 0) draws a bordered box. Extra left indent applies only for
// list of list nesting
public final class ListInlineEditor implements ArgumentValueEditor {
    private final TypeDefinition elementType;
    private final EnumValuesProvider enumValuesProvider;
    private final int depth;
    private final int listIndentDepth;
    private final StructuredRowsPanel rows;
    private final JComponent component;
    private final List<RowHandle> rowHandles = new ArrayList<>();
    private boolean editable = true;

    public ListInlineEditor(TypeDefinition elementType, EnumValuesProvider enumValuesProvider) {
        this(elementType, enumValuesProvider, 0, 0);
    }

    ListInlineEditor(TypeDefinition elementType, EnumValuesProvider enumValuesProvider, int depth,
            int listIndentDepth) {
        this.elementType = elementType;
        this.enumValuesProvider = enumValuesProvider;
        this.depth = depth;
        this.listIndentDepth = listIndentDepth;

        rows = new StructuredRowsPanel(() -> addRow(
                ArgumentEditorFactory.defaultValueFor(elementType, enumValuesProvider)));
        component =
                depth > 0
                        ? StructuredInlineEditorSupport.boxNestedListContent(rows.getRowsPanel(),
                                listIndentDepth)
                        : rows.getRowsPanel();
    }

    @Override
    public JComponent getComponent() {
        return component;
    }

    @Override
    public void setValue(Object value) {
        rows.clearEntryRows();
        rowHandles.clear();

        List<?> list = value instanceof List<?> l ? l : List.of();
        for (Object element : list) {
            addRow(element);
        }
        rows.refresh();
    }

    @Override
    public Object getValue() {
        List<Object> values = new ArrayList<>();
        for (RowHandle row : rowHandles) {
            values.add(row.editor.getValue());
        }
        return values;
    }

    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
        for (RowHandle row : rowHandles) {
            row.editor.setEditable(editable);
            row.removeButton.setVisible(editable);
        }
        rows.setAddButtonVisible(editable);
    }

    private void addRow(Object initialValue) {
        int childListIndentDepth = elementType.isList() ? listIndentDepth + 1 : listIndentDepth;
        ArgumentValueEditor elementEditor = ArgumentEditorFactory.createForType(elementType,
                enumValuesProvider, depth + 1, childListIndentDepth);
        elementEditor.setValue(initialValue);
        elementEditor.setEditable(editable);

        JButton removeButton = StructuredInlineEditorSupport.createRemoveButton(editable);

        JPanel rowPanel = StructuredInlineEditorSupport
                .buildListEntryRow(elementEditor.getComponent(), elementType, removeButton);

        RowHandle row = new RowHandle(rowPanel, elementEditor, removeButton);
        removeButton.addActionListener(e -> removeRow(row));

        rowHandles.add(row);
        rows.insertEntryRow(rowPanel);
    }

    private void removeRow(RowHandle row) {
        rowHandles.remove(row);
        rows.removeEntryRow(row.rowPanel);
    }

    private static final class RowHandle {
        private final JPanel rowPanel;
        private final ArgumentValueEditor editor;
        private final JButton removeButton;

        private RowHandle(JPanel rowPanel, ArgumentValueEditor editor, JButton removeButton) {
            this.rowPanel = rowPanel;
            this.editor = editor;
            this.removeButton = removeButton;
        }
    }
}
