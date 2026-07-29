package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import redactedrice.randomizer.lua.arguments.TypeDefinition;

// Minimum widths for value column cells based on the structured grid controls each type needs.
final class ValueColumnWidths {
    static final int ENTRY_BOX_WIDTH = 80;
    static final int VIEW_MODE_MIN_WIDTH = 160;
    static final int REMOVE_BUTTON_WIDTH = 60;
    static final int TABLE_ARROW_WIDTH = 40;

    private ValueColumnWidths() {}

    static int minimumWidth(TypeDefinition valueType) {
        return minimumWidth(valueType, true);
    }

    static int minimumWidth(TypeDefinition valueType, boolean editable) {
        if (!editable) {
            return VIEW_MODE_MIN_WIDTH;
        }
        if (valueType == null || (!valueType.isList() && !valueType.isTable())) {
            return ENTRY_BOX_WIDTH;
        }
        StructuredGridModel.LayoutControlCounts counts =
                StructuredGridModel.layoutControlCounts(valueType);
        return counts.entryBoxes() * ENTRY_BOX_WIDTH + counts.removeButtons() * REMOVE_BUTTON_WIDTH
                + counts.tableLevels() * TABLE_ARROW_WIDTH;
    }
}
