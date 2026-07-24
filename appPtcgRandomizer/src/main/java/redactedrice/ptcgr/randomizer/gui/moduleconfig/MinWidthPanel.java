package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;

// Wraps a component to enforce a minimum preferred width without pinning its preferred size the
// way calling setPreferredSize(...) directly on the wrapped component would. That distinction
// matters for content that can change size later (e.g. a ListInlineEditor gaining/losing rows as
// entries are added/removed) - overriding getPreferredSize() here always reflects the wrapped
// component's current natural size, only ever raising the width floor, never freezing a stale
// snapshot of it.
final class MinWidthPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final int minWidth;
    private final boolean expandHorizontally;

    MinWidthPanel(JComponent content, int minWidth) {
        this(content, minWidth, false);
    }

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

    // Compact fields (keys) stay at their natural size so they do not stretch when a nested value
    // shrinks or grow taller when a nested value is tall. Expandable fields (scalar values, top
    // level argument cells) may grow horizontally with the row/column.
    @Override
    public Dimension getMaximumSize() {
        Dimension pref = getPreferredSize();
        if (expandHorizontally) {
            return new Dimension(Integer.MAX_VALUE, pref.height);
        }
        return pref;
    }
}
