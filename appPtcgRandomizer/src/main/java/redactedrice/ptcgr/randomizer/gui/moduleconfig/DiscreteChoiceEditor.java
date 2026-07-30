package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;

// Drop down editor for numeric constraints with a fixed, prepopulated set of choices:
// DISCRETE_RANGE (min/max/step) and ENUM (explicit allowed numeric values). Both cases need to
// normalize choices to a consistent int or double representation (see
// NumericChoiceMatching.normalize()), which is
// specific to numeric types - for a fixed set of non-numeric choices (string/boolean enums, or
// the ENUM base type) see EnumEditor instead.
public class DiscreteChoiceEditor implements ArgumentValueEditor {
    static final int MAX_DROPDOWN_CHOICES = 20;

    private final boolean integer;
    private final List<Number> choices;
    private final JComboBox<Number> comboBox;

    private DiscreteChoiceEditor(boolean integer, List<Number> choices) {
        this.integer = integer;
        this.choices = List.copyOf(choices);
        this.comboBox = new JComboBox<>(this.choices.toArray(new Number[0]));
        comboBox.setPrototypeDisplayValue(widestChoice(this.choices));
    }

    private static Number widestChoice(List<Number> choices) {
        if (choices.isEmpty()) {
            return 0;
        }
        return choices.stream().max(Comparator.comparing(n -> n.toString().length()))
                .orElse(choices.get(0));
    }

    static boolean prefersDropdownForDiscreteRange(double min, double max, double step) {
        return discreteRangeStepCount(min, max, step) <= MAX_DROPDOWN_CHOICES;
    }

    static int discreteRangeStepCount(double min, double max, double step) {
        if (step <= 0) {
            return 1;
        }
        BigDecimal current = BigDecimal.valueOf(min);
        BigDecimal maxBd = BigDecimal.valueOf(max);
        BigDecimal stepBd = BigDecimal.valueOf(step);
        int count = 0;
        while (current.compareTo(maxBd) <= 0) {
            count++;
            current = current.add(stepBd);
        }
        return count;
    }

    public static DiscreteChoiceEditor forDiscreteRange(boolean integer, double min, double max,
            double step) {
        return new DiscreteChoiceEditor(integer, buildRangeChoices(integer, min, max, step));
    }

    public static DiscreteChoiceEditor forEnumValues(boolean integer, List<Object> allowedValues) {
        List<Number> choices = new ArrayList<>();
        for (Object value : allowedValues) {
            if (value instanceof Number) {
                choices.add(NumericChoiceMatching.normalize(integer, (Number) value));
            }
        }
        return new DiscreteChoiceEditor(integer, choices);
    }

    private static List<Number> buildRangeChoices(boolean integer, double min, double max,
            double step) {
        List<Number> choices = new ArrayList<>();
        if (step <= 0) {
            // Defensive fallback for a misconfigured module
            choices.add(NumericChoiceMatching.normalize(integer, min));
            return choices;
        }
        BigDecimal current = BigDecimal.valueOf(min);
        BigDecimal maxBd = BigDecimal.valueOf(max);
        BigDecimal stepBd = BigDecimal.valueOf(step);
        while (current.compareTo(maxBd) <= 0) {
            choices.add(NumericChoiceMatching.normalize(integer, current));
            current = current.add(stepBd);
        }
        return choices;
    }

    @Override
    public JComponent getComponent() {
        return comboBox;
    }

    @Override
    public void setValue(Object value) {
        if (!(value instanceof Number)) {
            return;
        }
        Number nearest = NumericChoiceMatching.nearestChoice((Number) value, integer, choices);
        comboBox.setSelectedItem(nearest);
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
