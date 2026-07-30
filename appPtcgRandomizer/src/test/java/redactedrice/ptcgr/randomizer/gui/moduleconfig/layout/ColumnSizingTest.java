package redactedrice.ptcgr.randomizer.gui.moduleconfig.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import redactedrice.randomizer.lua.arguments.TypeDefinition;

public class ColumnSizingTest {
    @Test
    void minimumWidth_derivesFromLayoutControlCounts() {
        assertEquals(ColumnSizing.ENTRY_BOX_WIDTH + ColumnSizing.REMOVE_BUTTON_WIDTH,
                ColumnSizing.minimumValueWidth(TypeDefinition.listOf(TypeDefinition.string())));

        assertEquals(ColumnSizing.ENTRY_BOX_WIDTH + 2 * ColumnSizing.REMOVE_BUTTON_WIDTH,
                ColumnSizing.minimumValueWidth(
                        TypeDefinition.listOf(TypeDefinition.listOf(TypeDefinition.integer()))));

        assertEquals(2 * ColumnSizing.ENTRY_BOX_WIDTH + ColumnSizing.REMOVE_BUTTON_WIDTH
                + ColumnSizing.TABLE_ARROW_WIDTH,
                ColumnSizing.minimumValueWidth(TypeDefinition.tableOf(TypeDefinition.string(),
                        TypeDefinition.integer())));

        TypeDefinition tableOfTableOfList = TypeDefinition.tableOf(TypeDefinition.string(),
                TypeDefinition.tableOf(TypeDefinition.string(),
                        TypeDefinition.listOf(TypeDefinition.integer())));
        assertEquals(3 * ColumnSizing.ENTRY_BOX_WIDTH
                + 3 * ColumnSizing.REMOVE_BUTTON_WIDTH
                + 2 * ColumnSizing.TABLE_ARROW_WIDTH,
                ColumnSizing.minimumValueWidth(tableOfTableOfList));

        TypeDefinition nested = TypeDefinition.listOf(TypeDefinition.listOf(TypeDefinition.integer()));
        assertEquals(ColumnSizing.VIEW_MODE_MIN_WIDTH,
                ColumnSizing.minimumValueWidth(nested, false));
        assertEquals(ColumnSizing.ENTRY_BOX_WIDTH + 2 * ColumnSizing.REMOVE_BUTTON_WIDTH,
                ColumnSizing.minimumValueWidth(nested, true));
    }
}
