package redactedrice.ptcgr.randomizer.gui.moduleconfig.dialog;

import java.awt.Rectangle;

import javax.swing.JScrollPane;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.ModuleConfigGridPanel;

// Suppresses focus-driven scrolling while the dialog is first shown and sized.
final class ModuleConfigScrollPane extends JScrollPane {
    private static final long serialVersionUID = 1L;

    private boolean suppressScrollRectToVisible = true;

    ModuleConfigScrollPane(ModuleConfigGridPanel grid) {
        super(grid);
    }

    void releaseInitialScrollLock() {
        suppressScrollRectToVisible = false;
    }

    @Override
    public void scrollRectToVisible(Rectangle rect) {
        if (!suppressScrollRectToVisible) {
            super.scrollRectToVisible(rect);
        }
    }
}
