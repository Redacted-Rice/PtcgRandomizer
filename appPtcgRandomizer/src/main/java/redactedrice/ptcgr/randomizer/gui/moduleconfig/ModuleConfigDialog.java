package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.randomizer.lua.Module;
import redactedrice.randomizer.lua.arguments.ArgumentDefinition;

// Shows the seed offset (for seeded modules) and every module argument as an aligned list of
// rows. All rows share a single GridBagLayout so columns stay lined up regardless of how many
// rows there are. Opens with a fixed max size and scrolls if there are more rows than fit.
public class ModuleConfigDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private static final int MIN_WIDTH = 520;
    private static final int MAX_WIDTH = 560;
    private static final int MIN_HEIGHT = 168;
    private static final int MAX_HEIGHT = 420;
    private static final int VALUE_FIELD_WIDTH = 180;
    private static final Color LINE_COLOR = new Color(215, 215, 215);
    private static final int CELL_PADDING_H = 8;
    private static final int CELL_PADDING_V = 4;
    private static final int LINE_WIDTH = 1;

    private static final int ARGUMENT_COLUMN = 0;
    private static final int SEPARATOR_AFTER_ARGUMENT = 1;
    private static final int CONSTRAINTS_COLUMN = 2;
    private static final int SEPARATOR_AFTER_CONSTRAINTS = 3;
    private static final int VALUE_COLUMN = 4;
    private static final int COLUMN_COUNT = 5;

    private static final int DIALOG_PADDING = 10;
    // Tighter gap between the table and buttons
    private static final int CONTENT_BUTTON_GAP = 4;

    private final Action action;
    private final boolean editable;
    private ArgumentValueEditor seedOffsetEditor;
    private final Map<String, ArgumentValueEditor> argumentEditors = new LinkedHashMap<>();
    private JScrollPane rowsScrollPane;

    public ModuleConfigDialog(Window owner, Action action, boolean editable) {
        super(owner, editable ? "Edit Configs" : "Show Configs", ModalityType.APPLICATION_MODAL);
        this.action = action;
        this.editable = editable;

        setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(DIALOG_PADDING, DIALOG_PADDING,
                DIALOG_PADDING, DIALOG_PADDING));
        rowsScrollPane = buildRowsPanel();
        rowsScrollPane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        contentPanel.add(rowsScrollPane);
        contentPanel.add(Box.createVerticalStrut(CONTENT_BUTTON_GAP));
        JPanel buttonPanel = buildButtonPanel();
        buttonPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createVerticalGlue());
        add(contentPanel, BorderLayout.CENTER);

        pack();
        applySizeConstraints();
        setLocationRelativeTo(owner);
    }

    private JScrollPane buildRowsPanel() {
        Module module = action.getModule();

        JPanel grid = new JPanel(new GridBagLayout());
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
                ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);
                editor.setValue(action.getArgument(name));
                argumentEditors.put(name, editor);
                valueComponent = editor.getComponent();
            } else {
                valueComponent = readOnlyValueLabel(action.getArgument(name));
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

        JScrollPane scrollPane = new JScrollPane(grid);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(grid.getPreferredSize());
        return scrollPane;
    }

    private void addHeaderRow(JPanel grid, GridBagConstraints gbc, int row) {
        addCell(grid, gbc, row, ARGUMENT_COLUMN, headerLabel("Argument"), GridBagConstraints.WEST,
                false);
        addCell(grid, gbc, row, CONSTRAINTS_COLUMN, headerLabel("Constraints"),
                GridBagConstraints.CENTER, false);
        addCell(grid, gbc, row, VALUE_COLUMN, headerLabel("Value"), GridBagConstraints.WEST, true);
    }

    private static JLabel headerLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    private void addCell(JPanel grid, GridBagConstraints gbc, int row, int column,
            JComponent content, int anchor, boolean fillHorizontal) {
        gbc.gridx = column;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = anchor;
        gbc.weightx = column == VALUE_COLUMN ? 1 : 0;
        gbc.weighty = 0;
        gbc.fill = fillHorizontal ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        gbc.insets = new Insets(CELL_PADDING_V, CELL_PADDING_H, CELL_PADDING_V, CELL_PADDING_H);
        grid.add(content, gbc);
    }

    private int addRow(JPanel grid, GridBagConstraints gbc, int row, String name,
            String constraintDescription, JComponent valueComponent) {
        addCell(grid, gbc, row, ARGUMENT_COLUMN, new JLabel(name), GridBagConstraints.WEST, false);

        JLabel constraintLabel = new JLabel(constraintDescription);
        constraintLabel.setForeground(Color.DARK_GRAY);
        addCell(grid, gbc, row, CONSTRAINTS_COLUMN, constraintLabel, GridBagConstraints.CENTER,
                false);

        addCell(grid, gbc, row, VALUE_COLUMN, widenValueComponent(valueComponent),
                GridBagConstraints.WEST, true);

        return row + 1;
    }

    private static JComponent widenValueComponent(JComponent valueComponent) {
        Dimension pref = valueComponent.getPreferredSize();
        valueComponent.setPreferredSize(new Dimension(VALUE_FIELD_WIDTH, pref.height));
        return valueComponent;
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

    public static void show(Window owner, Action action, boolean editable) {
        ModuleConfigDialog dialog = new ModuleConfigDialog(owner, action, editable);
        dialog.setVisible(true);
    }
}
