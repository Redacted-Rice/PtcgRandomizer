package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.InvalidInputDialogs;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.ColumnSizing;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.ColumnWidthPanel;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.GridSeparators;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.ModuleConfigColumnWidths;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.ModuleConfigColumnWidths.ColumnSpec;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.ModuleConfigGridPanel;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.WrappingLabel;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.structured.StructuredText;
import redactedrice.randomizer.lua.Module;
import redactedrice.randomizer.lua.arguments.ArgumentDefinition;
import redactedrice.randomizer.lua.arguments.TypeDefinition;

// Shows the seed offset (for seeded modules) and every module argument as an aligned list of
// rows. All rows share a single GridBagLayout so columns stay lined up regardless of how many
// rows there are. Opens with a fixed max size and scrolls if there are more rows than fit.
public class ModuleConfigDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private static final int MAX_WIDTH = 940;
    private static final int MAX_HEIGHT = 680;

    public static final int ARGUMENT_COLUMN = 0;
    private static final int SEPARATOR_AFTER_ARGUMENT = 1;
    public static final int TYPE_COLUMN = 2;
    private static final int SEPARATOR_AFTER_TYPE = 3;
    public static final int CONSTRAINTS_COLUMN = 4;
    private static final int SEPARATOR_AFTER_CONSTRAINTS = 5;
    public static final int VALUE_COLUMN = 6;
    private static final int COLUMN_COUNT = 7;

    private static final int ARGUMENT_COLUMN_MIN_WIDTH = 90;
    private static final int ARGUMENT_COLUMN_MAX_WIDTH = 150;
    private static final int TYPE_COLUMN_MIN_WIDTH = 60;
    private static final int TYPE_COLUMN_MAX_WIDTH = 100;
    private static final int CONSTRAINTS_COLUMN_MIN_WIDTH = 60;
    private static final int CONSTRAINTS_COLUMN_MAX_WIDTH = 100;
    private static final int VALUE_COLUMN_MIN_WIDTH = ColumnSizing.ENTRY_BOX_WIDTH;
    private static final double COLUMN_GROW_WEIGHT = 1;

    private static final int DIALOG_PADDING = 10;
    // Tighter gap between the table and buttons
    private static final int CONTENT_BUTTON_GAP = 4;

    private static final Color LINE_COLOR = new Color(215, 215, 215);
    private static final int CELL_PADDING_H = 8;
    private static final int CELL_PADDING_V = 4;
    private static final int LINE_WIDTH = 1;

    private static final ColumnSpec[] COLUMN_SPECS = {
            ColumnSpec.bounded(ARGUMENT_COLUMN_MIN_WIDTH, ARGUMENT_COLUMN_MAX_WIDTH,
                    COLUMN_GROW_WEIGHT),
            ColumnSpec.bounded(TYPE_COLUMN_MIN_WIDTH, TYPE_COLUMN_MAX_WIDTH, COLUMN_GROW_WEIGHT),
            ColumnSpec.bounded(CONSTRAINTS_COLUMN_MIN_WIDTH, CONSTRAINTS_COLUMN_MAX_WIDTH,
                    COLUMN_GROW_WEIGHT),
            ColumnSpec.minOnly(VALUE_COLUMN_MIN_WIDTH, COLUMN_GROW_WEIGHT),};
    private static final int COLUMN_HORIZONTAL_CHROME =
            ModuleConfigColumnWidths.horizontalChrome(4, 3, CELL_PADDING_H, LINE_WIDTH);

    private final Action action;
    private final boolean editable;
    private final EnumValuesProvider enumValuesProvider;
    private ArgumentValueEditor seedOffsetEditor;
    private final Map<String, ArgumentValueEditor> argumentEditors = new LinkedHashMap<>();
    private final JPanel contentPanel;
    private final JPanel buttonPanel;
    private ModuleConfigGridPanel configGrid;
    private ConfigScrollPane rowsScrollPane;
    private JButton defaultButton;

    public ModuleConfigDialog(Window owner, Action action, boolean editable,
            EnumValuesProvider enumValuesProvider) {
        super(owner, editable ? "Edit Configs" : "Show Configs", ModalityType.APPLICATION_MODAL);
        this.action = action;
        this.editable = editable;
        this.enumValuesProvider = enumValuesProvider;

        setLayout(new BorderLayout());
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(DIALOG_PADDING, DIALOG_PADDING,
                DIALOG_PADDING, DIALOG_PADDING));
        rowsScrollPane = buildRowsPanel();
        contentPanel.add(rowsScrollPane, BorderLayout.CENTER);

        buttonPanel = buildButtonPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(CONTENT_BUTTON_GAP, 0, 0, 0));
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(contentPanel, BorderLayout.CENTER);

        setResizable(true);
        pack();
        applySizeConstraints();
        setLocationRelativeTo(owner);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                resetScrollPosition();
                rowsScrollPane.releaseInitialScrollLock();
                SwingUtilities.invokeLater(() -> {
                    resetScrollPosition();
                    if (defaultButton != null) {
                        defaultButton.requestFocusInWindow();
                    }
                });
            }
        });
    }

    private ConfigScrollPane buildRowsPanel() {
        Module module = action.getModule();

        ModuleConfigGridPanel grid =
                new ModuleConfigGridPanel(COLUMN_SPECS, COLUMN_HORIZONTAL_CHROME);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(CELL_PADDING_V, CELL_PADDING_H, CELL_PADDING_V, CELL_PADDING_H);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        addHeaderRow(grid, gbc, row);
        row++;

        addHorizontalLine(grid, gbc, row);
        row++;

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
            row = addRow(grid, gbc, row, "Seed Offset", "int", "", valueComponent,
                    TypeDefinition.integer());
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
            row = addRow(grid, gbc, row, name,
                    StructuredText.describeStructuredShape(argDef.getTypeDefinition()),
                    ArgumentConstraintDescription.describe(argDef.getTypeDefinition()),
                    valueComponent,
                    argDef.getTypeDefinition());
            hasDataRow = true;
        }

        int bodyRowSpan = row;
        if (bodyRowSpan > 0) {
            addColumnSeparator(grid, gbc, 0, SEPARATOR_AFTER_ARGUMENT, bodyRowSpan);
            addColumnSeparator(grid, gbc, 0, SEPARATOR_AFTER_TYPE, bodyRowSpan);
            addColumnSeparator(grid, gbc, 0, SEPARATOR_AFTER_CONSTRAINTS, bodyRowSpan);
        }

        addHorizontalLine(grid, gbc, row);

        ConfigScrollPane scrollPane = new ConfigScrollPane(grid);
        scrollPane.setBorder(BorderFactory.createLineBorder(LINE_COLOR, LINE_WIDTH));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        configGrid = grid;
        return scrollPane;
    }

    private void addHeaderRow(ModuleConfigGridPanel grid, GridBagConstraints gbc, int row) {
        addCell(grid, gbc, row, ARGUMENT_COLUMN, headerLabel("Argument"), GridBagConstraints.CENTER,
                true);
        addCell(grid, gbc, row, TYPE_COLUMN, headerLabel("Type"), GridBagConstraints.CENTER, true);
        addCell(grid, gbc, row, CONSTRAINTS_COLUMN, headerLabel("Constraint"),
                GridBagConstraints.CENTER, true);
        addCell(grid, gbc, row, VALUE_COLUMN, headerLabel("Value"), GridBagConstraints.CENTER,
                true);
    }

    private static JLabel headerLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    private void addCell(ModuleConfigGridPanel grid, GridBagConstraints gbc, int row, int column,
            JComponent content, int anchor, boolean fillHorizontal) {
        addCell(grid, gbc, row, column, content, anchor, fillHorizontal, null);
    }

    private void addCell(ModuleConfigGridPanel grid, GridBagConstraints gbc, int row, int column,
            JComponent content, int anchor, boolean fillHorizontal, TypeDefinition valueType) {
        JComponent cell = wrapForColumn(grid, column, content, valueType);
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
        return column == ARGUMENT_COLUMN || column == TYPE_COLUMN || column == CONSTRAINTS_COLUMN
                || column == VALUE_COLUMN;
    }

    private JComponent wrapForColumn(ModuleConfigGridPanel grid, int column, JComponent content,
            TypeDefinition valueType) {
        if (!isDataColumn(column)) {
            return content;
        }
        ColumnWidthPanel panel = switch (column) {
            case ARGUMENT_COLUMN -> new ColumnWidthPanel(content, ARGUMENT_COLUMN_MIN_WIDTH,
                    ARGUMENT_COLUMN_MAX_WIDTH, true);
            case TYPE_COLUMN -> new ColumnWidthPanel(content, TYPE_COLUMN_MIN_WIDTH,
                    TYPE_COLUMN_MAX_WIDTH, true);
            case CONSTRAINTS_COLUMN -> new ColumnWidthPanel(content, CONSTRAINTS_COLUMN_MIN_WIDTH,
                    CONSTRAINTS_COLUMN_MAX_WIDTH, true);
            case VALUE_COLUMN -> new ColumnWidthPanel(content,
                    ColumnSizing.minimumValueWidth(valueType, editable), Integer.MAX_VALUE, true);
            default -> throw new IllegalArgumentException("Not a data column: " + column);
        };
        grid.registerColumnPanel(column, panel);
        return panel;
    }

    private int addRow(ModuleConfigGridPanel grid, GridBagConstraints gbc, int row, String name,
            String typeDescription, String constraintDescription, JComponent valueComponent,
            TypeDefinition valueType) {
        addCell(grid, gbc, row, ARGUMENT_COLUMN, new WrappingLabel(name), GridBagConstraints.WEST,
                true);
        addCell(grid, gbc, row, TYPE_COLUMN, new WrappingLabel(typeDescription),
                GridBagConstraints.WEST, true);
        addCell(grid, gbc, row, CONSTRAINTS_COLUMN,
                WrappingLabel.constraints(constraintDescription), GridBagConstraints.WEST, true);

        addCell(grid, gbc, row, VALUE_COLUMN, valueComponent, GridBagConstraints.WEST, true,
                valueType);

        return row + 1;
    }

    // Solid vertical rules spanning the header and data rows between the top and bottom horizontal
    // lines.
    private void addColumnSeparator(ModuleConfigGridPanel grid, GridBagConstraints gbc,
            int startRow, int column, int rowSpan) {
        gbc.gridx = column;
        gbc.gridy = startRow;
        gbc.gridwidth = 1;
        gbc.gridheight = rowSpan;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        grid.add(GridSeparators.verticalLine(), gbc);
        gbc.gridheight = 1;
    }

    private void addHorizontalLine(ModuleConfigGridPanel grid, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = COLUMN_COUNT;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        grid.add(GridSeparators.horizontalLine(), gbc);
        gbc.gridwidth = 1;
    }

    // Read only rows show the value as a plain label rather than a disabled input widget, so
    // it's visually clear that it can't be edited here
    private static WrappingLabel readOnlyValueLabel(Object value) {
        return new WrappingLabel(value == null ? "" : String.valueOf(value));
    }

    // LIST/TABLE values get compact preview text, e.g. "common, uncommon" or
    // "fire → 10, water → (1, 2, 3)" for nested complex values.
    private static WrappingLabel readOnlyValueLabel(TypeDefinition typeDef, Object value) {
        if (typeDef.isList() || typeDef.isTable()) {
            return new WrappingLabel(StructuredText.formatValue(typeDef, value));
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
            defaultButton = okButton;
        } else {
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dispose());
            buttonPanel.add(closeButton);
            defaultButton = closeButton;
        }
        return buttonPanel;
    }

    private void applySizeConstraints() {
        Dimension tablePref = tablePreferredSize();
        rowsScrollPane.setPreferredSize(tablePref);
        pack();

        Dimension preferred = getPreferredSize();
        int width = Math.min(preferred.width, MAX_WIDTH);

        if (preferred.height > MAX_HEIGHT) {
            int chromeHeight = preferred.height - tablePref.height;
            int scrollHeight = MAX_HEIGHT - chromeHeight;
            rowsScrollPane
                    .setPreferredSize(new Dimension(tablePref.width, Math.max(0, scrollHeight)));
            pack();
            preferred = getPreferredSize();
            tablePref = tablePreferredSize();
            resetScrollPosition();
        }

        int height = Math.min(preferred.height, MAX_HEIGHT);

        Dimension slack = scrollBarSlack(tablePref.width, tablePref.height);
        setSize(new Dimension(Math.min(width + slack.width, MAX_WIDTH + slack.width),
                Math.min(height + slack.height, MAX_HEIGHT)));

        resetScrollPosition();
    }

    private Dimension tablePreferredSize() {
        Dimension gridPref = configGrid.getPreferredSize();
        Insets border = rowsScrollPane.getBorder().getBorderInsets(rowsScrollPane);
        return new Dimension(gridPref.width + border.left + border.right,
                gridPref.height + border.top + border.bottom);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            resetScrollPosition();
        }
        super.setVisible(visible);
    }

    private void resetScrollPosition() {
        JViewport viewport = rowsScrollPane.getViewport();
        viewport.setViewPosition(new Point(0, 0));
        rowsScrollPane.getVerticalScrollBar().setValue(0);
        rowsScrollPane.getHorizontalScrollBar().setValue(0);
    }

    // Extra dialog size so AS_NEEDED scroll bars are fully visible (one pass, no loop).
    // Uses planned scroll-pane dimensions — viewport extent is unreliable before/while sizing.
    private Dimension scrollBarSlack(int scrollPaneWidth, int scrollPaneHeight) {
        Component view = rowsScrollPane.getViewport().getView();
        if (view == null || scrollPaneWidth <= 0 || scrollPaneHeight <= 0) {
            return new Dimension(0, 0);
        }

        Dimension viewSize = view.getPreferredSize();
        JScrollBar verticalBar = rowsScrollPane.getVerticalScrollBar();
        JScrollBar horizontalBar = rowsScrollPane.getHorizontalScrollBar();
        int verticalBarWidth = verticalBar.getPreferredSize().width;
        int horizontalBarHeight = horizontalBar.getPreferredSize().height;

        Insets insets = rowsScrollPane.getInsets();
        int viewportWidth = scrollPaneWidth - insets.left - insets.right;
        int viewportHeight = scrollPaneHeight - insets.top - insets.bottom;
        if (viewportWidth <= 0 || viewportHeight <= 0) {
            return new Dimension(0, 0);
        }

        int extraWidth = 0;
        int extraHeight = 0;
        if (viewSize.height > viewportHeight) {
            extraWidth += verticalBarWidth;
            viewportWidth -= verticalBarWidth;
        }
        // Vertical bar steals viewport width — may require a horizontal bar too.
        if (viewSize.width > viewportWidth) {
            extraHeight += horizontalBarHeight;
        }
        return new Dimension(extraWidth, extraHeight);
    }

    ArgumentValueEditor argumentEditor(String name) {
        ArgumentValueEditor editor = argumentEditors.get(name);
        if (editor == null) {
            throw new IllegalArgumentException(
                    "No editor for argument \"" + name + "\" on module \""
                            + action.getModuleId() + "\"");
        }
        return editor;
    }

    void confirmEdits() {
        saveAndClose();
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
            if (validatedSeedOffset != null) {
                action.setSeedOffset(validatedSeedOffset);
            }
            for (Map.Entry<String, Object> entry : validatedArguments.entrySet()) {
                action.setArgument(entry.getKey(), entry.getValue());
            }
        } catch (IllegalArgumentException ex) {
            InvalidInputDialogs.show(this, ex.getMessage());
            return;
        }

        dispose();
    }

    public static void show(Window owner, Action action, boolean editable,
            EnumValuesProvider enumValuesProvider) {
        ModuleConfigDialog dialog =
                new ModuleConfigDialog(owner, action, editable, enumValuesProvider);
        dialog.setVisible(true);
    }

    // Suppresses focus-driven scrolling while the dialog is first shown and sized.
    private static final class ConfigScrollPane extends JScrollPane {
        private static final long serialVersionUID = 1L;

        private boolean suppressScrollRectToVisible = true;

        private ConfigScrollPane(ModuleConfigGridPanel grid) {
            super(grid);
        }

        private void releaseInitialScrollLock() {
            suppressScrollRectToVisible = false;
        }

        @Override
        public void scrollRectToVisible(Rectangle rect) {
            if (!suppressScrollRectToVisible) {
                super.scrollRectToVisible(rect);
            }
        }
    }
}
