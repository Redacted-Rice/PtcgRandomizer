package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import redactedrice.randomizer.lua.arguments.TypeDefinition;

class ValueColumnWidthsTest {
    @Test
    void minimumWidth_derivesFromLayoutControlCounts() {
        assertEquals(ValueColumnWidths.ENTRY_BOX_WIDTH + ValueColumnWidths.REMOVE_BUTTON_WIDTH,
                ValueColumnWidths.minimumWidth(TypeDefinition.listOf(TypeDefinition.string())));

        assertEquals(ValueColumnWidths.ENTRY_BOX_WIDTH + 2 * ValueColumnWidths.REMOVE_BUTTON_WIDTH,
                ValueColumnWidths.minimumWidth(
                        TypeDefinition.listOf(TypeDefinition.listOf(TypeDefinition.integer()))));

        assertEquals(2 * ValueColumnWidths.ENTRY_BOX_WIDTH + ValueColumnWidths.REMOVE_BUTTON_WIDTH
                + ValueColumnWidths.TABLE_ARROW_WIDTH,
                ValueColumnWidths.minimumWidth(TypeDefinition.tableOf(TypeDefinition.string(),
                        TypeDefinition.integer())));

        TypeDefinition tableOfTableOfList = TypeDefinition.tableOf(TypeDefinition.string(),
                TypeDefinition.tableOf(TypeDefinition.string(),
                        TypeDefinition.listOf(TypeDefinition.integer())));
        assertEquals(3 * ValueColumnWidths.ENTRY_BOX_WIDTH
                + 3 * ValueColumnWidths.REMOVE_BUTTON_WIDTH
                + 2 * ValueColumnWidths.TABLE_ARROW_WIDTH,
                ValueColumnWidths.minimumWidth(tableOfTableOfList));
    }

    @Test
    void minimumWidth_usesViewModeMinimumWhenNotEditable() {
        TypeDefinition nested = TypeDefinition.listOf(TypeDefinition.listOf(TypeDefinition.integer()));
        assertEquals(ValueColumnWidths.VIEW_MODE_MIN_WIDTH,
                ValueColumnWidths.minimumWidth(nested, false));
        assertEquals(ValueColumnWidths.ENTRY_BOX_WIDTH + 2 * ValueColumnWidths.REMOVE_BUTTON_WIDTH,
                ValueColumnWidths.minimumWidth(nested, true));
    }
}
