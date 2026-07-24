package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import redactedrice.randomizer.lua.arguments.TypeDefinition;

// Shared layout constants and helpers for inline LIST/TABLE editors so nested boxes, indents,
// remove buttons, and field wrappers all look and behave the same at every depth.
final class StructuredInlineEditorSupport {
    // Minimum width for fields inside a structured row. The dialog's outer value column uses a
    // wider floor (ModuleConfigDialog.VALUE_FIELD_WIDTH) because it spans the full grid cell.
    static final int FIELD_MIN_WIDTH = 140;
    static final int ROW_HGAP = 6;
    static final int ROW_VGAP = 2;
    static final int LIST_NEST_INDENT_PX = 18;

    private static final Color NESTED_BOX_BORDER_COLOR = new Color(180, 180, 180);
    private static final int NESTED_BOX_PADDING_PX = 4;
    private static final int REMOVE_BUTTON_APPROX_WIDTH = 28;

    private StructuredInlineEditorSupport() {}

    // Draws a border around nested structured content. Extra left indent applies only for
    // list of list levels - nested tables pass 0.
    static JComponent boxNestedListContent(JComponent content, int listIndentDepth) {
        JPanel boxed = new JPanel(new BorderLayout());
        boxed.setOpaque(false);
        boxed.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, listIndentDepth * LIST_NEST_INDENT_PX, 0, 0),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(NESTED_BOX_BORDER_COLOR),
                        BorderFactory.createEmptyBorder(NESTED_BOX_PADDING_PX,
                                NESTED_BOX_PADDING_PX, NESTED_BOX_PADDING_PX,
                                NESTED_BOX_PADDING_PX))));
        boxed.add(content, BorderLayout.CENTER);
        return boxed;
    }

    static int structuredContentMinWidth() {
        return FIELD_MIN_WIDTH + ROW_HGAP + REMOVE_BUTTON_APPROX_WIDTH;
    }

    static void applyStructuredContentMinWidth(JComponent content) {
        Dimension current = content.getMinimumSize();
        int minWidth = Math.max(current.width, structuredContentMinWidth());
        content.setMinimumSize(new Dimension(minWidth, current.height));
    }

    static JComponent wrapCompactField(JComponent editorComponent) {
        return new MinWidthPanel(editorComponent, FIELD_MIN_WIDTH);
    }

    static JComponent wrapExpandableField(JComponent editorComponent) {
        return new MinWidthPanel(editorComponent, FIELD_MIN_WIDTH, true);
    }

    static JComponent prepareValueField(JComponent editorComponent, TypeDefinition valueType) {
        if (valueType.isList() || valueType.isTable()) {
            applyStructuredContentMinWidth(editorComponent);
            return editorComponent;
        }
        return wrapExpandableField(editorComponent);
    }

    static JLabel createArrowLabel() {
        return new JLabel(StructuredTypeText.ARROW);
    }

    static JButton createRemoveButton(boolean visible) {
        JButton removeButton = new JButton("\u00D7");
        removeButton.setToolTipText("Remove");
        removeButton.setFocusable(false);
        removeButton.setMargin(new Insets(0, 6, 0, 6));
        removeButton.setVisible(visible);
        return constrainToPreferredSize(removeButton);
    }

    static JPanel buildTableEntryRow(JComponent keyEditor, JComponent valueEditor,
            TypeDefinition valueType, JButton removeButton) {
        JPanel rowPanel = new JPanel(new GridBagLayout());
        rowPanel.setOpaque(false);

        boolean structuredValue = valueType.isList() || valueType.isTable();
        int compactAnchor = structuredValue ? GridBagConstraints.WEST : GridBagConstraints.BASELINE;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(1, 0, 1, 0);
        gbc.anchor = compactAnchor;
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0;
        rowPanel.add(wrapCompactField(keyEditor), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(1, ROW_HGAP, 1, ROW_HGAP);
        rowPanel.add(createArrowLabel(), gbc);

        gbc.gridx = 2;
        gbc.weightx = 1;
        gbc.anchor = structuredValue ? GridBagConstraints.NORTHWEST : GridBagConstraints.BASELINE;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(1, 0, 1, ROW_HGAP);
        rowPanel.add(prepareValueField(valueEditor, valueType), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(1, 0, 1, 0);
        rowPanel.add(removeButton, gbc);
        return rowPanel;
    }

    static JPanel buildListEntryRow(JComponent elementEditor, TypeDefinition elementType,
            JButton removeButton) {
        JPanel rowPanel = new JPanel(new GridBagLayout());
        rowPanel.setOpaque(false);

        boolean structuredElement = elementType.isList() || elementType.isTable();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(1, 0, 1, 0);

        JComponent valueComponent = prepareValueField(elementEditor, elementType);
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.anchor = structuredElement ? GridBagConstraints.NORTHWEST : GridBagConstraints.BASELINE;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        rowPanel.add(valueComponent, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(1, ROW_HGAP, 1, 0);
        rowPanel.add(removeButton, gbc);
        return rowPanel;
    }

    private static <T extends JComponent> T constrainToPreferredSize(T component) {
        Dimension preferred = component.getPreferredSize();
        component.setMinimumSize(preferred);
        component.setMaximumSize(preferred);
        return component;
    }
}
