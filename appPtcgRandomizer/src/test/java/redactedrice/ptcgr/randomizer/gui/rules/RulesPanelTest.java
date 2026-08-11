package redactedrice.ptcgr.randomizer.gui.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.ptcgr.configs.AppPreferences;
import redactedrice.ptcgr.constants.romenums.CardId;
import redactedrice.ptcgr.rules.MoveExclusion;
import redactedrice.ptcgr.randomizer.RandomizerCore;

class RulesPanelTest {
    private static RandomizerCore randomizerCore;

    @BeforeAll
    static void loadRandomizerCore() {
        randomizerCore = new RandomizerCore(new JPanel());
    }

    @BeforeEach
    void resetRules() {
        randomizerCore.resetRulesToBundledDefaults(null);
    }

    @Test
    void withoutRomShowsAssignmentsPlaceholderAndDisablesAdd() {
        RulesPanel panel = newPanel();
        panel.refresh();

        assertNotNull(findLabelWithText(panel,
                "Open a ROM to view and edit move assignments."));
        assertFalse(addAssignmentButton(panel).isEnabled());
    }

    @Test
    void refreshListsBundledExclusions() {
        RulesPanel panel = newPanel();
        panel.refresh();

        assertTrue(exclusionTable(panel).getRowCount() > 0);
    }

    @Test
    void refreshReflectsUserAddedExclusion() {
        RulesPanel panel = newPanel();
        int before = randomizerCore.getRules().getMoveExclusions().getAllExclusions().size();

        randomizerCore.getRules().addMoveExclusion(
                new MoveExclusion(CardId.NO_CARD, "UserOnlyMove", true, false,
                        RulesPanel.USER_ADDED_SOURCE),
                null);
        panel.refresh();

        assertEquals(before + 1, exclusionTable(panel).getRowCount());
    }

    @Test
    void exportChooserPreferencesUseAppPreferences() throws Exception {
        AppPreferences prefs = AppPreferences.loadDefaults();
        RulesPanel[] holder = new RulesPanel[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new RulesPanel(randomizerCore, prefs, null));
        RulesPanel panel = holder[0];
        panel.applyExportChooserPreferences();

        assertNotNull(panel.getExportUserRulesSelectedFile());
        assertEquals(prefs.resolveExportUserRulesFile().getName(),
                panel.getExportUserRulesSelectedFile().getName());
    }

    private static RulesPanel newPanel() {
        try {
            RulesPanel[] holder = new RulesPanel[1];
            SwingUtilities.invokeAndWait(() -> holder[0] =
                    new RulesPanel(randomizerCore, AppPreferences.loadDefaults(), null));
            return holder[0];
        } catch (Exception error) {
            throw new AssertionError(error);
        }
    }

    private static JButton addAssignmentButton(RulesPanel panel) {
        try {
            var field = RulesPanel.class.getDeclaredField("addAssignmentButton");
            field.setAccessible(true);
            return (JButton) field.get(panel);
        } catch (ReflectiveOperationException error) {
            throw new AssertionError(error);
        }
    }

    private static JTable exclusionTable(RulesPanel panel) {
        List<JTable> tables = findTables(panel);
        assertFalse(tables.isEmpty());
        return tables.get(0);
    }

    private static List<JTable> findTables(Container root) {
        List<JTable> tables = new ArrayList<>();
        collectComponents(root, JTable.class, tables);
        return tables;
    }

    private static JLabel findLabelWithText(Container root, String text) {
        for (Component component : findComponents(root, JLabel.class)) {
            if (text.equals(((JLabel) component).getText())) {
                return (JLabel) component;
            }
        }
        return null;
    }

    private static <T extends Component> List<T> findComponents(Container root, Class<T> type) {
        List<T> matches = new ArrayList<>();
        collectComponents(root, type, matches);
        return matches;
    }

    private static <T extends Component> void collectComponents(Container root, Class<T> type,
            List<T> matches) {
        for (Component child : root.getComponents()) {
            if (type.isInstance(child)) {
                matches.add(type.cast(child));
            }
            if (child instanceof JScrollPane scrollPane) {
                Component view = scrollPane.getViewport().getView();
                if (type.isInstance(view)) {
                    matches.add(type.cast(view));
                }
            }
            if (child instanceof Container) {
                collectComponents((Container) child, type, matches);
            }
        }
    }
}
