package redactedrice.ptcgr.randomizer.gui.moduleconfig.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.swing.JLabel;

import org.junit.jupiter.api.Test;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.ModuleConfigDialog;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.ModuleConfigColumnWidths.ColumnSpec;
import redactedrice.randomizer.lua.arguments.TypeDefinition;

public class ModuleConfigGridPanelTest {
    private static final ColumnSpec[] SPECS = {
            ColumnSpec.bounded(80, 150, 1),
            ColumnSpec.bounded(70, 160, 1),
            ColumnSpec.bounded(80, 200, 1),
            ColumnSpec.minOnly(ColumnSizing.ENTRY_BOX_WIDTH, 1),
    };

    @Test
    void valueColumnMinimumWidth_usesLargestRegisteredValueCellMinimum() {
        ModuleConfigGridPanel grid = new ModuleConfigGridPanel(SPECS, 67);

        registerValueCell(grid, TypeDefinition.string());
        assertEquals(ColumnSizing.ENTRY_BOX_WIDTH, grid.valueColumnMinimumWidth());

        TypeDefinition listOfList = TypeDefinition.listOf(TypeDefinition.listOf(TypeDefinition.integer()));
        int nestedMin = ColumnSizing.minimumValueWidth(listOfList);
        registerValueCell(grid, listOfList);
        assertEquals(nestedMin, grid.valueColumnMinimumWidth());
        assertEquals(80 + 70 + 80 + nestedMin + 67, grid.minimumContentWidth());
    }

    private static void registerValueCell(ModuleConfigGridPanel grid, TypeDefinition valueType) {
        ColumnWidthPanel panel = new ColumnWidthPanel(new JLabel("v"),
                ColumnSizing.minimumValueWidth(valueType), Integer.MAX_VALUE, true);
        grid.registerColumnPanel(ModuleConfigDialog.VALUE_COLUMN, panel);
    }
}
