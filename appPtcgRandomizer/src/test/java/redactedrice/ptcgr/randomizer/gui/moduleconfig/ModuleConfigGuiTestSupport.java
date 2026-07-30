package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.editor.NumericEditing;

public abstract class ModuleConfigGuiTestSupport {
    // Value adjustment popups block automated GUI tests on machines with a display
    @BeforeAll
    public static void suppressValueAdjustmentPopups() {
        NumericEditing.setNotifierForTests((parent, message) -> {});
    }

    @AfterAll
    public static void restoreValueAdjustmentPopups() {
        NumericEditing.resetNotifierForTests();
    }
}
