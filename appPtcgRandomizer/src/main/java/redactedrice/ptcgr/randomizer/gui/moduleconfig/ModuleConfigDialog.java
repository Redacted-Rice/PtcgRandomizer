package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;

import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.randomizer.lua.Module;
import redactedrice.randomizer.lua.arguments.ArgumentDefinition;
import redactedrice.randomizer.lua.arguments.TypeDefinition;

// Shows the seed offset (for seeded modules) and every module argument as an aligned list of
// rows. All rows share a single GridBagLayout so columns stay lined up regardless of how many
// rows there are. Opens with a fixed max size and scrolls if there are more rows than fit.
public class ModuleConfigDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private static final int MIN_WIDTH = 520;
    private static final int MAX_WIDTH = 940;
    private static final int MIN_HEIGHT = 320;
    private static final int MAX_HEIGHT = 680;

    static final int ARGUMENT_COLUMN = 0;
    private static final int SEPARATOR_AFTER_ARGUMENT = 1;
    static final int CONSTRAINTS_COLUMN = 2;
    private static final int SEPARATOR_AFTER_CONSTRAINTS = 3;
    static final int VALUE_COLUMN = 4;
    private static final int COLUMN_COUNT = 5;

    private static final int ARGUMENT_COLUMN_MIN_WIDTH = 80;
    private static final int ARGUMENT_COLUMN_MAX_WIDTH = 150;
    private static final int CONSTRAINTS_COLUMN_MIN_WIDTH = 80;
    private static final int CONSTRAINTS_COLUMN_MAX_WIDTH = 200;
    private static final int VALUE_COLUMN_MIN_WIDTH = 80;
    private static final double COLUMN_GROW_WEIGHT = 1;

    private static final int DIALOG_PADDING = 10;
    // Tighter gap between the table and buttons
    private static final int CONTENT_BUTTON_GAP = 4;

    private static final Color LINE_COLOR = new Color(215, 215, 215);
    private static final int CELL_PADDING_H = 8;
    private static final int CELL_PADDING_V = 4;
    private static final int LINE_WIDTH = 1;

    private static final ModuleConfigColumnWidths.ColumnSpec[] COLUMN_SPECS = {
            ModuleConfigColumnWidths.ColumnSpec.bounded(ARGUMENT_COLUMN_MIN_WIDTH,
                    ARGUMENT_COLUMN_MAX_WIDTH, COLUMN_GROW_WEIGHT),
            ModuleConfigColumnWidths.ColumnSpec.bounded(CONSTRAINTS_COLUMN_MIN_WIDTH,
                    CONSTRAINTS_COLUMN_MAX_WIDTH, COLUMN_GROW_WEIGHT),
            ModuleConfigColumnWidths.ColumnSpec.minOnly(VALUE_COLUMN_MIN_WIDTH,
                    COLUMN_GROW_WEIGHT),};
    private static final int COLUMN_HORIZONTAL_CHROME =
            ModuleConfigColumnWidths.horizontalChrome(3, 2, CELL_PADDING_H, LINE_WIDTH);

    private final Action action;
    private final boolean editable;
    private final EnumValuesProvider enumValuesProvider;
    private ArgumentValueEditor seedOffsetEditor;
    private final Map<String, ArgumentValueEditor> argumentEditors = new LinkedHashMap<>();
    private JScrollPane rowsScrollPane;

    public ModuleConfigDialog(Window owner, Action action, boolean editable,
            EnumValuesProvider enumValuesProvider) {
        super(owner, editable ? "Edit Configs" : "Show Configs", ModalityType.APPLICATION_MODAL);
        this.action = action;
        this.editable = editable;
        this.enumValuesProvider = enumValuesProvider;

        setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(DIALOG_PADDING, DIALOG_PADDING,
                DIALOG_PADDING, DIALOG_PADDING));
        rowsScrollPane = buildRowsPanel();
        contentPanel.add(rowsScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = buildButtonPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(CONTENT_BUTTON_GAP, 0, 0, 0));
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(contentPanel, BorderLayout.CENTER);

        pack();
        applySizeConstraints();
        setLocationRelativeTo(owner);
    }

    private JScrollPane buildRowsPanel() {
        Module module = action.getModule();

        JPanel grid = new ModuleConfigGridPanel(COLUMN_SPECS, COLUMN_HORIZONTAL_CHROME);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(CELL_PADDING_V, CELL_PADDING_H, CELL_PADDING_V, CELL_PADDING_H);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        addHeaderRow(grid, gbc, row);
        row++;

        addHorizontalLine(grid, gbc, row);
        row++;

        int dataStartRow = row;
        boolean hasDataRow = false;

        if (module.isSeeded()) {
            if (hasDataRow) {
                addHorizontalLine(grid, gbc, row);
                row++;
            }
            JComponent valueComponent;
            if (editable) {
                seedOffsetEditor = ArgumentEditorFactory.forSeedOffset();
                seedOffsetEditor.setValue(action.getSeedOffset());
                valueComponent = seedOffsetEditor.getComponent();
            } else {
                valueComponent = readOnlyValueLabel(action.getSeedOffset());
            }
            row = addRow(grid, gbc, row, "Seed Offset", ArgumentEditorFactory.describeSeedOffset(),
                    valueComponent);
            hasDataRow = true;
        }

        for (ArgumentDefinition argDef : module.getArguments()) {
            if (hasDataRow) {
                addHorizontalLine(grid, gbc, row);
                row++;
            }
            String name = argDef.getName();
            JComponent valueComponent;
            if (editable) {
                ArgumentValueEditor editor =
                        ArgumentEditorFactory.create(argDef, enumValuesProvider);
                editor.setValue(action.getArgument(name));
                argumentEditors.put(name, editor);
                valueComponent = editor.getComponent();
            } else {
                valueComponent =
                        readOnlyValueLabel(argDef.getTypeDefinition(), action.getArgument(name));
            }
            row = addRow(grid, gbc, row, name, ArgumentEditorFactory.describeConstraint(argDef),
                    valueComponent);
            hasDataRow = true;
        }

        int dataSectionSpan = row - dataStartRow;
        if (dataSectionSpan > 0) {
            addColumnSeparator(grid, gbc, dataStartRow, SEPARATOR_AFTER_ARGUMENT, dataSectionSpan);
            addColumnSeparator(grid, gbc, dataStartRow, SEPARATOR_AFTER_CONSTRAINTS,
                    dataSectionSpan);
        }

        addHorizontalLine(grid, gbc, row);

        JPanel viewPanel = new JPanel(new BorderLayout());
        viewPanel.add(grid, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(viewPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(grid.getPreferredSize());
        return scrollPane;
    }

    private void addHeaderRow(JPanel grid, GridBagConstraints gbc, int row) {
        addCell(grid, gbc, row, ARGUMENT_COLUMN, headerLabel("Argument"), GridBagConstraints.WEST,
                true);
        addCell(grid, gbc, row, CONSTRAINTS_COLUMN, headerLabel("Constraints"),
                GridBagConstraints.WEST, true);
        addCell(grid, gbc, row, VALUE_COLUMN, headerLabel("Value"), GridBagConstraints.WEST, true);
    }

    private static JLabel headerLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    private void addCell(JPanel grid, GridBagConstraints gbc, int row, int column,
            JComponent content, int anchor, boolean fillHorizontal) {
        JComponent cell = wrapForColumn(grid, column, content);
        gbc.gridx = column;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = anchor;
        gbc.weightx = isDataColumn(column) ? COLUMN_GROW_WEIGHT : 0;
        gbc.weighty = 0;
        gbc.fill = fillHorizontal ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        gbc.insets = new Insets(CELL_PADDING_V, CELL_PADDING_H, CELL_PADDING_V, CELL_PADDING_H);
        grid.add(cell, gbc);
    }

    private static boolean isDataColumn(int column) {
        return column == ARGUMENT_COLUMN || column == CONSTRAINTS_COLUMN || column == VALUE_COLUMN;
    }

    private static JComponent wrapForColumn(JPanel grid, int column, JComponent content) {
        if (!isDataColumn(column)) {
            return content;
        }
        ColumnWidthPanel panel = switch (column) {
            case ARGUMENT_COLUMN -> new ColumnWidthPanel(content, ARGUMENT_COLUMN_MIN_WIDTH,
                    ARGUMENT_COLUMN_MAX_WIDTH, true);
            case CONSTRAINTS_COLUMN -> new ColumnWidthPanel(content, CONSTRAINTS_COLUMN_MIN_WIDTH,
                    CONSTRAINTS_COLUMN_MAX_WIDTH, true);
            case VALUE_COLUMN -> new ColumnWidthPanel(content, VALUE_COLUMN_MIN_WIDTH,
                    Integer.MAX_VALUE, true);
            default -> throw new IllegalArgumentException("Not a data column: " + column);
        };
        if (grid instanceof ModuleConfigGridPanel configGrid) {
            configGrid.registerColumnPanel(column, panel);
        }
        return panel;
    }

    private int addRow(JPanel grid, GridBagConstraints gbc, int row, String name,
            String constraintDescription, JComponent valueComponent) {
        addCell(grid, gbc, row, ARGUMENT_COLUMN, new JLabel(name), GridBagConstraints.WEST, true);

        JLabel constraintLabel = new JLabel(constraintDescription);
        constraintLabel.setForeground(Color.DARK_GRAY);
        addCell(grid, gbc, row, CONSTRAINTS_COLUMN, constraintLabel, GridBagConstraints.WEST, true);

        addCell(grid, gbc, row, VALUE_COLUMN, valueComponent, GridBagConstraints.WEST, true);

        return row + 1;
    }

    // Solid vertical rules spanning the full data section between the top and bottom horizontal
    // lines
    private void addColumnSeparator(JPanel grid, GridBagConstraints gbc, int startRow, int column,
            int rowSpan) {
        gbc.gridx = column;
        gbc.gridy = startRow;
        gbc.gridwidth = 1;
        gbc.gridheight = rowSpan;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        grid.add(createVerticalLine(), gbc);
        gbc.gridheight = 1;
    }

    private void addHorizontalLine(JPanel grid, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = COLUMN_COUNT;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        grid.add(createHorizontalLine(), gbc);
        gbc.gridwidth = 1;
    }

    private static JComponent createVerticalLine() {
        JPanel line = new JPanel();
        line.setBackground(LINE_COLOR);
        line.setOpaque(true);
        line.setPreferredSize(new Dimension(LINE_WIDTH, 0));
        line.setMinimumSize(new Dimension(LINE_WIDTH, 0));
        line.setMaximumSize(new Dimension(LINE_WIDTH, Integer.MAX_VALUE));
        return line;
    }

    private static JComponent createHorizontalLine() {
        JPanel line = new JPanel();
        line.setBackground(LINE_COLOR);
        line.setOpaque(true);
        line.setPreferredSize(new Dimension(0, LINE_WIDTH));
        line.setMinimumSize(new Dimension(0, LINE_WIDTH));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, LINE_WIDTH));
        return line;
    }

    // Read only rows show the value as a plain label rather than a disabled input widget, so
    // it's visually clear that it can't be edited here
    private static JLabel readOnlyValueLabel(Object value) {
        return new JLabel(value == null ? "" : String.valueOf(value));
    }

    // LIST/TABLE values get compact preview text, e.g. "common, uncommon" or
    // "fire → 10, water → (1, 2, 3)" for nested complex values.
    private static JLabel readOnlyValueLabel(TypeDefinition typeDef, Object value) {
        if (typeDef.isList() || typeDef.isTable()) {
            return new JLabel(StructuredValueFormatting.format(typeDef, value));
        }
        return readOnlyValueLabel(value);
    }

    private JPanel buildButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        if (editable) {
            JButton okButton = new JButton("OK");
            okButton.addActionListener(e -> saveAndClose());
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> dispose());
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
        } else {
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dispose());
            buttonPanel.add(closeButton);
        }
        return buttonPanel;
    }

    private void applySizeConstraints() {
        Dimension preferred = getPreferredSize();
        int width = clamp(preferred.width, MIN_WIDTH, MAX_WIDTH);

        if (preferred.height > MAX_HEIGHT) {
            int chromeHeight = preferred.height - rowsScrollPane.getPreferredSize().height;
            int scrollHeight = MAX_HEIGHT - chromeHeight;
            rowsScrollPane.setPreferredSize(new Dimension(rowsScrollPane.getPreferredSize().width,
                    Math.max(MIN_HEIGHT / 2, scrollHeight)));
            pack();
            preferred = getPreferredSize();
        }

        int height = clamp(preferred.height, MIN_HEIGHT, MAX_HEIGHT);
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setSize(new Dimension(width, height));
        validate();

        Dimension slack = scrollBarSlack();
        setSize(new Dimension(clamp(width + slack.width, MIN_WIDTH, MAX_WIDTH + slack.width),
                clamp(height + slack.height, MIN_HEIGHT, MAX_HEIGHT + slack.height)));
    }

    // Extra dialog size so AS_NEEDED scroll bars are fully visible (one pass, no loop).
    private Dimension scrollBarSlack() {
        JViewport viewport = rowsScrollPane.getViewport();
        Component view = viewport.getView();
        if (view == null) {
            return new Dimension(0, 0);
        }

        Dimension viewSize = view.getPreferredSize();
        Dimension extent = viewport.getExtentSize();
        if (extent.width <= 0 || extent.height <= 0) {
            return new Dimension(0, 0);
        }

        JScrollBar verticalBar = rowsScrollPane.getVerticalScrollBar();
        JScrollBar horizontalBar = rowsScrollPane.getHorizontalScrollBar();
        int verticalBarWidth = verticalBar.getPreferredSize().width;
        int horizontalBarHeight = horizontalBar.getPreferredSize().height;

        int extraWidth = 0;
        int extraHeight = 0;
        if (viewSize.height > extent.height) {
            extraWidth += verticalBarWidth;
        }
        // Vertical bar steals viewport width — may require a horizontal bar too.
        if (viewSize.width > extent.width - extraWidth) {
            extraHeight += horizontalBarHeight;
        }
        return new Dimension(extraWidth, extraHeight);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private void saveAndClose() {
        Integer validatedSeedOffset = null;
        Map<String, Object> validatedArguments = new LinkedHashMap<>();
        try {
            if (seedOffsetEditor != null) {
                validatedSeedOffset = (Integer) seedOffsetEditor.getValue();
            }
            for (Map.Entry<String, ArgumentValueEditor> entry : argumentEditors.entrySet()) {
                validatedArguments.put(entry.getKey(), entry.getValue().getValue());
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (validatedSeedOffset != null) {
            action.setSeedOffset(validatedSeedOffset);
        }
        for (Map.Entry<String, Object> entry : validatedArguments.entrySet()) {
            action.setArgument(entry.getKey(), entry.getValue());
        }
        dispose();
    }

    public static void show(Window owner, Action action, boolean editable,
            EnumValuesProvider enumValuesProvider) {
        ModuleConfigDialog dialog =
                new ModuleConfigDialog(owner, action, editable, enumValuesProvider);
        dialog.setVisible(true);
    }
}
