package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import redactedrice.randomizer.lua.arguments.TypeDefinition;

// Inline ArgumentValueEditor for TABLE arguments. Each entry is a key editor, an arrow,
// a value editor, and a remove button stacked in the module config dialog's value cell, with a
// "+ Add" row pinned to the end - the same interaction model as ListInlineEditor, just keyed.
// Nested tables draw the same bordered box as nested lists.
public final class TableInlineEditor implements ArgumentValueEditor {
    private final TypeDefinition keyType;
    private final TypeDefinition valueType;
    private final EnumValuesProvider enumValuesProvider;
    private final int depth;
    private final StructuredRowsPanel rows;
    private final JComponent component;
    private final List<RowHandle> rowHandles = new ArrayList<>();
    private boolean editable = true;

    public TableInlineEditor(TypeDefinition keyType, TypeDefinition valueType,
            EnumValuesProvider enumValuesProvider) {
        this(keyType, valueType, enumValuesProvider, 0);
    }

    TableInlineEditor(TypeDefinition keyType, TypeDefinition valueType,
            EnumValuesProvider enumValuesProvider, int depth) {
        this.keyType = keyType;
        this.valueType = valueType;
        this.enumValuesProvider = enumValuesProvider;
        this.depth = depth;

        rows = new StructuredRowsPanel(
                () -> addRow(ArgumentEditorFactory.defaultValueFor(keyType, enumValuesProvider),
                        ArgumentEditorFactory.defaultValueFor(valueType, enumValuesProvider)));
        component = depth > 0
                ? StructuredInlineEditorSupport.boxNestedListContent(rows.getRowsPanel(), 0)
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

        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                addRow(entry.getKey(), entry.getValue());
            }
        }
        rows.refresh();
    }

    @Override
    public Object getValue() {
        Map<Object, Object> values = new LinkedHashMap<>();
        for (RowHandle row : rowHandles) {
            Object key = row.keyEditor.getValue();
            if (values.containsKey(key)) {
                throw new IllegalArgumentException("Duplicate table key: " + key);
            }
            values.put(key, row.valueEditor.getValue());
        }
        return values;
    }

    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
        for (RowHandle row : rowHandles) {
            row.keyEditor.setEditable(editable);
            row.valueEditor.setEditable(editable);
            row.removeButton.setVisible(editable);
        }
        rows.setAddButtonVisible(editable);
    }

    private void addRow(Object initialKey, Object initialValue) {
        ArgumentValueEditor keyEditor =
                ArgumentEditorFactory.createForType(keyType, enumValuesProvider, depth + 1, 0);
        keyEditor.setValue(initialKey);
        keyEditor.setEditable(editable);

        ArgumentValueEditor valueEditor =
                ArgumentEditorFactory.createForType(valueType, enumValuesProvider, depth + 1, 0);
        valueEditor.setValue(initialValue);
        valueEditor.setEditable(editable);

        JButton removeButton = StructuredInlineEditorSupport.createRemoveButton(editable);

        JPanel rowPanel = StructuredInlineEditorSupport.buildTableEntryRow(keyEditor.getComponent(),
                valueEditor.getComponent(), valueType, removeButton);

        RowHandle row = new RowHandle(rowPanel, keyEditor, valueEditor, removeButton);
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
        private final ArgumentValueEditor keyEditor;
        private final ArgumentValueEditor valueEditor;
        private final JButton removeButton;

        private RowHandle(JPanel rowPanel, ArgumentValueEditor keyEditor,
                ArgumentValueEditor valueEditor, JButton removeButton) {
            this.rowPanel = rowPanel;
            this.keyEditor = keyEditor;
            this.valueEditor = valueEditor;
            this.removeButton = removeButton;
        }
    }
}
