package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

// Free entry numeric field used for the ANY and RANGE constraints (and the seed offset, which
// has no constraint at all). Restricts keystrokes to digits (plus a leading '-' and, for
// doubles, a single decimal point). Out of range values are set to the closest bound.
public class NumberFieldEditor implements ArgumentValueEditor {
    private final boolean integer;
    private final Double min;
    private final Double max;
    private final JTextField field;

    public NumberFieldEditor(boolean integer, Double min, Double max) {
        this.integer = integer;
        this.min = min;
        this.max = max;
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
        if (value == null) {
            field.setText("");
        } else {
            field.setText(formatFieldText(value));
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
            double numeric = Double.parseDouble(text);
            Double exceededBound = exceededBound(numeric);
            parsed = boundToNumber(exceededBound != null ? exceededBound : numeric);
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

    // Caps the fields current text to min/max on lost focus
    private void capFieldTextToBounds() {
        String text = field.getText().trim();
        if (text.isEmpty() || text.equals("-")) {
            return;
        }
        double numeric;
        try {
            numeric = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return;
        }
        Double exceededBound = exceededBound(numeric);
        if (exceededBound != null) {
            field.setText(formatFieldText(boundToNumber(exceededBound)));
        }
    }

    private String formatFieldText(Object value) {
        if (integer && value instanceof Number) {
            return String.valueOf(((Number) value).longValue());
        }
        if (value instanceof Number) {
            double numeric = ((Number) value).doubleValue();
            if (Double.isNaN(numeric) || Double.isInfinite(numeric)) {
                return String.valueOf(numeric);
            }
            return BigDecimal.valueOf(numeric).stripTrailingZeros().toPlainString();
        }
        return String.valueOf(value);
    }

    // Returns the bound the passed number falls outside of (min or max), or null if it's within
    // range
    private Double exceededBound(double number) {
        if (Double.isNaN(number)) {
            return min != null ? min : max;
        }
        if (Double.isInfinite(number)) {
            if (number > 0) {
                return max;
            }
            return min;
        }
        if (min != null && number < min) {
            return min;
        }
        if (max != null && number > max) {
            return max;
        }
        return null;
    }

    private Number boundToNumber(double value) {
        if (integer) {
            return Integer.valueOf((int) value);
        }
        return value;
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
