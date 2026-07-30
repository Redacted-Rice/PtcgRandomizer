package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.util.Comparator;
import java.util.List;

import java.util.Objects;

import javax.swing.JComboBox;
import javax.swing.JComponent;

// Drop down editor for a fixed set of non-numeric choices: STRING/BOOLEAN ENUM constraints and
// the ENUM base type (values pulled from a registered enum by name, e.g. via
// context.registerEnum in a module's onLoad, or a Java defined enum). Every choice is used
// exactly as given - no normalization needed like DiscreteChoiceEditor does for numeric types.
public class EnumEditor implements ArgumentValueEditor {
    private final JComboBox<Object> comboBox;

    public EnumEditor(List<?> choices) {
        this.comboBox = new JComboBox<>(choices.toArray());
        comboBox.setPrototypeDisplayValue(widestChoice(choices));
    }

    private static String widestChoice(List<?> choices) {
        return choices.stream().map(String::valueOf).max(Comparator.comparingInt(String::length))
                .orElse("");
    }

    @Override
    public JComponent getComponent() {
        return comboBox;
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            return;
        }
        comboBox.setSelectedItem(value);
        if (!Objects.equals(value, comboBox.getSelectedItem())) {
            comboBox.insertItemAt(value, 0);
            comboBox.setSelectedItem(value);
        }
    }

    @Override
    public Object getValue() {
        Object selected = comboBox.getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("A value must be selected.");
        }
        return selected;
    }

    @Override
    public void setEditable(boolean editable) {
        comboBox.setEnabled(editable);
    }
}
