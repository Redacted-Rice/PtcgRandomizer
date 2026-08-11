package redactedrice.ptcgr.randomizer.gui.support;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.ptcgr.randomizer.gui.SortableTableHelpers;
import redactedrice.randomizer.lua.Module;

/** Read only list of loaded pre/post scripts. */
public class SupportPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final String TYPE_PRE = "Pre";
    private static final String TYPE_POST = "Post";

    private static final int SECTION_SCROLL_WIDTH = 1000;
    private static final int SECTION_SCROLL_HEIGHT = 360;

    /** What the support tab reads from the running app. */
    interface Session {
        List<Module> getPreScripts();

        List<Module> getPostScripts();
    }

    private final Session session;
    private final ScriptsTableModel scriptsModel;

    public SupportPanel(ActionBank actionBank) {
        this(sessionFrom(actionBank));
    }

    // package private so tests can skip spinning up ActionBank
    SupportPanel(Session session) {
        this.session = session;
        scriptsModel = new ScriptsTableModel();
        createUi();
        refresh();
    }

    private static Session sessionFrom(ActionBank actionBank) {
        return new Session() {
            @Override
            public List<Module> getPreScripts() {
                return actionBank.getPreScripts();
            }

            @Override
            public List<Module> getPostScripts() {
                return actionBank.getPostScripts();
            }
        };
    }

    public void refresh() {
        scriptsModel.setRows(buildRows());
    }

    private List<ScriptRow> buildRows() {
        List<ScriptRow> rows = new ArrayList<>();
        addScriptRows(rows, session.getPreScripts(), TYPE_PRE);
        addScriptRows(rows, session.getPostScripts(), TYPE_POST);
        return rows;
    }

    private static void addScriptRows(List<ScriptRow> rows, List<Module> scripts, String type) {
        if (scripts == null) {
            return;
        }
        for (Module script : scripts) {
            rows.add(new ScriptRow(
                    nullToEmpty(script.getName()),
                    nullToEmpty(script.getId()),
                    nullToEmpty(script.getVersion()),
                    type,
                    nullToEmpty(script.getWhen()),
                    nullToEmpty(script.getDescription())));
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void createUi() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JTable table = createScriptsTable();
        JPanel section = createSection("Scripts", table);
        add(section, BorderLayout.CENTER);
    }

    private JTable createScriptsTable() {
        // same hover description pattern as ActionsHoverToolTipTable
        JTable table = new JTable(scriptsModel) {
            private static final long serialVersionUID = 1L;

            @Override
            public String getToolTipText(MouseEvent event) {
                int viewRow = rowAtPoint(event.getPoint());
                if (viewRow < 0) {
                    return null;
                }
                int modelRow = convertRowIndexToModel(viewRow);
                String description = scriptsModel.getRowDescription(modelRow);
                if (description == null || description.isBlank()) {
                    return null;
                }
                return description;
            }
        };
        // match Actions tables: dont fill empty viewport space or tip timing feels slower
        // when the mouse crosses from blank area onto a row
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setRowHeight(24);
        SortableTableHelpers.resizeColumnsFromHeaders(table, false);
        SortableTableHelpers.configureRowSorter(table, 0);
        return table;
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
        scrollPane.setPreferredSize(new Dimension(SECTION_SCROLL_WIDTH, SECTION_SCROLL_HEIGHT));
        section.add(scrollPane, tableConstraints);
        return section;
    }

    private record ScriptRow(String name, String id, String version, String type, String when,
            String description) {
    }

    private static final class ScriptsTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_HEADERS = { "Name", "Id", "Version", "Type", "When" };

        private List<ScriptRow> rows = List.of();

        void setRows(List<ScriptRow> rows) {
            this.rows = new ArrayList<>(rows);
            fireTableDataChanged();
        }

        String getRowDescription(int row) {
            if (row < 0 || row >= rows.size()) {
                return "";
            }
            return rows.get(row).description();
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
            ScriptRow row = rows.get(rowIndex);
            return switch (columnIndex) {
            case 0 -> row.name();
            case 1 -> row.id();
            case 2 -> row.version();
            case 3 -> row.type();
            case 4 -> row.when();
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
