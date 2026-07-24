package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

// Shared row container for inline LIST/TABLE editors - stacked entry rows with a trailing "+ Add"
// row. Handles insert before add, clear, refresh, and add button visibility.
final class StructuredRowsPanel {
    private final JPanel rowsPanel = new JPanel();
    private final JPanel addRowPanel;
    private final JButton addButton = new JButton("+ Add");

    StructuredRowsPanel(Runnable onAdd) {
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
        rowsPanel.setOpaque(false);

        addButton.setFocusable(false);
        addButton.addActionListener(e -> onAdd.run());

        addRowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,
                StructuredInlineEditorSupport.ROW_HGAP, StructuredInlineEditorSupport.ROW_VGAP));
        addRowPanel.setOpaque(false);
        addRowPanel.add(addButton);
        rowsPanel.add(addRowPanel);
    }

    JPanel getRowsPanel() {
        return rowsPanel;
    }

    JButton getAddButton() {
        return addButton;
    }

    void clearEntryRows() {
        for (int index = rowsPanel.getComponentCount() - 2; index >= 0; index--) {
            rowsPanel.remove(index);
        }
    }

    void insertEntryRow(JPanel rowPanel) {
        rowsPanel.add(rowPanel, rowsPanel.getComponentCount() - 1);
        refresh();
    }

    void removeEntryRow(JPanel rowPanel) {
        rowsPanel.remove(rowPanel);
        refresh();
    }

    void refresh() {
        StructuredInlineEditorSupport.applyStructuredContentMinWidth(rowsPanel);
        rowsPanel.revalidate();
        rowsPanel.repaint();
    }

    void setAddButtonVisible(boolean visible) {
        addButton.setVisible(visible);
    }
}
