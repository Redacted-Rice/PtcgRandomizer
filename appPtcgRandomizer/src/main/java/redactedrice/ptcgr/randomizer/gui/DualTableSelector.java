package redactedrice.ptcgr.randomizer.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;

public class DualTableSelector extends JPanel {
    public DualTableSelector() {
        createUI();
    }
    
    // Custom Table Model to make entries non-editable
    static class NonEditableTableModel extends DefaultTableModel {
        NonEditableTableModel(Object[][] data, String[] columnNames) {
            super(data, columnNames);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
    
    static class ReorderableTableTransferHandler extends TransferHandler {
        private final JTable table;

        public ReorderableTableTransferHandler(JTable table) {
            this.table = table;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            int[] selectedRows = table.getSelectedRows();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < selectedRows.length; i++) {
                if (i > 0) {
                    sb.append("\n");
                }
                int row = selectedRows[i];
                // Encode the row index and cell data (assuming a single column table)
                sb.append(row).append(":").append(table.getValueAt(row, 0));
            }
            return new StringSelection(sb.toString());
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        public boolean canImport(TransferSupport support) {
            // Allow drop only if it is a drop operation and the String flavor is supported.
            return support.isDrop() && support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support))
                return false;
            try {
                String data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                if (data == null || data.isEmpty())
                    return false;
                String[] lines = data.split("\n");
                ArrayList<Integer> sourceIndices = new ArrayList<>();
                ArrayList<Object> values = new ArrayList<>();
                for (String line : lines) {
                    String[] parts = line.split(":", 2);
                    if(parts.length == 2) {
                        sourceIndices.add(Integer.parseInt(parts[0]));
                        values.add(parts[1]);
                    }
                }
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                Point dropPoint = support.getDropLocation().getDropPoint();
                int dropIndex = table.rowAtPoint(dropPoint);
                if (dropIndex < 0) {
                    dropIndex = model.getRowCount();
                }
                // Adjust dropIndex to account for removed rows that were originally above the drop position.
                int adjustment = 0;
                for (int idx : sourceIndices) {
                    if (idx < dropIndex) {
                        adjustment++;
                    }
                }
                dropIndex = dropIndex - adjustment;
                
                // Remove dragged rows from the model in descending order to avoid index shifting.
                Collections.sort(sourceIndices, Collections.reverseOrder());
                for (int idx : sourceIndices) {
                    model.removeRow(idx);
                }
                // Insert dragged values at the drop location while preserving their original order.
                for (int i = 0; i < values.size(); i++) {
                    model.insertRow(dropIndex + i, new Object[]{values.get(i)});
                }
                // Re-select the newly inserted rows.
                table.setRowSelectionInterval(dropIndex, dropIndex + values.size() - 1);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }
    
    static class LeftTableDoubleClickListener extends MouseAdapter {
        private final JTable leftTable;
        private final DefaultTableModel leftModel;
        private final DefaultTableModel rightModel;

        public LeftTableDoubleClickListener(JTable leftTable, DefaultTableModel leftModel, DefaultTableModel rightModel) {
            this.leftTable = leftTable;
            this.leftModel = leftModel;
            this.rightModel = rightModel;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) { // Double-click detection
                int row = leftTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    Object item = leftModel.getValueAt(row, 0);
                    rightModel.addRow(new Object[]{item});
                }
            }
        }
    }
    
    static class RightTableDoubleClickListener extends MouseAdapter {
        private final JTable rightTable;
        private final DefaultTableModel rightModel;

        public RightTableDoubleClickListener(JTable rightTable, DefaultTableModel rightModel) {
            this.rightTable = rightTable;
            this.rightModel = rightModel;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) { // Double-click detection
                int row = rightTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    rightModel.removeRow(row);
                }
            }
        }
    }
    
    static class AddButtonActionListener implements ActionListener {
        private final JTable leftTable;
        private final DefaultTableModel leftModel;
        private final DefaultTableModel rightModel;

        public AddButtonActionListener(JTable leftTable, DefaultTableModel leftModel, DefaultTableModel rightModel) {
            this.leftTable = leftTable;
            this.leftModel = leftModel;
            this.rightModel = rightModel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int[] selectedRows = leftTable.getSelectedRows();
            for (int row : selectedRows) {
                Object item = leftModel.getValueAt(row, 0);
                rightModel.addRow(new Object[]{item});
            }
        }
    }
    
    public class RemoveButtonActionListener implements ActionListener {
        private final JTable rightTable;
        private final DefaultTableModel rightModel;

        public RemoveButtonActionListener(JTable rightTable, DefaultTableModel rightModel) {
            this.rightTable = rightTable;
            this.rightModel = rightModel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int[] selectedRows = rightTable.getSelectedRows();
            if (selectedRows.length > 0) {
                int removedCount = selectedRows.length;
                // Compute the original count before removals.
                int origCount = rightModel.getRowCount() + removedCount;
                int newSelection;
                int highestRemoved = selectedRows[selectedRows.length - 1];

                if (highestRemoved < origCount - 1) {
                    // An item exists below the removed block.
                    newSelection = highestRemoved - removedCount + 1;
                } else {
                    // No item below; select the item above the removed block.
                    newSelection = selectedRows[0] - 1;
                    if (newSelection < 0) {
                        newSelection = 0;
                    }
                }

                // Remove the selected rows in descending order to avoid shifting issues.
                for (int i = selectedRows.length - 1; i >= 0; i--) {
                    rightModel.removeRow(selectedRows[i]);
                }

                // If the table isn’t empty, set the selection.
                if (rightModel.getRowCount() > 0) {
                    newSelection = Math.min(newSelection, rightModel.getRowCount() - 1);
                    rightTable.setRowSelectionInterval(newSelection, newSelection);
                }
            }
        }
    }
    
    private void createUI() {
        setLayout(new BorderLayout());

        // Table Models
        DefaultTableModel leftModel = new NonEditableTableModel(new Object[][]{
            {"Item 1", "Description A"},
            {"Item 2", "Description B"},
            {"Item 3", "Description C"},
            {"Item 4", "Description D"}
        }, new String[]{"Name", "Details"});
        
        DefaultTableModel rightModel = new NonEditableTableModel(new Object[][]{}, new String[]{"Name", "Details"});
        
        JTable leftTable = new JTableHoveredItemToolTip(leftModel);
        JTable rightTable = new JTableHoveredItemToolTip(rightModel);

        leftTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        leftTable.addMouseListener(new LeftTableDoubleClickListener(leftTable, leftModel, rightModel));
        
        rightTable.setTransferHandler(new ReorderableTableTransferHandler(rightTable));
        rightTable.setDragEnabled(true);
        rightTable.addMouseListener(new RightTableDoubleClickListener(rightTable, rightModel));

        // Wrap tables in scroll panes
        JScrollPane leftScrollPane = new JScrollPane(leftTable);
        JScrollPane rightScrollPane = new JScrollPane(rightTable);

        leftScrollPane.setPreferredSize(new Dimension(250, 300));
        rightScrollPane.setPreferredSize(new Dimension(250, 300));

        JButton moveUpButton = new JButton("Move Up");
        moveUpButton.addActionListener(e -> moveRow(rightTable, rightModel, -1));

        JButton moveDownButton = new JButton("Move Down");
        moveDownButton.addActionListener(e -> moveRow(rightTable, rightModel, 1));

        // Layout
	     // Create a combo box (drop-down) as before…
	     JComboBox<String> leftComboBox = new JComboBox<>(new String[] { "Option 1", "Option 2", "Option 3" });
	     // Wrap it in a panel that centers it (using FlowLayout with CENTER)
	     JPanel topLeftPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
	     topLeftPanel.add(leftComboBox);
	     // Create a top panel and add the combo to its WEST so it aligns with the left table column
	     JPanel topPanel = new JPanel(new BorderLayout());
	     topPanel.add(topLeftPanel, BorderLayout.WEST);
	
	     // Remove the combo box from here; keep only the left scroll pane.
	     JPanel leftPanel = new JPanel(new BorderLayout());
	     leftPanel.add(leftScrollPane, BorderLayout.CENTER);
	
         JButton addButton = new JButton("Add Selected");
         addButton.addActionListener(new AddButtonActionListener(leftTable, leftModel, rightModel));
         
         JButton removeButton = new JButton("Remove Selected");
         removeButton.addActionListener(new RemoveButtonActionListener(rightTable, rightModel));
	     
	     // 1. Compute the maximum preferred width for both buttons.
	     Dimension addPref = addButton.getPreferredSize();
	     Dimension removePref = removeButton.getPreferredSize();
	     int maxWidth = Math.max(addPref.width, removePref.width);

	     // 2. Create new dimensions for both buttons using the maximum width.
	     Dimension newAddSize = new Dimension(maxWidth, addPref.height);
	     Dimension newRemoveSize = new Dimension(maxWidth, removePref.height);

	     // 3. Update the add and remove buttons to have the same size.
	     addButton.setPreferredSize(newAddSize);
	     addButton.setMaximumSize(newAddSize);
	     removeButton.setPreferredSize(newRemoveSize);
	     removeButton.setMaximumSize(newRemoveSize);

	     // 4. Center buttons horizontally.
	     addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	     removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

	     // 5. Calculate the fixed size of the middle panel (fixed width is maxWidth, height includes both buttons plus spacing).
	     int verticalGap = 10;
	     int fixedHeight = newAddSize.height + newRemoveSize.height + verticalGap;
	     Dimension fixedPanelSize = new Dimension(maxWidth, fixedHeight);

	     // 6. Create the middle panel with BoxLayout on the Y_AXIS and fix its size.
	     JPanel middlePanel = new JPanel();
	     middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
	     middlePanel.setPreferredSize(fixedPanelSize);
	     middlePanel.setMinimumSize(fixedPanelSize);
	     middlePanel.setMaximumSize(fixedPanelSize);

	     // 7. Add vertical glue to center the buttons inside the fixed panel.
	     middlePanel.add(Box.createVerticalGlue());
	     middlePanel.add(addButton);
	     middlePanel.add(Box.createRigidArea(new Dimension(0, verticalGap)));
	     middlePanel.add(removeButton);
	     middlePanel.add(Box.createVerticalGlue());
	
	     // Remains the same: right table in CENTER and move buttons in SOUTH.
	     JPanel rightPanel = new JPanel(new BorderLayout());
	     rightPanel.add(rightScrollPane, BorderLayout.CENTER);
	     JPanel moveButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
	     moveButtonPanel.add(moveUpButton);
	     moveButtonPanel.add(moveDownButton);
	     rightPanel.add(moveButtonPanel, BorderLayout.SOUTH);
	
	  // Wrap leftPanel in a container that fills available space.
	     JPanel leftWrapper = new JPanel(new BorderLayout());
	     leftWrapper.add(leftPanel, BorderLayout.CENTER);
	     leftWrapper.setMinimumSize(new Dimension(0, 0)); // allow continuous vertical shrink

	     // Wrap rightPanel similarly.
	     JPanel rightWrapper = new JPanel(new BorderLayout());
	     rightWrapper.add(rightPanel, BorderLayout.CENTER);
	     rightWrapper.setMinimumSize(new Dimension(0, 0));

	     // Now set up the columnsPanel using GridBagLayout.
	     JPanel columnsPanel = new JPanel(new GridBagLayout());
	     GridBagConstraints gbc = new GridBagConstraints();
	     gbc.insets = new Insets(10, 10, 10, 10);
	     gbc.fill = GridBagConstraints.BOTH;

	     // Left column: Expand both horizontally and vertically.
	     GridBagConstraints gbcLeft = (GridBagConstraints) gbc.clone();
	     gbcLeft.gridx = 0;
	     gbcLeft.weightx = 1.0;
	     gbcLeft.weighty = 1.0;
	     columnsPanel.add(leftWrapper, gbcLeft);

	     // Middle column: Fixed size.
	     GridBagConstraints gbcMiddle = (GridBagConstraints) gbc.clone();
	     gbcMiddle.gridx = 1;
	     gbcMiddle.weightx = 0.0;
	     gbcMiddle.weighty = 0.0;
	     columnsPanel.add(middlePanel, gbcMiddle);

	     // Right column: Expand both horizontally and vertically.
	     GridBagConstraints gbcRight = (GridBagConstraints) gbc.clone();
	     gbcRight.gridx = 2;
	     gbcRight.weightx = 1.0;
	     gbcRight.weighty = 1.0;
	     columnsPanel.add(rightWrapper, gbcRight);
	
	     // Use BorderLayout for the main DualTablePanel: add the topPanel in the NORTH and 
	     // the columnsPanel in the CENTER.
	     setLayout(new BorderLayout());
	     add(topPanel, BorderLayout.NORTH);
	     add(columnsPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void moveRow(JTable table, DefaultTableModel model, int direction) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && selectedRow + direction >= 0 && selectedRow + direction < model.getRowCount()) {
            Object temp = model.getValueAt(selectedRow, 0);
            model.setValueAt(model.getValueAt(selectedRow + direction, 0), selectedRow, 0);
            model.setValueAt(temp, selectedRow + direction, 0);
            table.setRowSelectionInterval(selectedRow + direction, selectedRow + direction);
        }
    }
}