package redactedrice.ptcgr.randomizer.gui.moduleconfig.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.ArgumentValueEditor;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.EnumValuesProvider;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.factory.ArgumentEditorFactory;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.StructuredGridHelpers;
import redactedrice.randomizer.lua.arguments.TupleEntry;
import redactedrice.randomizer.lua.arguments.TupleFieldDefinition;
import redactedrice.randomizer.lua.arguments.TypeDefinition;

// Edits a two field tuple value as side by side scalar field editors 
public final class TupleValueEditor implements ArgumentValueEditor {
    private final TupleFieldDefinition field0;
    private final TupleFieldDefinition field1;
    private final ArgumentValueEditor field0Editor;
    private final ArgumentValueEditor field1Editor;
    private final EnumValuesProvider enumValuesProvider;
    private final JPanel panel;

    public TupleValueEditor(TypeDefinition tupleType, EnumValuesProvider enumValuesProvider) {
        if (!tupleType.isTuple()) {
            throw new IllegalArgumentException("Type is not a tuple: " + tupleType);
        }
        this.field0 = tupleType.getTupleField(0);
        this.field1 = tupleType.getTupleField(1);
        this.enumValuesProvider = enumValuesProvider;
        this.field0Editor = ArgumentEditorFactory.createForType(field0.type(), enumValuesProvider);
        this.field1Editor = ArgumentEditorFactory.createForType(field1.type(), enumValuesProvider);

        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints head = new GridBagConstraints();
        head.gridx = 0;
        head.gridy = 0;
        head.weightx = 0;
        head.fill = GridBagConstraints.HORIZONTAL;
        head.anchor = GridBagConstraints.WEST;
        head.insets = new Insets(0, 0, 0, StructuredGridHelpers.ROW_HGAP);
        panel.add(StructuredGridHelpers.wrapExpandableField(field0Editor.getComponent()), head);

        GridBagConstraints tail = new GridBagConstraints();
        tail.gridx = 1;
        tail.gridy = 0;
        tail.weightx = 1;
        tail.fill = GridBagConstraints.HORIZONTAL;
        tail.anchor = GridBagConstraints.WEST;
        panel.add(StructuredGridHelpers.wrapExpandableField(field1Editor.getComponent()), tail);
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Map<?, ?> entry) {
            field0Editor.setValue(entry.get(field0.name()));
            field1Editor.setValue(entry.get(field1.name()));
            return;
        }
        field0Editor.setValue(
                ArgumentEditorFactory.defaultValueFor(field0.type(), enumValuesProvider));
        field1Editor.setValue(
                ArgumentEditorFactory.defaultValueFor(field1.type(), enumValuesProvider));
    }

    @Override
    public Object getValue() {
        return TupleEntry.of(field0.name(), field0Editor.getValue(), field1.name(),
                field1Editor.getValue());
    }

    @Override
    public void setEditable(boolean editable) {
        field0Editor.setEditable(editable);
        field1Editor.setEditable(editable);
    }
}
