package redactedrice.ptcgr.randomizer.gui.moduleconfig.layout;

import java.awt.FontMetrics;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.structured.StructuredGridModel;
import redactedrice.randomizer.lua.arguments.TypeDefinition;

// Minimum widths and natural content measurement for module config columns.
public final class ColumnSizing {
    public static final int ENTRY_BOX_WIDTH = 80;
    public static final int VIEW_MODE_MIN_WIDTH = 160;
    public static final int REMOVE_BUTTON_WIDTH = 60;
    public static final int TABLE_ARROW_WIDTH = 40;

    private static final int TEXT_WIDTH_PADDING = 4;
    private static final int COMBO_ARROW_PADDING = 20;

    private ColumnSizing() {}

    public static int minimumValueWidth(TypeDefinition valueType) {
        return minimumValueWidth(valueType, true);
    }

    public static int minimumValueWidth(TypeDefinition valueType, boolean editable) {
        if (!editable) {
            return VIEW_MODE_MIN_WIDTH;
        }
        if (valueType == null || (!valueType.isList() && !valueType.isTable())) {
            return ENTRY_BOX_WIDTH;
        }
        StructuredGridModel.LayoutControlCounts counts =
                StructuredGridModel.layoutControlCounts(valueType);
        return counts.entryBoxes() * ENTRY_BOX_WIDTH + counts.removeButtons() * REMOVE_BUTTON_WIDTH
                + counts.tableLevels() * TABLE_ARROW_WIDTH;
    }

    public static int measureContent(JComponent content) {
        if (content instanceof WrappingLabel wrappingLabel) {
            return wrappingLabel.getUnwrappedWidth();
        }
        if (content instanceof JComboBox<?> comboBox) {
            return measureComboBox(comboBox);
        }
        if (content instanceof JCheckBox) {
            return REMOVE_BUTTON_WIDTH;
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
        return Math.max(measured, ENTRY_BOX_WIDTH);
    }
}
