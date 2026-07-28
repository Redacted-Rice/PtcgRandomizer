package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;

import javax.swing.JTextArea;
import javax.swing.text.View;

// Read-only, word-wrapping label backed by JTextArea's built-in line wrapping. Column layout
// drives the wrap width; height grows/shrinks as text reflows.
final class WrappingLabel extends JTextArea {
    private static final long serialVersionUID = 1L;

    private int wrapWidth = -1;

    WrappingLabel(String text) {
        super(text == null ? "" : text);
        configureAsLabel();
    }

    static WrappingLabel header(String text) {
        WrappingLabel label = new WrappingLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    static WrappingLabel constraints(String text) {
        WrappingLabel label = new WrappingLabel(text);
        label.setForeground(Color.DARK_GRAY);
        return label;
    }

    void setWrapWidth(int width) {
        if (width <= 0 || wrapWidth == width) {
            return;
        }
        wrapWidth = width;
        revalidate();
    }

    int getUnwrappedWidth() {
        FontMetrics metrics = getFontMetrics(getFont());
        int widestLine = 0;
        for (String line : getText().split("\n", -1)) {
            widestLine = Math.max(widestLine, metrics.stringWidth(line));
        }
        return widestLine;
    }

    @Override
    public Dimension getPreferredSize() {
        if (wrapWidth > 0) {
            return new Dimension(wrapWidth, wrappedTextHeight(wrapWidth));
        }

        FontMetrics metrics = getFontMetrics(getFont());
        return new Dimension(getUnwrappedWidth(), metrics.getHeight());
    }

    private int wrappedTextHeight(int width) {
        int innerWidth = Math.max(0, width);
        View root = getUI().getRootView(this);
        if (root == null) {
            return getFontMetrics(getFont()).getHeight();
        }
        root.setSize(innerWidth, 0);
        float span = root.getPreferredSpan(View.Y_AXIS);
        return (int) Math.ceil(span);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension pref = getPreferredSize();
        return new Dimension(Integer.MAX_VALUE, pref.height);
    }

    private void configureAsLabel() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setEditable(false);
        setFocusable(false);
        setOpaque(false);
        setBorder(null);
        setMargin(new Insets(0, 0, 0, 0));
        setHighlighter(null);
        setCursor(null);
    }
}
