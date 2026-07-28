package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.ModuleConfigColumnWidths.ColumnSpec;

class ModuleConfigColumnWidthsTest {
    private static final ColumnSpec[] SPECS = {
            ColumnSpec.bounded(100, 200, 1),
            ColumnSpec.bounded(160, 320, 1),
            ColumnSpec.minOnly(60, 1),
    };

    @Test
    void compute_usesNaturalWidthsClampedToMinAndMaxWhenNoExtraSpace() {
        int[] result = ModuleConfigColumnWidths.compute(0, new int[] {80, 140, 40}, SPECS, 50);

        assertArrayEquals(new int[] {100, 160, 60}, result);
    }

    @Test
    void compute_givesRemainingSlackToUnboundedColumnAfterOthersReachMax() {
        int chrome = 50;
        int available = chrome + 200 + 320 + 260;

        int[] result = ModuleConfigColumnWidths.compute(available, new int[] {120, 180, 80},
                SPECS, chrome);

        assertArrayEquals(new int[] {200, 320, 260}, result);
    }

    @Test
    void compute_growsColumnsProportionallyUntilSomeReachMax() {
        int chrome = 50;
        int available = chrome + 200 + 270 + 170;

        int[] result = ModuleConfigColumnWidths.compute(available, new int[] {120, 180, 80},
                SPECS, chrome);

        assertArrayEquals(new int[] {200, 270, 170}, result);
    }

    @Test
    void compute_splitsSlackProportionallyBeforeAnyColumnHitsMax() {
        int chrome = 50;
        int available = chrome + 153 + 213 + 113;

        int[] result = ModuleConfigColumnWidths.compute(available, new int[] {120, 180, 80},
                SPECS, chrome);

        assertArrayEquals(new int[] {153, 213, 113}, result);
    }
}
