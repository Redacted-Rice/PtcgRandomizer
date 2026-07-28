package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;

// Wraps a component to enforce a minimum preferred width without pinning its preferred size the
// way calling setPreferredSize(...) directly on the wrapped component would. That distinction
// matters for content that can change size later (e.g. a StructuredGridPanel gaining/losing rows as
// entries are added/removed) - overriding getPreferredSize() here always reflects the wrapped
// component's current natural size, only ever raising the width floor, never freezing a stale
// snapshot of it.
final class MinWidthPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final int minWidth;
    private final boolean expandHorizontally;

    MinWidthPanel(JComponent content, int minWidth, boolean expandHorizontally) {
        super(new BorderLayout());
        this.minWidth = minWidth;
        this.expandHorizontally = expandHorizontally;
        setOpaque(false);
        add(content, BorderLayout.CENTER);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension pref = super.getPreferredSize();
        return new Dimension(Math.max(pref.width, minWidth), pref.height);
    }

    // Never stretches taller than its own preferred height - a spanning cell (e.g. a TABLE key)
    // should stay anchored at the top of its span rather than growing to match a tall nested
    // value regardless of whether it expands horizontally.
    @Override
    public Dimension getMaximumSize() {
        Dimension pref = getPreferredSize();
        if (expandHorizontally) {
            return new Dimension(Integer.MAX_VALUE, pref.height);
        }
        return pref;
    }
}
