package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

// Free entry numeric field used for the ANY and RANGE constraints (and the seed offset, which
// has no constraint at all). Restricts keystrokes to digits (plus a leading '-' and, for
// doubles, a single decimal point). Out of range values are set to the closest bound and the
// user is warned when their entry is adjusted.
public class NumberFieldEditor implements ArgumentValueEditor {
    private final boolean integer;
    private final Double min;
    private final Double max;
    private final Double step;
    private final JTextField field;
    // If we set it in code, we want to supress user pop ups
    private int suppressAdjustmentWarnings;

    public NumberFieldEditor(boolean integer, Double min, Double max) {
        this(integer, min, max, null);
    }

    public NumberFieldEditor(boolean integer, Double min, Double max, Double step) {
        this.integer = integer;
        this.min = min;
        this.max = max;
        this.step = step;
        this.field = new JTextField();
        ((AbstractDocument) field.getDocument())
                .setDocumentFilter(new NumericDocumentFilter(integer));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                capFieldTextToBounds();
            }
        });
    }

    @Override
    public JComponent getComponent() {
        return field;
    }

    @Override
    public void setValue(Object value) {
        suppressAdjustmentWarnings++;
        try {
            // Still normalize on load, just without warning the user about preset values
            if (value == null) {
                field.setText("");
                return;
            }
            if (value instanceof Number number) {
                double numeric = NumericChoiceMatching.applyBoundsAndStep(number.doubleValue(), min,
                        max, step);
                field.setText(formatFieldText(NumericDisplay.toTypedNumber(numeric, integer)));
                return;
            }
            field.setText(formatFieldText(value));
        } finally {
            suppressAdjustmentWarnings--;
        }
    }

    @Override
    public Object getValue() {
        String text = field.getText().trim();
        if (text.isEmpty() || text.equals("-")) {
            throw new IllegalArgumentException(
                    integer ? "Value must be a whole number." : "Value must be a number.");
        }

        // Parse as double first so values outside Integer.parseInt range can still be capped
        // to the types min/max
        Number parsed;
        try {
            parsed = normalizeFieldText(text, false);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    integer ? "Value must be a whole number." : "Value must be a number.");
        }

        return parsed;
    }

    @Override
    public void setEditable(boolean editable) {
        field.setEditable(editable);
    }

    // Caps and snaps the fields current text on lost focus, warning if the user typed off range
    private void capFieldTextToBounds() {
        String text = field.getText().trim();
        if (text.isEmpty() || text.equals("-")) {
            return;
        }
        try {
            normalizeFieldText(text, true);
        } catch (NumberFormatException e) {
            return;
        }
    }

    // Parses the fields text, clamps/snaps it, and optionally rewrites the field when adjusted
    private Number normalizeFieldText(String text, boolean updateFieldOnAdjust)
            throws NumberFormatException {
        double entered = Double.parseDouble(text);
        double adjusted = NumericChoiceMatching.applyBoundsAndStep(entered, min, max, step);
        if (Double.compare(adjusted, entered) != 0) {
            warnIfUserValueAdjusted(entered, adjusted);
            if (updateFieldOnAdjust) {
                field.setText(formatFieldText(NumericDisplay.toTypedNumber(adjusted, integer)));
            }
        }
        return NumericDisplay.toTypedNumber(adjusted, integer);
    }

    // User typed edits only - preset load goes through setValue with warnings suppressed
    private void warnIfUserValueAdjusted(double entered, double adjusted) {
        if (suppressAdjustmentWarnings > 0) {
            return;
        }
        ValueAdjustmentWarnings.showNumericAdjustment(field, entered, adjusted, integer, min, max,
                step);
    }

    private String formatFieldText(Object value) {
        if (value instanceof Number number) {
            return NumericDisplay.format(number.doubleValue(), integer);
        }
        return String.valueOf(value);
    }

    private static class NumericDocumentFilter extends DocumentFilter {
        private final boolean integer;

        NumericDocumentFilter(boolean integer) {
            this.integer = integer;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (isValidInput(fb, offset, string, offset, 0)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text,
                AttributeSet attrs) throws BadLocationException {
            if (isValidInput(fb, offset, text, offset, length)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        private boolean isValidInput(FilterBypass fb, int offset, String newText, int replaceOffset,
                int replaceLength) throws BadLocationException {
            if (newText == null) {
                return true;
            }
            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String updated = current.substring(0, replaceOffset) + newText
                    + current.substring(replaceOffset + replaceLength);
            if (updated.isEmpty() || updated.equals("-")) {
                return true;
            }
            String pattern = integer ? "-?\\d*" : "-?\\d*(\\.\\d*)?";
            return updated.matches(pattern);
        }
    }
}
