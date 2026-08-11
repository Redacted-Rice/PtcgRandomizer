package redactedrice.ptcgr.randomizer.gui.rules;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import redactedrice.ptcgr.configs.AppPreferences;
import redactedrice.ptcgr.configs.Config;
import redactedrice.ptcgr.configs.YamlIO;
import redactedrice.ptcgr.configs.rules.MoveAssignmentConfig;
import redactedrice.ptcgr.configs.rules.MoveExclusionConfig;
import redactedrice.ptcgr.configs.rules.RulesConfig;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.randomizer.RandomizerCore;
import redactedrice.ptcgr.randomizer.gui.RandomizerApp;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.StructuredGridHelpers;
import redactedrice.ptcgr.rules.MoveAssignment;
import redactedrice.ptcgr.rules.MoveAssignments;
import redactedrice.ptcgr.rules.MoveExclusion;
import redactedrice.ptcgr.rules.Rules;
import redactedrice.ptcgr.utils.FileExtensionUtils;
import redactedrice.ptcgr.utils.IssuePresenter;
import redactedrice.randomizer.utils.IssueTracker;

/** The active move exclusion and assignment rules. */
public class RulesPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public static final String USER_ADDED_SOURCE = "user added";

    private static final String ASSIGNMENTS_NO_ROM_PLACEHOLDER =
            "Open a ROM to view and edit move assignments.";

    private static final int SECTION_SCROLL_WIDTH = 1000;
    private static final int SECTION_SCROLL_HEIGHT = 180;

    private static final int EXCLUSION_REMOVE_FROM_POOL_COLUMN = 2;
    private static final int EXCLUSION_GENERATE_ASSIGNMENTS_COLUMN = 3;
    private static final int EXCLUSION_REMOVE_COLUMN = 5;

    private static final Comparator<String> MOVE_NAME_ORDER =
            String.CASE_INSENSITIVE_ORDER;

    private static final String DEFAULT_EXPORT_ALL_RULES_FILE_NAME = "all_rules.yaml";

    /** What the rules tab reads from the running app. */
    interface Session {
        Rules getRules();

        boolean isRomLoaded();

        CardGroup<MonsterCard> getReferenceMonsterCards();
    }

    private final JFileChooser exportUserRulesChooser = new JFileChooser();
    private final JFileChooser exportAllRulesChooser = new JFileChooser();
    private final Session session;
    private final AppPreferences appPreferences;
    private final RandomizerApp app;
    private final MoveExclusionsTableModel exclusionsModel;
    private final MoveAssignmentsTableModel assignmentsModel;
    private JButton addAssignmentButton;
    private CardLayout assignmentsContentLayout;
    private JPanel assignmentsContentPanel;

    public RulesPanel(RandomizerCore randomizerCore, AppPreferences appPreferences,
            RandomizerApp app) {
        this(sessionFrom(randomizerCore), appPreferences, app);
    }

    // package private so tests can skip spinning up RandomizerCore
    RulesPanel(Session session, AppPreferences appPreferences, RandomizerApp app) {
        this.session = session;
        this.appPreferences = appPreferences;
        this.app = app;
        exportUserRulesChooser.setFileFilter(
                new FileNameExtensionFilter("YAML files", "yaml", "yml"));
        exportAllRulesChooser.setFileFilter(
                new FileNameExtensionFilter("YAML files", "yaml", "yml"));
        applyExportChooserPreferences();
        exclusionsModel = new MoveExclusionsTableModel();
        assignmentsModel = new MoveAssignmentsTableModel();
        createUi();
        refresh();
    }

    private static Session sessionFrom(RandomizerCore randomizerCore) {
        return new Session() {
            @Override
            public Rules getRules() {
                return randomizerCore.getRules();
            }

            @Override
            public boolean isRomLoaded() {
                return randomizerCore.isRomLoaded();
            }

            @Override
            public CardGroup<MonsterCard> getReferenceMonsterCards() {
                return randomizerCore.getReferenceMonsterCards();
            }
        };
    }

    void applyExportChooserPreferences() {
        AppPreferences.applyChooserDirectory(exportUserRulesChooser,
                appPreferences.getExportUserRulesDirectory());
        exportUserRulesChooser.setSelectedFile(appPreferences.resolveExportUserRulesFile());
        AppPreferences.applyChooserDirectory(exportAllRulesChooser,
                appPreferences.getExportUserRulesDirectory());
        exportAllRulesChooser.setSelectedFile(resolveExportAllRulesFile());
    }

    private File resolveExportAllRulesFile() {
        File userRulesFile = appPreferences.resolveExportUserRulesFile();
        File directory = userRulesFile.getParentFile();
        if (directory != null && directory.isDirectory()) {
            return new File(directory, DEFAULT_EXPORT_ALL_RULES_FILE_NAME);
        }
        return new File(DEFAULT_EXPORT_ALL_RULES_FILE_NAME);
    }

    public File getExportUserRulesDirectory() {
        return exportUserRulesChooser.getCurrentDirectory();
    }

    public File getExportUserRulesSelectedFile() {
        return exportUserRulesChooser.getSelectedFile();
    }

    public void refresh() {
        exclusionsModel.setRows(session.getRules().getMoveExclusions().getAllExclusions());
        assignmentsModel.setRows(buildAssignmentRows());
        if (addAssignmentButton != null) {
            addAssignmentButton.setEnabled(session.isRomLoaded());
        }
        updateAssignmentsContentVisibility();
    }

    private void updateAssignmentsContentVisibility() {
        if (assignmentsContentLayout == null) {
            return;
        }
        boolean showTable = session.isRomLoaded() || hasAssignmentsToShow();
        String card = showTable ? "table" : "placeholder";
        assignmentsContentLayout.show(assignmentsContentPanel, card);
    }

    private boolean hasAssignmentsToShow() {
        return !session.getRules().getMoveAssignments().getAllAssignments().isEmpty();
    }

    private List<AssignmentRow> buildAssignmentRows() {
        CardGroup<MonsterCard> cards = session.getReferenceMonsterCards();

        List<AssignmentRow> rows = new ArrayList<>();
        for (MoveAssignment assignment : session.getRules().getMoveAssignments()
                .getAllAssignments()) {
            String toCard;
            String fromCardLabel;
            String moveName;
            if (assignment.isPending()) {
                toCard = assignment.getToCardSpecifier();
                fromCardLabel = assignment.getFromCardSpecifier();
                moveName = assignment.getMoveName();
            } else {
                MonsterCard card = cards != null ? cards.withId(assignment.getCardId()) : null;
                toCard = card != null ? card.toNameWithLevelSpecifier()
                        : assignment.getCardId().toString();
                MonsterCard fromCard = assignment.getMove().getSourceCard();
                fromCardLabel = fromCard != null ? fromCard.toNameWithLevelSpecifier() : "";
                moveName = assignment.getMoveName();
            }
            rows.add(new AssignmentRow(assignment,
                    MoveAssignments.assignmentSourceDisplayLabel(assignment.getSourceFileName()),
                    fromCardLabel,
                    toCard,
                    String.valueOf(assignment.getMoveSlot() + 1),
                    moveName));
        }
        return rows;
    }

    private void createUi() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JTable exclusionsTable = createExclusionsTable();
        JTable assignmentsTable = createAssignmentsTable();

        JPanel exclusionsSection = createSection("Move Exclusions", exclusionsTable,
                () -> addExclusion());
        JPanel assignmentsSection = createAssignmentsSection(assignmentsTable);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, exclusionsSection,
                assignmentsSection);
        splitPane.setResizeWeight(0.65);
        splitPane.setContinuousLayout(true);
        add(splitPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton exportAllButton = new JButton("Export All Rules");
        exportAllButton.setToolTipText(
                "Save all active exclusions and assignments to a YAML file.");
        exportAllButton.addActionListener(event -> exportAllRules());
        footer.add(exportAllButton);
        JButton exportButton = new JButton("Export Added Rules");
        exportButton.setToolTipText(
                "Save user added exclusions and assignments to a YAML file.");
        exportButton.addActionListener(event -> exportUserAddedRules());
        footer.add(exportButton);
        add(footer, BorderLayout.SOUTH);
    }

    private void exportAllRules() {
        RulesConfig exportConfig = RulesConfig.fromRules(session.getRules(),
                session.getReferenceMonsterCards());
        if (exportConfig.getMoveExclusionConfigs().isEmpty()
                && exportConfig.getMoveAssignmentConfigs().isEmpty()) {
            JOptionPane.showMessageDialog(this, "There are no rules to export.",
                    "Nothing to Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        applyExportChooserPreferences();
        if (exportAllRulesChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File exportPath = FileExtensionUtils.ensureExtension(
                exportAllRulesChooser.getSelectedFile(), YamlIO.FILE_EXTENSION);
        try {
            YamlIO.save(exportPath, Config.convertRulesOnlyToYamlMap(exportConfig));
            appPreferences.setExportUserRulesDirectory(
                    exportAllRulesChooser.getCurrentDirectory().getAbsolutePath());
            app.saveAppPreferencesQuietly();
        } catch (IOException error) {
            error.printStackTrace();
            JOptionPane.showMessageDialog(this, error.getMessage(), "Export Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportUserAddedRules() {
        RulesConfig exportConfig = buildUserAddedExportConfig();
        if (exportConfig.getMoveExclusionConfigs().isEmpty()
                && exportConfig.getMoveAssignmentConfigs().isEmpty()) {
            JOptionPane.showMessageDialog(this, "There are no user added rules to export.",
                    "Nothing to Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        applyExportChooserPreferences();
        if (exportUserRulesChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File exportPath = FileExtensionUtils.ensureExtension(
                exportUserRulesChooser.getSelectedFile(), YamlIO.FILE_EXTENSION);
        try {
            YamlIO.save(exportPath, Config.convertRulesOnlyToYamlMap(exportConfig));
            appPreferences.setExportUserRulesDirectory(
                    exportUserRulesChooser.getCurrentDirectory().getAbsolutePath());
            appPreferences.setExportUserRulesFileName(exportPath.getName());
            app.saveAppPreferencesQuietly();
        } catch (IOException error) {
            error.printStackTrace();
            JOptionPane.showMessageDialog(this, error.getMessage(), "Export Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private RulesConfig buildUserAddedExportConfig() {
        Rules rules = session.getRules();
        CardGroup<MonsterCard> cards = session.getReferenceMonsterCards();

        List<MoveExclusionConfig> exclusions = new ArrayList<>();
        for (MoveExclusion exclusion : rules.getMoveExclusions().getAllExclusions()) {
            if (USER_ADDED_SOURCE.equals(exclusion.getSourceFileName())) {
                exclusions.add(MoveExclusionConfig.fromMoveExclusion(exclusion, cards));
            }
        }

        List<MoveAssignmentConfig> assignments = new ArrayList<>();
        for (MoveAssignment assignment : rules.getMoveAssignments().getAllAssignments()) {
            if (USER_ADDED_SOURCE.equals(assignment.getSourceFileName())) {
                assignments.add(MoveAssignmentConfig.fromMoveAssignment(assignment, cards));
            }
        }
        return new RulesConfig(USER_ADDED_SOURCE, exclusions, assignments);
    }

    private JTable createExclusionsTable() {
        JTable table = createTable(exclusionsModel);
        RulesTableColumns.installYesNoColumn(table, EXCLUSION_REMOVE_FROM_POOL_COLUMN);
        RulesTableColumns.installYesNoColumn(table, EXCLUSION_GENERATE_ASSIGNMENTS_COLUMN);
        RulesTableColumns.installRemoveColumn(table, EXCLUSION_REMOVE_COLUMN,
                modelRow -> exclusionsModel.getRow(modelRow) != null, modelRow -> {
            MoveExclusion exclusion = exclusionsModel.getRow(modelRow);
            if (exclusion != null) {
                session.getRules().removeMoveExclusion(exclusion);
                exclusionsModel.removeRow(modelRow);
                assignmentsModel.setRows(buildAssignmentRows());
            }
        });
        configureRowSorter(table, EXCLUSION_REMOVE_COLUMN);
        return table;
    }

    private JTable createAssignmentsTable() {
        JTable table = createTable(assignmentsModel);
        int removeColumnIndex = assignmentsModel.getColumnCount() - 1;
        RulesTableColumns.installRemoveColumn(table, removeColumnIndex, modelRow -> {
            AssignmentRow row = assignmentsModel.getRow(modelRow);
            return row != null && !MoveAssignments.isAssignmentDerivedExclusionSource(
                    row.assignment().getSourceFileName());
        }, modelRow -> {
            AssignmentRow row = assignmentsModel.getRow(modelRow);
            if (row != null && !MoveAssignments.isAssignmentDerivedExclusionSource(
                    row.assignment().getSourceFileName())) {
                session.getRules().getMoveAssignments().removeMatching(row.assignment());
                assignmentsModel.removeRow(modelRow);
            }
        });
        configureRowSorter(table, removeColumnIndex);
        return table;
    }

    private void addExclusion() {
        if (AddMoveExclusionDialog.showDialog(this, session.getRules(),
                session.getReferenceMonsterCards())) {
            refresh();
        }
    }

    private void addAssignment() {
        if (!session.isRomLoaded()) {
            return;
        }
        if (AddMoveAssignmentDialog.showDialog(this, session.getRules(),
                session.getReferenceMonsterCards())) {
            refresh();
        }
    }

    private JPanel createAssignmentsSection(JTable table) {
        JPanel section = new JPanel(new GridBagLayout());
        section.setBorder(BorderFactory.createTitledBorder("Move Assignments"));

        GridBagConstraints contentConstraints = new GridBagConstraints();
        contentConstraints.gridx = 0;
        contentConstraints.gridy = 0;
        contentConstraints.weightx = 1;
        contentConstraints.weighty = 1;
        contentConstraints.fill = GridBagConstraints.BOTH;
        contentConstraints.insets = new Insets(0, 4, 4, 4);

        assignmentsContentLayout = new CardLayout();
        assignmentsContentPanel = new JPanel(assignmentsContentLayout);

        JLabel placeholder = new JLabel(ASSIGNMENTS_NO_ROM_PLACEHOLDER, SwingConstants.CENTER);
        assignmentsContentPanel.add(placeholder, "placeholder");

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(SECTION_SCROLL_WIDTH, SECTION_SCROLL_HEIGHT));
        assignmentsContentPanel.add(scrollPane, "table");
        section.add(assignmentsContentPanel, contentConstraints);

        GridBagConstraints addConstraints = new GridBagConstraints();
        addConstraints.gridx = 0;
        addConstraints.gridy = 1;
        addConstraints.anchor = GridBagConstraints.EAST;
        addConstraints.insets = new Insets(0, 4, 4, 4);

        addAssignmentButton = StructuredGridHelpers.createAddButton(true);
        addAssignmentButton.addActionListener(event -> addAssignment());
        addAssignmentButton.setEnabled(session.isRomLoaded());
        section.add(addAssignmentButton, addConstraints);
        updateAssignmentsContentVisibility();
        return section;
    }

    private JPanel createSection(String title, JTable table, Runnable onAdd) {
        JPanel section = new JPanel(new GridBagLayout());
        section.setBorder(BorderFactory.createTitledBorder(title));

        GridBagConstraints tableConstraints = new GridBagConstraints();
        tableConstraints.gridx = 0;
        tableConstraints.gridy = 0;
        tableConstraints.weightx = 1;
        tableConstraints.weighty = 1;
        tableConstraints.fill = GridBagConstraints.BOTH;
        tableConstraints.insets = new Insets(0, 4, 4, 4);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(SECTION_SCROLL_WIDTH, SECTION_SCROLL_HEIGHT));
        section.add(scrollPane, tableConstraints);

        GridBagConstraints addConstraints = new GridBagConstraints();
        addConstraints.gridx = 0;
        addConstraints.gridy = 1;
        addConstraints.anchor = GridBagConstraints.EAST;
        addConstraints.insets = new Insets(0, 4, 4, 4);

        JButton addButton = StructuredGridHelpers.createAddButton(true);
        addButton.addActionListener(event -> onAdd.run());
        section.add(addButton, addConstraints);
        return section;
    }

    private static JTable createTable(AbstractTableModel model) {
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setRowHeight(24);
        resizeColumns(table);
        return table;
    }

    private static void resizeColumns(JTable table) {
        TableColumnModel columns = table.getColumnModel();
        for (int i = 0; i < columns.getColumnCount() - 1; i++) {
            int width = table.getTableHeader().getFontMetrics(table.getTableHeader().getFont())
                    .stringWidth(table.getColumnName(i)) + 24;
            columns.getColumn(i).setPreferredWidth(width);
        }
    }

    private static void configureRowSorter(JTable table, int removeColumnIndex) {
        TableRowSorter<? extends javax.swing.table.TableModel> sorter =
                new TableRowSorter<>(table.getModel());
        sorter.setSortable(removeColumnIndex, false);
        sorter.setComparator(0, MOVE_NAME_ORDER);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        table.setRowSorter(sorter);
    }

    private static String formatCard(MoveExclusion exclusion, CardGroup<MonsterCard> cards) {
        if (exclusion.hasCardSpecifier()) {
            return exclusion.getCardSpecifier();
        }
        if (!exclusion.isCardIdSet()) {
            return "(any card)";
        }
        if (cards != null) {
            MonsterCard card = cards.withId(exclusion.getCardId());
            if (card != null) {
                return card.toNameWithLevelSpecifier();
            }
        }
        return exclusion.getCardId().toString();
    }

    private static String formatSource(String source) {
        return source == null || source.isBlank() ? "(unknown)" : source;
    }

    private record AssignmentRow(MoveAssignment assignment, String source, String fromCard,
            String toCard, String toMoveSlot, String move) {
    }

    private final class MoveExclusionsTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_HEADERS = { "Move", "Card", "Remove from Pool",
                "Generate Assignments", "Source", "" };

        private List<MoveExclusion> rows = List.of();

        void setRows(List<MoveExclusion> rows) {
            this.rows = new ArrayList<>(rows);
            fireTableDataChanged();
        }

        void updateRow(int index, MoveExclusion row) {
            if (index < 0 || index >= rows.size()) {
                return;
            }
            rows.set(index, row);
            fireTableRowsUpdated(index, index);
        }

        void removeRow(int index) {
            if (index < 0 || index >= rows.size()) {
                return;
            }
            rows.remove(index);
            fireTableRowsDeleted(index, index);
        }

        MoveExclusion getRow(int index) {
            if (index < 0 || index >= rows.size()) {
                return null;
            }
            return rows.get(index);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_HEADERS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_HEADERS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == EXCLUSION_REMOVE_COLUMN) {
                return "";
            }
            MoveExclusion row = rows.get(rowIndex);
            return switch (columnIndex) {
            case 0 -> row.getMoveName();
            case 1 -> formatCard(row, session.getReferenceMonsterCards());
            case 2 -> row.isRemoveFromPool();
            case 3 -> row.isExcludeFromRandomization();
            case 4 -> formatSource(row.getSourceFileName());
            default -> "";
            };
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == EXCLUSION_REMOVE_FROM_POOL_COLUMN
                    || columnIndex == EXCLUSION_GENERATE_ASSIGNMENTS_COLUMN) {
                return Boolean.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == EXCLUSION_REMOVE_FROM_POOL_COLUMN
                    || columnIndex == EXCLUSION_GENERATE_ASSIGNMENTS_COLUMN;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (!(value instanceof Boolean newValue)) {
                return;
            }
            MoveExclusion exclusion = getRow(rowIndex);
            if (exclusion == null) {
                return;
            }
            MoveExclusion updated = exclusion;
            switch (columnIndex) {
            case EXCLUSION_REMOVE_FROM_POOL_COLUMN -> {
                if (exclusion.isRemoveFromPool() != newValue) {
                    updated = exclusion.withRemoveFromPool(newValue);
                }
            }
            case EXCLUSION_GENERATE_ASSIGNMENTS_COLUMN -> {
                if (exclusion.isExcludeFromRandomization() != newValue) {
                    updated = exclusion.withExcludeFromRandomization(newValue);
                }
            }
            default -> {
            }
            }
            if (updated != exclusion) {
                IssueTracker.clearWarnings();
                session.getRules().updateMoveExclusion(exclusion, updated,
                        session.getReferenceMonsterCards());
                updateRow(rowIndex, updated);
                if (columnIndex == EXCLUSION_GENERATE_ASSIGNMENTS_COLUMN) {
                    assignmentsModel.setRows(buildAssignmentRows());
                }
                IssuePresenter.displayWarnings(RulesPanel.this, "updating exclusion");
            }
        }
    }

    private static final class MoveAssignmentsTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_HEADERS = { "Move", "From Card", "To Card",
                "To Move Slot", "Source", "" };

        private List<AssignmentRow> rows = List.of();

        void setRows(List<AssignmentRow> rows) {
            this.rows = new ArrayList<>(rows);
            fireTableDataChanged();
        }

        void removeRow(int index) {
            if (index < 0 || index >= rows.size()) {
                return;
            }
            rows.remove(index);
            fireTableRowsDeleted(index, index);
        }

        AssignmentRow getRow(int index) {
            if (index < 0 || index >= rows.size()) {
                return null;
            }
            return rows.get(index);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_HEADERS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_HEADERS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == getRemoveColumnIndex()) {
                return "";
            }
            AssignmentRow row = rows.get(rowIndex);
            return switch (columnIndex) {
            case 0 -> row.move();
            case 1 -> row.fromCard();
            case 2 -> row.toCard();
            case 3 -> row.toMoveSlot();
            case 4 -> formatSource(row.source());
            default -> "";
            };
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        private static int getRemoveColumnIndex() {
            return COLUMN_HEADERS.length - 1;
        }
    }
}
