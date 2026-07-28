package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;

// Column cell wrapper that enforces min/max width and can be assigned a shared column width
// during layout so every row in the column stays aligned.
final class ColumnWidthPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final int minWidth;
    private final int maxWidth;
    private final boolean expandHorizontally;
    private int layoutColumnWidth = -1;

    ColumnWidthPanel(JComponent content, int minWidth, int maxWidth, boolean expandHorizontally) {
        super(new BorderLayout());
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.expandHorizontally = expandHorizontally;
        setOpaque(false);
        add(content, BorderLayout.CENTER);
    }

    int getContentWidth() {
        return getContentSize().width;
    }

    void setLayoutColumnWidth(int width) {
        layoutColumnWidth = width;
    }

    void clearLayoutColumnWidth() {
        layoutColumnWidth = -1;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension content = getContentSize();
        return new Dimension(resolveWidth(content.width), content.height);
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension content = getContentSize();
        return new Dimension(minWidth, content.height);
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension pref = getPreferredSize();
        int widthCap = expandHorizontally ? maxWidth : pref.width;
        return new Dimension(widthCap, pref.height);
    }

    private Dimension getContentSize() {
        if (getComponentCount() == 0) {
            return new Dimension(0, 0);
        }
        return getComponent(0).getPreferredSize();
    }

    private int resolveWidth(int contentWidth) {
        if (layoutColumnWidth >= 0) {
            return layoutColumnWidth;
        }
        int width = Math.max(minWidth, contentWidth);
        if (maxWidth != Integer.MAX_VALUE) {
            width = Math.min(maxWidth, width);
        }
        return width;
    }
}
