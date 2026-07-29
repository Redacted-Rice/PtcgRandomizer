package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.awt.FontMetrics;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

// Measures natural content width for column width calculations. Uses component specific logic
// so opening column widths fit labels, combo boxes, and text fields without clipping.
final class ColumnContentWidths {
    private static final int TEXT_WIDTH_PADDING = 4;
    private static final int COMBO_ARROW_PADDING = 20;

    private ColumnContentWidths() {}

    static int measure(JComponent content) {
        if (content instanceof WrappingLabel wrappingLabel) {
            return wrappingLabel.getUnwrappedWidth();
        }
        if (content instanceof JComboBox<?> comboBox) {
            return measureComboBox(comboBox);
        }
        if (content instanceof JCheckBox) {
            return ValueColumnWidths.REMOVE_BUTTON_WIDTH;
        }
        if (content instanceof JTextField textField) {
            return measureTextField(textField);
        }
        return Math.max(0, content.getPreferredSize().width);
    }

    private static int measureComboBox(JComboBox<?> comboBox) {
        FontMetrics metrics = comboBox.getFontMetrics(comboBox.getFont());
        int widestText = 0;
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Object item = comboBox.getItemAt(i);
            if (item != null) {
                widestText = Math.max(widestText, metrics.stringWidth(item.toString()));
            }
        }
        Object selected = comboBox.getSelectedItem();
        if (selected != null) {
            widestText = Math.max(widestText, metrics.stringWidth(selected.toString()));
        }
        Insets insets = comboBox.getInsets();
        return widestText + insets.left + insets.right + COMBO_ARROW_PADDING + TEXT_WIDTH_PADDING;
    }

    private static int measureTextField(JTextField textField) {
        FontMetrics metrics = textField.getFontMetrics(textField.getFont());
        String text = textField.getText();
        Insets insets = textField.getInsets();
        int measured = metrics.stringWidth(text == null ? "" : text) + insets.left + insets.right
                + TEXT_WIDTH_PADDING;
        return Math.max(measured, ValueColumnWidths.ENTRY_BOX_WIDTH);
    }
}
