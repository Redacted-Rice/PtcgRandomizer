package redactedrice.ptcgr.randomizer.gui.dualselector;


import java.util.List;

import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;

import javax.swing.*;
import java.awt.*;

public class DualTableSelector extends JPanel {
    private static final long serialVersionUID = 1L;
    private final ActionsTableModel selectedModel;

    public DualTableSelector(ActionBank actions) {
        selectedModel = new ActionSelectedTableModel();
        createUI(actions);
    }

    public List<Action> getSelectedActions() {
        return selectedModel.getRows();
    }

    private void createUI(ActionBank actions) {
        setLayout(new BorderLayout());

        // Table Models
        ActionsListTableModel listModel = new ActionsListTableModel(actions);
        // selectedModel already set

        JTable listTable = new ActionsListTable(listModel, selectedModel);
        ActionsSelectedTable selectedTable = new ActionsSelectedTable(selectedModel);

        // Wrap tables in scroll panes
        JScrollPane leftScrollPane = new JScrollPane(listTable);
        JScrollPane rightScrollPane = new JScrollPane(selectedTable);

        leftScrollPane.setPreferredSize(new Dimension(250, 300));
        rightScrollPane.setPreferredSize(new Dimension(250, 300));

        JButton moveUpButton = new JButton("Move Up");
        moveUpButton.addActionListener(e -> selectedTable.moveSelectedRow(-1));

        JButton moveDownButton = new JButton("Move Down");
        moveDownButton.addActionListener(e -> selectedTable.moveSelectedRow(1));

        // Layout
        // Create a combo box (drop-down) as before…
        JComboBox<String> categoryComboBox = new JComboBox<>();
        for (String category : actions.getCategoriesWithAll()) {
            categoryComboBox.addItem(category);
        }
        categoryComboBox.addActionListener(new CategoryChangedListener(listModel));
        categoryComboBox.setSelectedIndex(0);

        // Wrap it in a panel that centers it (using FlowLayout with CENTER)
        JPanel topLeftPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topLeftPanel.add(categoryComboBox);
        // Create a top panel and add the combo to its WEST so it aligns with the left table column
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(topLeftPanel, BorderLayout.WEST);

        // Remove the combo box from here; keep only the left scroll pane.
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(leftScrollPane, BorderLayout.CENTER);

        JButton addButton = new JButton("Add Selected");
        addButton.addActionListener(new CopySelectedListener(listTable, listModel, selectedModel));

        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(new RemoveSelectedListener(selectedTable, selectedModel));

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

        // 5. Calculate the fixed size of the middle panel (fixed width is maxWidth, height includes
        // both buttons plus spacing).
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
}
