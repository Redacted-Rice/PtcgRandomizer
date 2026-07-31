package redactedrice.ptcgr.randomizer.gui.moduleconfig.structured;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.ArgumentEditorFactory;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.ArgumentValueEditor;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.EnumValuesProvider;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.InvalidInputDialogs;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.GridSeparators;
import redactedrice.randomizer.lua.arguments.TypeDefinition;

// Renders one top level LIST/TABLE argument's whole value as a single flattened GridBagLayout
// "tree table" - one column pair/triple per nesting level, one row per leaf entry, with a nested
// entry's own remove/key cell spanning every row its subtree occupies.
//
// Add/remove are structural - they mutate the underlying value, then rebuild() from scratch,
// which is why every rebuild first re-extracts current live widget values so an
// add/remove anywhere in the tree never discards an in progress edit elsewhere in it. Typing into
// a leaf field never rebuilds, so it never loses focus.
//
// Also implements ArgumentValueEditor for top level LIST/TABLE module arguments (see
// ArgumentEditorFactory).
public final class StructuredGridPanel extends JPanel implements ArgumentValueEditor {
    private static final long serialVersionUID = 1L;

    private final TypeDefinition type;
    private final EnumValuesProvider enumValuesProvider;

    private List<Object> rawValue;
    private boolean editable = true;
    private CollectionNode root;

    public StructuredGridPanel(TypeDefinition type, EnumValuesProvider enumValuesProvider) {
        super(new GridBagLayout());
        setOpaque(false);
        this.type = type;
        this.enumValuesProvider = enumValuesProvider;
        this.rawValue = new ArrayList<>();
        rebuild();
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void setValue(Object publicValue) {
        this.rawValue = StructuredGridModel.toRaw(type, publicValue);
        rebuild();
    }

    @Override
    public Object getValue() {
        return StructuredGridModel.toPublic(type, extractRawApplying(root, null, list -> {
        }));
    }

    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
        applyEditable(root);
    }

    private void applyEditable(CollectionNode node) {
        for (Entry entry : node.entries) {
            entry.removeButton.setVisible(editable);
            if (entry.keyEditor != null) {
                entry.keyEditor.setEditable(editable);
            }
            if (entry.leafEditor != null) {
                entry.leafEditor.setEditable(editable);
            }
            if (entry.nestedCollection != null) {
                applyEditable(entry.nestedCollection);
            }
        }
        node.addButton.setVisible(editable);
    }

    private void rebuild() {
        removeAll();
        root = new CollectionNode(type);
        renderCollection(root, rawValue, 0, 0);
        revalidate();
        repaint();
    }

    // Renders collType/rawEntries entries starting at (colOffset, rowOffset) and returns the
    // number of grid rows used.
    private int renderCollection(CollectionNode node, List<Object> rawEntries, int colOffset,
            int rowOffset) {
        TypeDefinition collType = node.type;
        TypeDefinition childType =
                collType.isTable() ? collType.getValueType() : collType.getElementType();
        int removeCol = StructuredGridModel.removeColumn(colOffset, collType);
        boolean showHorizontalSeparators = StructuredGridModel.showsHorizontalSeparators(collType);
        int row = rowOffset;

        for (int i = 0; i < rawEntries.size(); i++) {
            if (i > 0 && showHorizontalSeparators) {
                addHorizontalSeparator(colOffset, collType, row);
                row++;
            }
            Object rawEntry = rawEntries.get(i);
            Object childValue =
                    collType.isTable() ? ((StructuredGridModel.RawEntry) rawEntry).value()
                            : rawEntry;
            int entryRows = StructuredGridModel.entryRowCount(childType, childValue);

            Entry entry = new Entry();
            JButton removeButton = StructuredGridHelpers.createRemoveButton(editable);
            entry.removeButton = removeButton;

            int valueColOffset = colOffset;
            if (collType.isTable()) {
                ArgumentValueEditor keyEditor =
                        ArgumentEditorFactory.createForType(collType.getKeyType(), enumValuesProvider);
                keyEditor.setValue(((StructuredGridModel.RawEntry) rawEntry).key());
                keyEditor.setEditable(editable);
                entry.keyEditor = keyEditor;
                addSpanningCell(
                        StructuredGridHelpers.wrapExpandableField(keyEditor.getComponent()),
                        colOffset, row, entryRows, true, colOffset, true);
                addSpanningCell(StructuredGridHelpers.createArrowLabel(), colOffset + 1, row,
                        entryRows, true, colOffset, false);
                valueColOffset = colOffset + 2;
            }

            if (childType.isComplex()) {
                addVerticalSeparator(valueColOffset, row, entryRows);
                CollectionNode childNode = new CollectionNode(childType);
                entry.nestedCollection = childNode;
                @SuppressWarnings("unchecked")
                List<Object> childRaw = (List<Object>) childValue;
                renderCollection(childNode, childRaw, valueColOffset + 1, row);
                addFramedRemoveCell(removeButton, removeCol, row, entryRows);
            } else {
                ArgumentValueEditor leafEditor =
                        ArgumentEditorFactory.createForType(childType, enumValuesProvider);
                leafEditor.setValue(childValue);
                leafEditor.setEditable(editable);
                entry.leafEditor = leafEditor;
                addLeafCell(leafEditor.getComponent(), valueColOffset, row, colOffset);
                addRemoveCell(removeButton, removeCol, row);
            }

            node.entries.add(entry);
            removeButton.addActionListener(e -> removeEntry(node, entry));

            row += entryRows;
        }

        if (!rawEntries.isEmpty() && showHorizontalSeparators) {
            addHorizontalSeparator(colOffset, collType, row);
            row++;
        }

        renderAddRow(node, colOffset, collType, row);
        row++;

        return row - rowOffset;
    }

    private void renderAddRow(CollectionNode node, int colOffset, TypeDefinition collType,
            int row) {
        TypeDefinition childType =
                collType.isTable() ? collType.getValueType() : collType.getElementType();

        JButton addButton = StructuredGridHelpers.createAddButton(editable);
        addButton.addActionListener(e -> addEntry(node));
        node.addButton = addButton;

        int addGridwidth = !collType.isTable() && childType.isComplex() ? 2 : 1;
        addAddCell(addButton, colOffset, row, addGridwidth, colOffset);
    }

    private void removeEntry(CollectionNode node, Entry entry) {
        applyStructuralChange(node, list -> list.remove(node.entries.indexOf(entry)));
    }

    private void addEntry(CollectionNode node) {
        TypeDefinition childType =
                node.type.isTable() ? node.type.getValueType() : node.type.getElementType();
        Object defaultChildValue =
                ArgumentEditorFactory.defaultValueFor(childType, enumValuesProvider);
        Object defaultChildRaw =
                childType.isComplex() ? StructuredGridModel.toRaw(childType, defaultChildValue)
                        : defaultChildValue;
        Object newRawEntry = defaultChildRaw;
        if (node.type.isTable()) {
            Object defaultKey = ArgumentEditorFactory.defaultValueFor(node.type.getKeyType(),
                    enumValuesProvider);
            newRawEntry = new StructuredGridModel.RawEntry(defaultKey, defaultChildRaw);
        }
        final Object newRawEntryFinal = newRawEntry;
        applyStructuralChange(node, list -> list.add(newRawEntryFinal));
    }

    private void applyStructuralChange(CollectionNode target, Consumer<List<Object>> mutation) {
        try {
            rawValue = extractRawApplying(root, target, mutation);
            rebuild();
        } catch (IllegalArgumentException ex) {
            showInvalidInput(ex.getMessage());
        }
    }

    private void showInvalidInput(String message) {
        InvalidInputDialogs.show(this, message);
    }

    private List<Object> extractRawApplying(CollectionNode node, CollectionNode target,
            Consumer<List<Object>> mutation) {
        List<Object> raw = new ArrayList<>();
        for (Entry entry : node.entries) {
            Object value = entry.nestedCollection != null
                    ? extractRawApplying(entry.nestedCollection, target, mutation)
                    : entry.leafEditor.getValue();
            raw.add(node.type.isTable()
                    ? new StructuredGridModel.RawEntry(entry.keyEditor.getValue(), value)
                    : value);
        }
        if (node == target) {
            mutation.accept(raw);
        }
        return raw;
    }

    private void addSpanningCell(JComponent component, int gridx, int gridy, int gridheight,
            boolean centerVertical, int bandStartCol, boolean expandHorizontal) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridheight = gridheight;
        gbc.anchor = centerVertical ? GridBagConstraints.CENTER : GridBagConstraints.NORTHWEST;
        if (expandHorizontal) {
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
        } else {
            gbc.fill = GridBagConstraints.NONE;
        }
        gbc.insets = StructuredGridHelpers.cellInsets(gridx, bandStartCol);
        add(component, gbc);
    }

    private void addRemoveCell(JButton button, int gridx, int gridy) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = StructuredGridHelpers.paddedCellInsets();
        add(button, gbc);
    }

    // Parent remove for a nested entry. A vertical rule on the left (closing off the inner band)
    // with the button centered vertically beside it, instead of letting inner horizontal rules
    // visually run through a lone spanning button pinned to the top.
    private void addFramedRemoveCell(JButton button, int gridx, int gridy, int gridheight) {
        JPanel cell = new JPanel(new GridBagLayout());
        cell.setOpaque(false);

        GridBagConstraints lineGbc = new GridBagConstraints();
        lineGbc.gridx = 0;
        lineGbc.gridy = 0;
        lineGbc.weighty = 1;
        lineGbc.fill = GridBagConstraints.VERTICAL;
        lineGbc.insets = new Insets(0, 0, 0, StructuredGridHelpers.ROW_HGAP);
        cell.add(GridSeparators.verticalLine(), lineGbc);

        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.gridx = 1;
        buttonGbc.gridy = 0;
        buttonGbc.weighty = 1;
        buttonGbc.anchor = GridBagConstraints.CENTER;
        buttonGbc.fill = GridBagConstraints.NONE;
        cell.add(button, buttonGbc);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridheight = gridheight;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = StructuredGridHelpers.paddedCellInsets();
        add(cell, gbc);
    }

    private void addLeafCell(JComponent component, int gridx, int gridy, int bandStartCol) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = StructuredGridHelpers.cellInsets(gridx, bandStartCol);
        add(StructuredGridHelpers.wrapExpandableField(component), gbc);
    }

    private void addAddCell(JButton button, int gridx, int gridy, int gridwidth,
            int bandStartCol) {
        JComponent cell = StructuredGridHelpers.wrapFixedSizeControl(button);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = StructuredGridHelpers.cellInsets(gridx, bandStartCol);
        add(cell, gbc);
    }

    private void addHorizontalSeparator(int colOffset, TypeDefinition collType, int gridy) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = colOffset;
        gbc.gridy = gridy;
        gbc.gridwidth = StructuredGridModel.totalColumns(collType);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets =
                new Insets(StructuredGridHelpers.ROW_VGAP, 0, StructuredGridHelpers.ROW_VGAP, 0);
        add(GridSeparators.horizontalLine(), gbc);
    }

    private void addVerticalSeparator(int gridx, int gridy, int gridheight) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridheight = gridheight;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets =
                new Insets(0, StructuredGridHelpers.ROW_HGAP, 0, StructuredGridHelpers.ROW_HGAP);
        add(GridSeparators.verticalLine(), gbc);
    }

    private static final class CollectionNode {
        private final TypeDefinition type;
        private final List<Entry> entries = new ArrayList<>();
        private JButton addButton;

        private CollectionNode(TypeDefinition type) {
            this.type = type;
        }
    }

    private static final class Entry {
        private JButton removeButton;
        private ArgumentValueEditor keyEditor;
        private ArgumentValueEditor leafEditor;
        private CollectionNode nestedCollection;
    }
}
