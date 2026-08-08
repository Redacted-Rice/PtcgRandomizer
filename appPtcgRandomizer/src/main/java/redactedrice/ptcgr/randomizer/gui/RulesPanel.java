package redactedrice.ptcgr.randomizer.gui.rules;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import redactedrice.ptcgr.configs.rules.MoveExclusionConfig;
import redactedrice.ptcgr.configs.rules.RulesConfig;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.randomizer.RandomizerCore;
import redactedrice.ptcgr.rules.MoveAssignment;
import redactedrice.ptcgr.rules.MoveAssignments;

/** The active move exclusion and assignment rules. */
public class RulesPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final RandomizerCore randomizerCore;
    private final MoveExclusionsTableModel exclusionsModel;
    private final MoveAssignmentsTableModel assignmentsModel;

    public RulesPanel(RandomizerCore randomizerCore) {
        this.randomizerCore = randomizerCore;
        exclusionsModel = new MoveExclusionsTableModel();
        assignmentsModel = new MoveAssignmentsTableModel();
        createUi();
        refresh();
    }

    public void refresh() {
        RulesConfig rules = randomizerCore.getPendingRules();
        exclusionsModel.setRows(rules.getMoveExclusionConfigs());

        if (randomizerCore.isRomLoaded()) {
            assignmentsModel.setRows(buildAssignmentRows());
        } else {
            assignmentsModel.setRows(List.of());
        }
    }

    private List<AssignmentRow> buildAssignmentRows() {
        CardGroup<MonsterCard> cards = randomizerCore.getOriginalMonsterCards();
        if (cards == null) {
            return List.of();
        }

        List<AssignmentRow> rows = new ArrayList<>();
        for (MoveAssignment assignment : randomizerCore.getMoveAssignments()) {
            MonsterCard card = cards.withId(assignment.getCardId());
            String toCard = card != null ? card.toNameWithLevelSpecifier()
                    : assignment.getCardId().toString();
            rows.add(new AssignmentRow(
                    MoveAssignments.exclusionSourceLabel(assignment.getSourceFileName()), toCard,
                    String.valueOf(assignment.getMoveSlot() + 1),
                    assignment.getMove().name.toString()));
        }
        return rows;
    }

    private void createUi() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JTable exclusionsTable = createTable(exclusionsModel);
        JTable assignmentsTable = createTable(assignmentsModel);

        JPanel exclusionsSection = createSection("Move Exclusions", exclusionsTable);
        JPanel assignmentsSection = createSection("Move Assignments", assignmentsTable);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, exclusionsSection,
                assignmentsSection);
        splitPane.setResizeWeight(0.65);
        splitPane.setContinuousLayout(true);
        add(splitPane, BorderLayout.CENTER);
    }

    private static JPanel createSection(String title, JTable table) {
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
        scrollPane.setPreferredSize(new Dimension(400, 180));
        section.add(scrollPane, tableConstraints);
        return section;
    }

    private static JTable createTable(AbstractTableModel model) {
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
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

    private static String formatFlag(boolean value) {
        return value ? "Yes" : "No";
    }

    private static String formatCard(String card) {
        return card == null || card.isBlank() ? "(any card)" : card;
    }

    private static String formatSource(String source) {
        return source == null || source.isBlank() ? "(unknown)" : source;
    }

    private record AssignmentRow(String source, String toCard, String toMoveSlot, String move) {
    }

    private static final class MoveExclusionsTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_HEADERS = { "Move", "Card", "Remove from Pool",
                "Exclude from Randomization", "Source" };

        private List<MoveExclusionConfig> rows = List.of();

        void setRows(List<MoveExclusionConfig> rows) {
            this.rows = List.copyOf(rows);
            fireTableDataChanged();
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
            MoveExclusionConfig row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.getMove();
                case 1 -> formatCard(row.getCard());
                case 2 -> formatFlag(row.isRemoveFromPool());
                case 3 -> formatFlag(row.isExcludeFromRandomization());
                case 4 -> formatSource(row.getSourceLabel());
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
    }

    private static final class MoveAssignmentsTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_HEADERS = { "Move", "To Card", "To Move Slot",
                "Source" };

        private List<AssignmentRow> rows = List.of();

        void setRows(List<AssignmentRow> rows) {
            this.rows = List.copyOf(rows);
            fireTableDataChanged();
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
            AssignmentRow row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.move();
                case 1 -> row.toCard();
                case 2 -> row.toMoveSlot();
                case 3 -> formatSource(row.source());
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
    }
}
