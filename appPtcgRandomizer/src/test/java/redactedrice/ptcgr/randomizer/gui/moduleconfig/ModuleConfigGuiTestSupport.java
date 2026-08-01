package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.dialog.InvalidInputDialogs;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.editor.NumericEditing;

public abstract class ModuleConfigGuiTestSupport {
    // Modal dialogs block automated GUI tests on machines with a display
    @BeforeAll
    public static void suppressGuiDialogs() {
        NumericEditing.setNotifierForTests((parent, message) -> {});
        InvalidInputDialogs.setNotifierForTests((parent, message) -> {});
    }

    @AfterAll
    public static void restoreGuiDialogs() {
        NumericEditing.resetNotifierForTests();
        InvalidInputDialogs.resetNotifierForTests();
    }
}
