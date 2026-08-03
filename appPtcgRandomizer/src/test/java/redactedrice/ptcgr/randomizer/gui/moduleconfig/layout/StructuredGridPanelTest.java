package redactedrice.ptcgr.randomizer.gui.moduleconfig.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.StructuredGridTestSupport.*;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;

import org.junit.jupiter.api.Test;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.support.ModuleConfigGuiTestSupport;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.StructuredGridModel;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.StructuredGridPanel;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.StructuredGridTestSupport;
import redactedrice.randomizer.lua.arguments.TypeDefinition;

public class StructuredGridPanelTest extends ModuleConfigGuiTestSupport {

    private static StructuredGridPanel stringListEditor() {
        return new StructuredGridPanel(TypeDefinition.listOf(TypeDefinition.string()), null);
    }

    private static StructuredGridPanel nestedIntListEditor() {
        return new StructuredGridPanel(
                TypeDefinition.listOf(TypeDefinition.listOf(TypeDefinition.integer())), null);
    }

    private static StructuredGridPanel stringIntTableEditor() {
        return new StructuredGridPanel(
                TypeDefinition.tableOf(TypeDefinition.string(), TypeDefinition.integer()), null);
    }

    private static StructuredGridPanel intListEditor() {
        return new StructuredGridPanel(TypeDefinition.listOf(TypeDefinition.integer()), null);
    }

    @Test
    void invalidInputBlocksStructuralChanges() {
        StructuredGridPanel editor = intListEditor();
        editor.setValue(List.of(1, 2, 3));

        List<JTextField> fields = findComponents(editor.getComponent(), JTextField.class);
        fields.get(0).setText("");
        findAddButton(editor.getComponent()).doClick();
        assertEquals(3, findComponents(editor.getComponent(), JTextField.class).size());

        findRemoveButtons(editor.getComponent()).get(2).doClick();
        assertEquals(3, findComponents(editor.getComponent(), JTextField.class).size());
    }

    @Test
    void listRowsAreLiveEditableAndAddAppendsRows() {
        StructuredGridPanel editor = stringListEditor();
        editor.setValue(List.of("a", "b"));

        List<JTextField> fields = findComponents(editor.getComponent(), JTextField.class);
        assertEquals(2, fields.size());
        fields.get(0).setText("edited-a");
        fields.get(1).setText("edited-b");
        assertEquals(List.of("edited-a", "edited-b"), editor.getValue());

        findAddButton(editor.getComponent()).doClick();
        assertEquals(List.of("edited-a", "edited-b", ""), editor.getValue());

        fields = findComponents(editor.getComponent(), JTextField.class);
        assertEquals(3, fields.size());
        fields.get(2).setText("c");
        assertEquals(List.of("edited-a", "edited-b", "c"), editor.getValue());
    }

    @Test
    void tableRowsAreLiveEditableAndAddAppendsRows() {
        StructuredGridPanel editor = stringIntTableEditor();
        editor.setValue(linkedMap("fire", 10, "water", 5));

        List<JTextField> fields = findComponents(editor.getComponent(), JTextField.class);
        assertEquals(4, fields.size());
        fields.get(0).setText("earth");
        fields.get(1).setText("99");
        assertEquals(linkedMap("earth", 99, "water", 5), editor.getValue());

        findAddButton(editor.getComponent()).doClick();
        fields = findComponents(editor.getComponent(), JTextField.class);
        assertEquals(6, fields.size());
        fields.get(4).setText("grass");
        fields.get(5).setText("3");
        assertEquals(linkedMap("earth", 99, "water", 5, "grass", 3), editor.getValue());
    }

    @Test
    void listRemoveAndEmptyBehavior() {
        StructuredGridPanel editor = stringListEditor();
        editor.setValue(List.of("a", "b", "c"));
        findRemoveButtons(editor.getComponent()).get(1).doClick();
        assertEquals(List.of("a", "c"), editor.getValue());

        editor.setValue(List.of());
        assertTrue(((List<?>) editor.getValue()).isEmpty());
        assertEquals(0, findComponents(editor.getComponent(), JTextField.class).size());
    }

    @Test
    void tableRemoveDuplicateKeysAndEmptyBehavior() {
        StructuredGridPanel editor = stringIntTableEditor();
        editor.setValue(linkedMap("a", 1, "b", 2, "c", 3));
        findRemoveButtons(editor.getComponent()).get(1).doClick();
        assertEquals(linkedMap("a", 1, "c", 3), editor.getValue());

        editor.setValue(linkedMap("dup", 1));
        findAddButton(editor.getComponent()).doClick();
        List<JTextField> fields = findComponents(editor.getComponent(), JTextField.class);
        fields.get(2).setText("dup");
        fields.get(3).setText("2");
        assertThrows(IllegalArgumentException.class, editor::getValue);

        editor.setValue(linkedMap("a", 1));
        findAddButton(editor.getComponent()).doClick();
        fields = findComponents(editor.getComponent(), JTextField.class);
        assertThrows(IllegalArgumentException.class, editor::getValue);

        editor.setValue(new LinkedHashMap<String, Integer>());
        assertEquals(0, gridBagConstraintsOf(findAddButton(editor.getComponent())).gridy);
    }

    @Test
    void setEditableFalseHidesAddAndRemoveButtons() {
        StructuredGridPanel listEditor = stringListEditor();
        listEditor.setValue(List.of("a"));
        listEditor.setEditable(false);
        for (JButton button : findRemoveButtons(listEditor.getComponent())) {
            assertFalse(button.isVisible());
        }
        assertFalse(findAddButton(listEditor.getComponent()).isVisible());

        StructuredGridPanel tableEditor = stringIntTableEditor();
        tableEditor.setValue(linkedMap("a", 1));
        tableEditor.setEditable(false);
        for (JButton button : findRemoveButtons(tableEditor.getComponent())) {
            assertFalse(button.isVisible());
        }
        assertFalse(findAddButton(tableEditor.getComponent()).isVisible());
    }

    @Test
    void nestedListRoundTripsAndSupportsOuterRemove() {
        StructuredGridPanel editor = nestedIntListEditor();
        editor.setValue(List.of(List.of(1, 2), List.of(3)));
        assertEquals(List.of(List.of(1, 2), List.of(3)), editor.getValue());

        findRemoveButtons(editor.getComponent()).stream()
                .filter(StructuredGridTestSupport::isFramedRemoveButton).findFirst().orElseThrow()
                .doClick();
        assertEquals(List.of(List.of(3)), editor.getValue());
    }

    @Test
    void nestedTableRoundTripsAndSpansNestedRows() {
        StructuredGridPanel editor = new StructuredGridPanel(
                TypeDefinition.tableOf(TypeDefinition.string(),
                        TypeDefinition.tableOf(TypeDefinition.string(), TypeDefinition.integer())),
                null);
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("groupA", linkedMap("x", 1, "y", 2));
        editor.setValue(value);
        assertEquals(value, editor.getValue());

        List<JButton> removeButtons = findRemoveButtons(editor.getComponent());
        assertEquals(3, removeButtons.size());
        List<JButton> outerRemoveButtons = removeButtons.stream()
                .filter(StructuredGridTestSupport::isFramedRemoveButton).toList();
        assertEquals(1, outerRemoveButtons.size());
        assertEquals(3, gridBagConstraintsOf(outerRemoveButtons.get(0)).gridheight);
        assertEquals(2, removeButtons.stream().filter(button -> !isFramedRemoveButton(button))
                .filter(button -> gridBagConstraintsOf(button).gridheight == 1).count());
    }

    @Test
    void nestedListUsesExpectedGridLayout() {
        TypeDefinition rootType =
                TypeDefinition.listOf(TypeDefinition.listOf(TypeDefinition.integer()));
        StructuredGridPanel editor = new StructuredGridPanel(rootType, null);
        editor.setValue(List.of(List.of(1, 2, 3), List.of(4)));

        List<JButton> removeButtons = findRemoveButtons(editor.getComponent());
        assertEquals(6, removeButtons.size());

        List<JButton> outerRemoveButtons = removeButtons.stream()
                .filter(StructuredGridTestSupport::isFramedRemoveButton).toList();
        assertEquals(2, outerRemoveButtons.size());
        assertEquals(4, gridBagConstraintsOf(outerRemoveButtons.get(0)).gridheight);
        assertEquals(2, gridBagConstraintsOf(outerRemoveButtons.get(1)).gridheight);
        assertEquals(4, removeButtons.stream().filter(button -> !isFramedRemoveButton(button))
                .filter(button -> gridBagConstraintsOf(button).gridheight == 1).count());

        JButton removeButton = removeButtons.stream()
                .filter(button -> !isFramedRemoveButton(button)).findFirst().orElseThrow();
        JComponent valueField = (JComponent) findComponents(editor.getComponent(), JTextField.class)
                .get(0).getParent();
        assertTrue(
                gridBagConstraintsOf(removeButton).gridx > gridBagConstraintsOf(valueField).gridx);
        assertEquals(GridBagConstraints.CENTER, gridBagConstraintsOf(removeButton).anchor);

        editor.setValue(List.of(List.of(1)));
        int totalColumns = StructuredGridModel.totalColumns(rootType);
        JButton outerRemove = findRemoveButtons(editor.getComponent()).stream()
                .filter(StructuredGridTestSupport::isFramedRemoveButton).findFirst().orElseThrow();
        assertEquals(totalColumns - 1, gridBagConstraintsOf(outerRemove).gridx);

        List<JButton> addButtons = findAddButtons(editor.getComponent());
        assertEquals(2, addButtons.size());
        assertEquals(1, gridBagConstraintsOf(addButtons.get(0)).gridx);
        assertEquals(1, gridBagConstraintsOf(addButtons.get(0)).gridwidth);

        GridBagConstraints outerAddConstraints = gridBagConstraintsOf(addButtons.get(1));
        assertEquals(0, outerAddConstraints.gridx);
        assertEquals(2, outerAddConstraints.gridwidth);
        assertEquals(GridBagConstraints.NONE, outerAddConstraints.fill);
        assertEquals(GridBagConstraints.WEST, outerAddConstraints.anchor);
    }

    @Test
    void tableRemoveButtonSitsRightOfKeyAndValueColumns() {
        StructuredGridPanel editor = stringIntTableEditor();
        editor.setValue(linkedMap("fire", 10));

        JButton removeButton = findRemoveButtons(editor.getComponent()).get(0);
        List<JTextField> fields = findComponents(editor.getComponent(), JTextField.class);
        int removeGridx = gridBagConstraintsOf(removeButton).gridx;
        int keyGridx = gridBagConstraintsOf((JComponent) fields.get(0).getParent()).gridx;
        int valueGridx = gridBagConstraintsOf((JComponent) fields.get(1).getParent()).gridx;

        assertTrue(removeGridx > valueGridx);
        assertTrue(keyGridx < valueGridx);
        assertEquals(GridBagConstraints.CENTER, gridBagConstraintsOf(removeButton).anchor);
    }

    @Test
    void listWideningGrowsLeafValueColumns() {
        StructuredGridPanel flatEditor = stringListEditor();
        flatEditor.setValue(List.of("a", "b"));
        JComponent flatRoot = (JComponent) flatEditor.getComponent();
        Dimension flatPreferred = flatRoot.getPreferredSize();
        flatRoot.setSize(new Dimension(flatPreferred.width + 1000, flatPreferred.height));
        layoutFully(flatRoot);

        JButton removeButton = findRemoveButtons(flatRoot).get(0);
        JTextField field = findComponents(flatRoot, JTextField.class).get(0);
        assertTrue(field.getWidth() > flatPreferred.width - 50);
        assertEquals(removeButton.getPreferredSize().width, removeButton.getWidth());

        StructuredGridPanel nestedEditor = nestedIntListEditor();
        nestedEditor.setValue(List.of(List.of(1, 2)));
        JComponent nestedRoot = (JComponent) nestedEditor.getComponent();
        nestedRoot.setSize(new Dimension(400, nestedRoot.getPreferredSize().height));
        layoutFully(nestedRoot);
        int narrowWidth = findComponents(nestedRoot, JTextField.class).get(0).getWidth();

        nestedRoot.setSize(new Dimension(1400, nestedRoot.getPreferredSize().height));
        layoutFully(nestedRoot);
        int wideWidth = findComponents(nestedRoot, JTextField.class).get(0).getWidth();
        assertTrue(wideWidth > narrowWidth);
    }

    @Test
    void tableWideningSharesExtraWidthAcrossKeyAndValueColumns() {
        StructuredGridPanel editor = stringIntTableEditor();
        editor.setValue(linkedMap("fire", 10, "water", 5));

        JComponent root = (JComponent) editor.getComponent();
        Dimension preferred = root.getPreferredSize();
        root.setSize(new Dimension(preferred.width + 1000, preferred.height));
        layoutFully(root);

        JButton removeButton = findRemoveButtons(root).get(0);
        List<JTextField> fields = findComponents(root, JTextField.class);
        JTextField keyField = fields.get(0);
        JTextField valueField = fields.get(1);

        assertEquals(removeButton.getPreferredSize().width, removeButton.getWidth());
        assertTrue(keyField.getWidth() > preferred.width / 2);
        assertTrue(valueField.getWidth() > preferred.width / 2);
        assertTrue(Math.abs(keyField.getWidth() - valueField.getWidth()) < 20);

        StructuredGridPanel stringTableEditor = new StructuredGridPanel(
                TypeDefinition.tableOf(TypeDefinition.string(), TypeDefinition.string()), null);
        LinkedHashMap<String, String> value = new LinkedHashMap<>();
        value.put("fire", "a");
        value.put(
                "waterwaterwaterwaterwaterwaterwaterwaterwaterwaterwaterwaterwaterwaterwaterwater",
                "bb");
        stringTableEditor.setValue(value);
        root = (JComponent) stringTableEditor.getComponent();
        root.setSize(root.getPreferredSize());
        layoutFully(root);

        fields = findComponents(root, JTextField.class);
        assertEquals(fields.get(2).getWidth(), fields.get(0).getWidth());
        assertEquals(fields.get(1).getWidth(), fields.get(3).getWidth());
    }

    @Test
    void nestedCollectionsShowSeparatorsAboveAdd() {
        StructuredGridPanel flatListEditor = stringListEditor();
        flatListEditor.setValue(List.of("a", "b"));
        assertEquals(0, countHorizontalSeparators(flatListEditor.getComponent()));

        StructuredGridPanel emptyListEditor = stringListEditor();
        emptyListEditor.setValue(List.of());
        assertEquals(0, gridBagConstraintsOf(findAddButton(emptyListEditor.getComponent())).gridy);

        StructuredGridPanel nestedListEditor = nestedIntListEditor();
        nestedListEditor.setValue(List.of(List.of(1)));
        assertTrue(separatorAboveAdd((JComponent) nestedListEditor.getComponent()));

        StructuredGridPanel flatTableEditor = stringIntTableEditor();
        flatTableEditor.setValue(linkedMap("fire", 10, "water", 5));
        assertEquals(0, countHorizontalSeparators(flatTableEditor.getComponent()));

        StructuredGridPanel nestedTableEditor =
                new StructuredGridPanel(TypeDefinition.tableOf(TypeDefinition.string(),
                        TypeDefinition.listOf(TypeDefinition.integer())), null);
        nestedTableEditor.setValue(Map.of("fire", List.of(1)));
        assertTrue(separatorAboveAdd((JComponent) nestedTableEditor.getComponent()));
    }

    private static boolean separatorAboveAdd(JComponent root) {
        int addRow = gridBagConstraintsOf(findAddButton(root)).gridy;
        for (java.awt.Component child : root.getComponents()) {
            GridBagConstraints constraints = gridBagConstraintsOf(child);
            if (constraints.gridy == addRow - 1 && constraints.gridx == 0
                    && isHorizontalSeparator(child)) {
                return true;
            }
        }
        return false;
    }

    private static LinkedHashMap<String, Integer> linkedMap(Object... keysAndValues) {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        for (int index = 0; index < keysAndValues.length; index += 2) {
            map.put((String) keysAndValues[index], (Integer) keysAndValues[index + 1]);
        }
        return map;
    }
}
