package redactedrice.ptcgr.randomizer.gui.moduleconfig.editor;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

final class NumericDocumentFilter extends DocumentFilter {
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
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
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
