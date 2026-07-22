package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import javax.swing.JComponent;
import javax.swing.JTextField;

// Placeholder editor for argument types/constraints that don't have dedicated UI support yet
// (e.g. STRING, BOOLEAN, LIST, MAP, GROUP, or the ENUM base type). Displays the current value
// read only so the dialog can still render every argument without crashing, and support for
// the type can be added later just by adding a case in ArgumentEditorFactory.
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
