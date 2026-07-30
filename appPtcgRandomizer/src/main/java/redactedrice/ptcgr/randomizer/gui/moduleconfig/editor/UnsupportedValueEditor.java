package redactedrice.ptcgr.randomizer.gui.moduleconfig.editor;

import javax.swing.JComponent;
import javax.swing.JTextField;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.ArgumentValueEditor;

// Read only fallback when ArgumentEditorFactory cannot build an editable widget, most
// commonly because an ENUM base type references a name that is not registered in the enum
// registry (misspelled id or enum registered only at runtime). Keeps the config dialog
// renderable without crashing until the enum is available or a dedicated editor is added.
public class UnsupportedValueEditor implements ArgumentValueEditor {
    private final JTextField field;
    private Object value;

    public UnsupportedValueEditor() {
        field = new JTextField();
        field.setEditable(false);
        field.setToolTipText("Editing this argument type is not supported yet.");
    }

    @Override
    public JComponent getComponent() {
        return field;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
        field.setText(value == null ? "" : value.toString());
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setEditable(boolean editable) {
        // Never editable regardless of dialog mode
    }
}
