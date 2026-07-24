package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static redactedrice.ptcgr.randomizer.gui.moduleconfig.StructuredInlineEditorTestSupport.findAddButton;
import static redactedrice.ptcgr.randomizer.gui.moduleconfig.StructuredInlineEditorTestSupport.findComponents;
import static redactedrice.ptcgr.randomizer.gui.moduleconfig.StructuredInlineEditorTestSupport.findFirstOuterRemoveButton;
import static redactedrice.ptcgr.randomizer.gui.moduleconfig.StructuredInlineEditorTestSupport.findRemoveButtons;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.junit.jupiter.api.Test;

import redactedrice.randomizer.lua.arguments.TypeDefinition;

class ListInlineEditorTest {

    @Test
    void everyRowIncludingPreexistingOnesIsLiveEditable() {
        ListInlineEditor editor = new ListInlineEditor(TypeDefinition.string(), null);
        editor.setValue(List.of("a", "b"));

        List<JTextField> fields = findComponents(editor.getComponent(), JTextField.class);
        assertEquals(2, fields.size());

        // Not just the newly added row - every preexisting row is a real, editable field
        fields.get(0).setText("edited-a");
        fields.get(1).setText("edited-b");
        assertEquals(List.of("edited-a", "edited-b"), editor.getValue());
    }

    @Test
    void addButtonAppendsAnEditableRow() {
        ListInlineEditor editor = new ListInlineEditor(TypeDefinition.string(), null);
        editor.setValue(new ArrayList<>(List.of("a")));

        JButton addButton = findAddButton(editor.getComponent());
        addButton.doClick();

        assertEquals(List.of("a", ""), editor.getValue());

        List<JTextField> fields = findComponents(editor.getComponent(), JTextField.class);
        assertEquals(2, fields.size());
        fields.get(1).setText("b");
        assertEquals(List.of("a", "b"), editor.getValue());
    }

    @Test
    void removeButtonDropsOnlyItsOwnRow() {
        ListInlineEditor editor = new ListInlineEditor(TypeDefinition.string(), null);
        editor.setValue(List.of("a", "b", "c"));

        List<JButton> removeButtons = findRemoveButtons(editor.getComponent());
        assertEquals(3, removeButtons.size());

        removeButtons.get(1).doClick();
        assertEquals(List.of("a", "c"), editor.getValue());
    }

    @Test
    void setEditableFalseHidesAddAndRemoveButtons() {
        ListInlineEditor editor = new ListInlineEditor(TypeDefinition.string(), null);
        editor.setValue(List.of("a"));

        editor.setEditable(false);

        for (JButton button : findRemoveButtons(editor.getComponent())) {
            assertFalse(button.isVisible());
        }
        assertFalse(findAddButton(editor.getComponent()).isVisible());
    }

    @Test
    void emptyListIsValidAndRoundTrips() {
        ListInlineEditor editor = new ListInlineEditor(TypeDefinition.string(), null);
        editor.setValue(List.of());

        assertTrue(((List<?>) editor.getValue()).isEmpty());
        assertEquals(0, findComponents(editor.getComponent(), JTextField.class).size());
    }

    @Test
    void topLevelListHasNoBoxButNestedListDoes() {
        ListInlineEditor topLevel = new ListInlineEditor(TypeDefinition.string(), null, 0, 0);
        assertNull(topLevel.getComponent().getBorder());

        ListInlineEditor nestedInList = new ListInlineEditor(TypeDefinition.string(), null, 1, 1);
        assertNotNull(nestedInList.getComponent().getBorder());

        ListInlineEditor nestedInTable = new ListInlineEditor(TypeDefinition.string(), null, 2, 0);
        assertNotNull(nestedInTable.getComponent().getBorder());
    }

    @Test
    void nestedListRoundTrips() {
        ListInlineEditor editor =
                new ListInlineEditor(TypeDefinition.listOf(TypeDefinition.integer()), null);
        editor.setValue(List.of(List.of(1, 2), List.of(3)));

        assertEquals(List.of(List.of(1, 2), List.of(3)), editor.getValue());
    }

    @Test
    void removingOuterRowDropsTheWholeNestedSublist() {
        ListInlineEditor editor =
                new ListInlineEditor(TypeDefinition.listOf(TypeDefinition.integer()), null);
        editor.setValue(List.of(List.of(1, 2), List.of(3)));

        // The row's own remove button (a direct child of the row, not one of the nested
        // sublist's inner remove buttons) should drop the entire sublist in one click.
        findFirstOuterRemoveButton(editor.getComponent()).doClick();

        assertEquals(List.of(List.of(3)), editor.getValue());
    }
}
