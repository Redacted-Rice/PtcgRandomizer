package redactedrice.ptcgr.randomizer.gui.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import org.junit.jupiter.api.Test;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

import redactedrice.randomizer.lua.Module;

class SupportPanelTest {
    @Test
    void refreshListsPreAndPostScriptsWithColumns() {
        StubSession session = new StubSession();
        session.preScripts.add(script("setup", "Setup Script", "1.0", "Runs before randomization"));
        session.postScripts.add(script("detect", "Detect Script", "2.1", "Runs after modules"));

        SupportPanel panel = newPanel(session);
        panel.refresh();

        JTable table = scriptsTable(panel);
        assertEquals(2, table.getRowCount());
        assertEquals(4, table.getColumnCount());
        assertEquals("Name", table.getColumnName(0));
        assertEquals("Id", table.getColumnName(1));
        assertEquals("Version", table.getColumnName(2));
        assertEquals("Type", table.getColumnName(3));

        assertEquals("Setup Script", valueAt(table, "setup", 0));
        assertEquals("setup", valueAt(table, "setup", 1));
        assertEquals("1.0", valueAt(table, "setup", 2));
        assertEquals("Pre", valueAt(table, "setup", 3));

        assertEquals("Detect Script", valueAt(table, "detect", 0));
        assertEquals("detect", valueAt(table, "detect", 1));
        assertEquals("2.1", valueAt(table, "detect", 2));
        assertEquals("Post", valueAt(table, "detect", 3));
    }

    @Test
    void hoverShowsDescriptionTooltip() {
        StubSession session = new StubSession();
        session.preScripts.add(script("setup", "Setup Script", "1.0", "Runs before randomization"));

        SupportPanel panel = newPanel(session);
        JTable table = scriptsTable(panel);

        java.awt.Rectangle cell = table.getCellRect(0, 0, false);
        String tip = table.getToolTipText(
                new java.awt.event.MouseEvent(table, 0, 0, 0, cell.x + 1, cell.y + 1, 0, false));
        assertEquals("Runs before randomization", tip);
    }

    @Test
    void tableIsReadOnlyAndSortable() {
        StubSession session = new StubSession();
        session.preScripts.add(script("b_script", "B", "1.0", ""));
        session.preScripts.add(script("a_script", "A", "1.0", ""));

        SupportPanel panel = newPanel(session);
        JTable table = scriptsTable(panel);

        assertFalse(table.isCellEditable(0, 0));
        assertNotNull(table.getRowSorter());
        assertFalse(table.getRowSorter().getSortKeys().isEmpty());
        assertEquals(0, table.getRowSorter().getSortKeys().get(0).getColumn());
        assertEquals(SortOrder.ASCENDING,
                table.getRowSorter().getSortKeys().get(0).getSortOrder());
    }

    private static SupportPanel newPanel(StubSession session) {
        try {
            SupportPanel[] holder = new SupportPanel[1];
            SwingUtilities.invokeAndWait(() -> holder[0] = new SupportPanel(session));
            return holder[0];
        } catch (Exception error) {
            throw new AssertionError(error);
        }
    }

    private static JTable scriptsTable(SupportPanel panel) {
        List<JTable> tables = new ArrayList<>();
        collectComponents(panel, JTable.class, tables);
        assertFalse(tables.isEmpty());
        return tables.get(0);
    }

    private static Object valueAt(JTable table, String id, int column) {
        TableModel model = table.getModel();
        for (int row = 0; row < model.getRowCount(); row++) {
            if (id.equals(model.getValueAt(row, 1))) {
                return model.getValueAt(row, column);
            }
        }
        throw new AssertionError("No row with id " + id);
    }

    private static <T extends Component> void collectComponents(Container root, Class<T> type,
            List<T> out) {
        for (Component component : root.getComponents()) {
            if (type.isInstance(component)) {
                out.add(type.cast(component));
            }
            if (component instanceof Container container) {
                collectComponents(container, type, out);
            }
        }
    }

    private static Module script(String id, String name, String version, String description) {
        return new Module(id, name, description, Set.of(), Set.of(), List.of(),
                new ZeroArgFunction() {
                    @Override
                    public LuaValue call() {
                        return LuaValue.NIL;
                    }
                }, null, "test.lua", 0, false, false, "randomize", "author", version, Map.of(), null,
                null, null, null, null);
    }

    private static final class StubSession implements SupportPanel.Session {
        private final List<Module> preScripts = new ArrayList<>();
        private final List<Module> postScripts = new ArrayList<>();

        @Override
        public List<Module> getPreScripts() {
            return preScripts;
        }

        @Override
        public List<Module> getPostScripts() {
            return postScripts;
        }
    }
}
