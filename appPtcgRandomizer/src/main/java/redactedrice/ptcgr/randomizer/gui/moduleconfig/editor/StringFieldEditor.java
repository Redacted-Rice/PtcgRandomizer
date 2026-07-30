package redactedrice.ptcgr.randomizer.gui.moduleconfig.editor;

import javax.swing.JComponent;
import javax.swing.JTextField;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.ArgumentValueEditor;

// Free entry text field used for STRING arguments with the ANY constraint. Unlike
// NumberFieldEditor there's no natural bound to enforce or keystroke filter to apply - any text
// is a valid string.
public class StringFieldEditor implements ArgumentValueEditor {
    private final JTextField field;

    public StringFieldEditor() {
        field = new JTextField();
    }

    @Override
    public JComponent getComponent() {
        return field;
    }

    @Override
    public void setValue(Object value) {
        field.setText(value == null ? "" : String.valueOf(value));
    }

    @Override
    public Object getValue() {
        return field.getText();
    }

    @Override
    public void setEditable(boolean editable) {
        field.setEditable(editable);
    }
}
