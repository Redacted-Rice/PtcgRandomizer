package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import javax.swing.JComponent;

// Common contract for widgets that edit a single argument (or the seed offset) value.
// Widgets perform input formatting and basic field checks in getValue(). Module argument
// constraints are enforced when the value is saved via Action.setArgument().
public interface ArgumentValueEditor {
    JComponent getComponent();

    // Sets the value currently displayed by the widget (the instance value when editable,
    // or the module default when read only).
    void setValue(Object value);

    // Returns the current value from the widget, converted to the java type the argument
    // expects (Integer or Double). Throws IllegalArgumentException with a user displayable
    // message if the current input is not valid.
    Object getValue();

    void setEditable(boolean editable);
}
