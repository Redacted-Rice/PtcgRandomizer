package redactedrice.ptcgr.randomizer.gui;

import java.util.Comparator;
import java.util.List;

import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/** Shared Swing bits for read mostly tables with clickable column headers. */
public final class SortableTableHelpers {
    private static final Comparator<String> CASE_INSENSITIVE = String.CASE_INSENSITIVE_ORDER;

    private SortableTableHelpers() {}

    public static JTable createTable(AbstractTableModel model) {
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setRowHeight(24);
        return table;
    }

    // Prefer sizing from header text so narrow columns dont start crushed
    public static void resizeColumnsFromHeaders(JTable table, boolean skipLastColumn) {
        TableColumnModel columns = table.getColumnModel();
        int lastIndex = skipLastColumn ? columns.getColumnCount() - 1 : columns.getColumnCount();
        for (int i = 0; i < lastIndex; i++) {
            int width = table.getTableHeader().getFontMetrics(table.getTableHeader().getFont())
                    .stringWidth(table.getColumnName(i)) + 24;
            columns.getColumn(i).setPreferredWidth(width);
        }
    }

    public static void configureRowSorter(JTable table, int primarySortColumn,
            int... unsortableColumns) {
        TableRowSorter<? extends TableModel> sorter = new TableRowSorter<>(table.getModel());
        for (int column : unsortableColumns) {
            sorter.setSortable(column, false);
        }
        TableModel model = table.getModel();
        for (int column = 0; column < model.getColumnCount(); column++) {
            if (model.getColumnClass(column) == String.class) {
                sorter.setComparator(column, CASE_INSENSITIVE);
            }
        }
        sorter.setSortKeys(List.of(new RowSorter.SortKey(primarySortColumn, SortOrder.ASCENDING)));
        table.setRowSorter(sorter);
    }
}
