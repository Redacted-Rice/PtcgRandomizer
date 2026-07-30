package redactedrice.ptcgr.randomizer.gui.moduleconfig.layout;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;

// Column cell wrapper that enforces min/max width and can be assigned a shared column width
// during layout so every row in the column stays aligned.
public final class ColumnWidthPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final int minWidth;
    private final int maxWidth;
    private final boolean expandHorizontally;
    private int layoutColumnWidth = -1;

    public ColumnWidthPanel(JComponent content, int minWidth, int maxWidth, boolean expandHorizontally) {
        super(new BorderLayout());
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.expandHorizontally = expandHorizontally;
        setOpaque(false);
        add(content, BorderLayout.CENTER);
    }

    public int getDeclaredMinWidth() {
        return minWidth;
    }

    public int getContentWidth() {
        if (getComponentCount() == 0) {
            return 0;
        }
        JComponent content = (JComponent) getComponent(0);
        return ColumnSizing.measureContent(content);
    }

    public void setLayoutColumnWidth(int width) {
        if (layoutColumnWidth == width) {
            return;
        }
        layoutColumnWidth = width;
        syncWrapWidth(width);
        revalidate();
    }

    @Override
    public Dimension getPreferredSize() {
        int width = resolvePreferredWidth(getContentWidth());
        syncWrapWidth(width);
        return new Dimension(width, getWrappedContentHeight());
    }

    @Override
    public Dimension getMinimumSize() {
        syncWrapWidth(minWidth);
        return new Dimension(minWidth, getWrappedContentHeight());
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension pref = getPreferredSize();
        int widthCap = expandHorizontally ? maxWidth : pref.width;
        return new Dimension(widthCap, pref.height);
    }

    private int getWrappedContentHeight() {
        if (getComponentCount() == 0) {
            return 0;
        }
        return getComponent(0).getPreferredSize().height;
    }

    private void syncWrapWidth(int width) {
        if (getComponentCount() == 0) {
            return;
        }
        JComponent content = (JComponent) getComponent(0);
        if (content instanceof WrappingLabel wrappingLabel) {
            wrappingLabel.setWrapWidth(width);
        }
    }

    private int resolvePreferredWidth(int contentWidth) {
        if (layoutColumnWidth >= 0) {
            return clampWidth(layoutColumnWidth);
        }
        return clampWidth(Math.max(0, contentWidth));
    }

    private int clampWidth(int width) {
        if (maxWidth != Integer.MAX_VALUE) {
            return Math.min(maxWidth, Math.max(minWidth, width));
        }
        return Math.max(minWidth, width);
    }
}
