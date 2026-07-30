package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

abstract class ModuleConfigGuiTestSupport {
    // Value adjustment popups block automated GUI tests on machines with a display
    @BeforeAll
    static void suppressValueAdjustmentPopups() {
        ValueAdjustmentWarnings.setNotifierForTests((parent, message) -> {});
    }

    @AfterAll
    static void restoreValueAdjustmentPopups() {
        ValueAdjustmentWarnings.resetNotifierForTests();
    }
}
