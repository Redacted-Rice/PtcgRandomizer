package redactedrice.ptcgr.randomizer.gui.moduleconfig.layout;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.ModuleConfigColumnWidths.ColumnSpec;

public class ModuleConfigColumnWidthsTest {
    private static final ColumnSpec[] SPECS = {
            ColumnSpec.bounded(100, 200, 1),
            ColumnSpec.bounded(160, 320, 1),
            ColumnSpec.minOnly(60, 1),
    };
    private static final int[] NATURAL = {120, 180, 80};

    @Test
    void compute_respectsColumnBounds() {
        assertArrayEquals(new int[] {100, 160, 60},
                ModuleConfigColumnWidths.compute(0, new int[] {80, 140, 40}, SPECS, 50));
        assertArrayEquals(new int[] {120, 180, 120},
                ModuleConfigColumnWidths.compute(0, new int[] {120, 180, 120}, SPECS, 50));
        assertArrayEquals(new int[] {200, 320, 120},
                ModuleConfigColumnWidths.compute(0, new int[] {250, 400, 120}, SPECS, 50));
    }

    @Test
    void compute_adjustsToAvailableWidth() {
        int chrome = 50;

        assertArrayEquals(new int[] {200, 320, 260},
                ModuleConfigColumnWidths.compute(chrome + 200 + 320 + 260, NATURAL, SPECS, chrome));
        assertArrayEquals(new int[] {200, 290, 190},
                ModuleConfigColumnWidths.compute(chrome + 200 + 320 + 160, NATURAL, SPECS, chrome));
        assertArrayEquals(new int[] {100, 160, 60},
                ModuleConfigColumnWidths.compute(chrome + 100 + 150 + 50, NATURAL, SPECS, chrome));
        assertArrayEquals(new int[] {104, 164, 64},
                ModuleConfigColumnWidths.compute(chrome + 104 + 164 + 64, NATURAL, SPECS, chrome));
    }
}
