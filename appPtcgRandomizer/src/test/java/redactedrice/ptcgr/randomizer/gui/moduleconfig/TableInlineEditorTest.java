package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static redactedrice.ptcgr.randomizer.gui.moduleconfig.StructuredInlineEditorTestSupport.findAddButton;
import static redactedrice.ptcgr.randomizer.gui.moduleconfig.StructuredInlineEditorTestSupport.findComponents;
import static redactedrice.ptcgr.randomizer.gui.moduleconfig.StructuredInlineEditorTestSupport.findEntryRemoveButtons;
import static redactedrice.ptcgr.randomizer.gui.moduleconfig.StructuredInlineEditorTestSupport.findRemoveButtons;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.junit.jupiter.api.Test;

import redactedrice.randomizer.lua.arguments.TypeDefinition;

class TableInlineEditorTest {

    @Test
    void everyRowIncludingPreexistingOnesIsLiveEditable() {
        TableInlineEditor editor =
                new TableInlineEditor(TypeDefinition.string(), TypeDefinition.integer(), null);
        editor.setValue(linkedMap("fire", 10, "water", 5));

        List<JTextField> fields = findComponents(editor.getComponent(), JTextField.class);
        assertEquals(4, fields.size());

        fields.get(0).setText("earth");
        fields.get(1).setText("99");
        assertEquals(linkedMap("earth", 99, "water", 5), editor.getValue());
    }

    @Test
    void addButtonAppendsAnEditableRow() {
        TableInlineEditor editor =
                new TableInlineEditor(TypeDefinition.string(), TypeDefinition.integer(), null);
        editor.setValue(linkedMap("fire", 10));

        findAddButton(editor.getComponent()).doClick();

        List<JTextField> fields = findComponents(editor.getComponent(), JTextField.class);
        assertEquals(4, fields.size());
        fields.get(2).setText("water");
        fields.get(3).setText("5");
        assertEquals(linkedMap("fire", 10, "water", 5), editor.getValue());
    }

    @Test
    void removeButtonDropsOnlyItsOwnRow() {
        TableInlineEditor editor =
                new TableInlineEditor(TypeDefinition.string(), TypeDefinition.integer(), null);
        editor.setValue(linkedMap("a", 1, "b", 2, "c", 3));

        List<JButton> removeButtons = findEntryRemoveButtons(editor.getComponent());
        assertEquals(3, removeButtons.size());

        removeButtons.get(1).doClick();
        assertEquals(linkedMap("a", 1, "c", 3), editor.getValue());
    }

    @Test
    void duplicateKeysAreRejectedOnGetValue() {
        TableInlineEditor editor =
                new TableInlineEditor(TypeDefinition.string(), TypeDefinition.integer(), null);
        editor.setValue(linkedMap("dup", 1));

        findAddButton(editor.getComponent()).doClick();

        List<JTextField> fields = findComponents(editor.getComponent(), JTextField.class);
        fields.get(2).setText("dup");
        fields.get(3).setText("2");

        assertThrows(IllegalArgumentException.class, editor::getValue);
    }

    @Test
    void nestedTableHasBorderAtDepthGreaterThanZero() {
        TableInlineEditor topLevel =
                new TableInlineEditor(TypeDefinition.string(), TypeDefinition.integer(), null, 0);
        assertNull(topLevel.getComponent().getBorder());

        TableInlineEditor nested =
                new TableInlineEditor(TypeDefinition.string(), TypeDefinition.integer(), null, 1);
        assertNotNull(nested.getComponent().getBorder());
    }

    @Test
    void nestedTableRoundTrips() {
        TableInlineEditor editor = new TableInlineEditor(TypeDefinition.string(),
                TypeDefinition.tableOf(TypeDefinition.string(), TypeDefinition.integer()), null);
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("groupA", Map.of("x", 1));
        editor.setValue(value);

        assertEquals(value, editor.getValue());
    }

    @Test
    void setEditableFalseHidesAddAndRemoveButtons() {
        TableInlineEditor editor =
                new TableInlineEditor(TypeDefinition.string(), TypeDefinition.integer(), null);
        editor.setValue(linkedMap("a", 1));

        editor.setEditable(false);

        for (JButton button : findRemoveButtons(editor.getComponent())) {
            assertFalse(button.isVisible());
        }
        assertFalse(findAddButton(editor.getComponent()).isVisible());
    }

    private static LinkedHashMap<String, Integer> linkedMap(Object... keysAndValues) {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        for (int index = 0; index < keysAndValues.length; index += 2) {
            map.put((String) keysAndValues[index], (Integer) keysAndValues[index + 1]);
        }
        return map;
    }
}
