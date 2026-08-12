package redactedrice.ptcgr.randomizer.gui.dualselector.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;

import redactedrice.ptcgr.configs.AppPreferences;
import redactedrice.ptcgr.configs.Config;
import redactedrice.ptcgr.configs.YamlIO;
import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.ptcgr.randomizer.gui.RandomizerApp;
import redactedrice.ptcgr.randomizer.gui.dualselector.listener.ActionsFilterChangedListener;
import redactedrice.ptcgr.randomizer.gui.dualselector.listener.CopySelectedListener;
import redactedrice.ptcgr.randomizer.gui.dualselector.listener.RemoveSelectedListener;
import redactedrice.ptcgr.randomizer.gui.dualselector.model.ActionsListTableModel;
import redactedrice.ptcgr.randomizer.gui.dualselector.model.ActionsSelectedTableModel;
import redactedrice.ptcgr.utils.FileExtensionUtils;

public class DualTableSelector extends JPanel {
    private static final long serialVersionUID = 1L;
    private final ActionsSelectedTableModel selectedModel;
    private final ActionBank actionBank;
    private final AppPreferences appPreferences;
    private final RandomizerApp app;
    private final JFileChooser exportActionsChooser = new JFileChooser();

    public DualTableSelector(ActionBank actions, AppPreferences appPreferences, RandomizerApp app) {
        this.actionBank = actions;
        this.appPreferences = appPreferences;
        this.app = app;
        selectedModel = new ActionsSelectedTableModel();
        exportActionsChooser.setFileFilter(
                new FileNameExtensionFilter("YAML files", "yaml", "yml"));
        applyExportChooserPreferences();
        createUI(actions);
    }

    public List<Action> getSelectedActions() {
        return selectedModel.getRows();
    }

    public void setSelectedActions(List<Action> actions) {
        selectedModel.setRows(actions);
    }

    public void mergeSelectedActions(List<Action> importedActions) {
        if (importedActions == null || importedActions.isEmpty()) {
            return;
        }
        for (Action action : importedActions) {
            selectedModel.appendRow(action);
        }
    }

    void applyExportChooserPreferences() {
        AppPreferences.applyChooserDirectory(exportActionsChooser,
                appPreferences.getExportActionsDirectory());
        exportActionsChooser.setSelectedFile(appPreferences.resolveExportActionsFile());
    }

    public File getExportActionsDirectory() {
        return exportActionsChooser.getCurrentDirectory();
    }

    public File getExportActionsSelectedFile() {
        return exportActionsChooser.getSelectedFile();
    }

    private void exportActions() {
        List<Action> actions = getSelectedActions();
        if (actions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add at least one module to the selected list.",
                    "Nothing to Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        applyExportChooserPreferences();
        if (exportActionsChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File exportPath = FileExtensionUtils.ensureExtension(
                exportActionsChooser.getSelectedFile(), YamlIO.FILE_EXTENSION);
        try {
            YamlIO.save(exportPath,
                    Config.convertActionsOnlyToYamlMap(actions, actionBank));
            appPreferences.setExportActionsDirectory(
                    exportActionsChooser.getCurrentDirectory().getAbsolutePath());
            appPreferences.setExportActionsFileName(exportPath.getName());
            app.saveAppPreferencesQuietly();
        } catch (IOException error) {
            error.printStackTrace();
            JOptionPane.showMessageDialog(this, error.getMessage(), "Export Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createUI(ActionBank actions) {
        setLayout(new BorderLayout());

        // Table Models
        ActionsListTableModel listModel = new ActionsListTableModel(actions);
        // selectedModel already set

        JTable listTable = new ActionsListTable(listModel, selectedModel, actions);
        ActionsSelectedTable selectedTable = new ActionsSelectedTable(selectedModel, actions);

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
        // Filter combos for the available actions list
        JComboBox<String> groupFilterComboBox = new JComboBox<>();
        for (String category : actions.getCategoriesWithAll()) {
            groupFilterComboBox.addItem(category);
        }
        groupFilterComboBox.setSelectedIndex(0);

        JComboBox<String> fieldFilterComboBox = new JComboBox<>();
        for (String field : actions.getModifiesWithAll()) {
            fieldFilterComboBox.addItem(field);
        }
        fieldFilterComboBox.setSelectedIndex(0);

        ActionsFilterChangedListener filterListener =
                new ActionsFilterChangedListener(listModel, groupFilterComboBox, fieldFilterComboBox);
        groupFilterComboBox.addActionListener(filterListener);
        fieldFilterComboBox.addActionListener(filterListener);
        filterListener.actionPerformed(null);

        JPanel topLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topLeftPanel.add(new JLabel("Action group filter:"));
        topLeftPanel.add(groupFilterComboBox);
        topLeftPanel.add(new JLabel("Field effected filter:"));
        topLeftPanel.add(fieldFilterComboBox);
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
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(topPanel, BorderLayout.NORTH);
        add(columnsPanel, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton exportButton = new JButton("Export Actions");
        exportButton.setToolTipText(
                "Save the selected module list and settings to a YAML file.");
        exportButton.addActionListener(event -> exportActions());
        footer.add(exportButton);
        add(footer, BorderLayout.SOUTH);
        setVisible(true);
    }
}
