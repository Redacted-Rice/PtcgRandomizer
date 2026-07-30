package redactedrice.ptcgr.randomizer.gui.moduleconfig.layout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;

import javax.swing.JTextArea;

// Read-only, word-wrapping label backed by JTextArea. Column layout drives the wrap width;
// height grows/shrinks as text reflows. Text measurement uses LineBreakMeasurer so opening
// widths and wrapped row heights match what will actually fit.
public final class WrappingLabel extends JTextArea {
    private static final long serialVersionUID = 1L;

    // JTextArea paints slightly wider than FontMetrics alone so keep opening widths from clipping
    private static final int TEXT_WIDTH_PADDING = 4;

    private int wrapWidth = -1;

    public WrappingLabel(String text) {
        super(text == null ? "" : text);
        configureAsLabel();
    }

    public static WrappingLabel constraints(String text) {
        WrappingLabel label = new WrappingLabel(text);
        label.setForeground(Color.DARK_GRAY);
        return label;
    }

    public void setWrapWidth(int width) {
        if (width <= 0) {
            return;
        }
        wrapWidth = width;
        revalidate();
        repaint();
    }

    public int getUnwrappedWidth() {
        Font font = getFont();
        FontRenderContext context = fontRenderContext();
        int widest = 0;
        for (String paragraph : getText().split("\n", -1)) {
            widest = Math.max(widest, widestLineAdvance(paragraph, font, context));
        }
        return widest + TEXT_WIDTH_PADDING;
    }

    @Override
    public Dimension getPreferredSize() {
        if (wrapWidth > 0) {
            return new Dimension(wrapWidth, wrappedTextHeight(wrapWidth));
        }

        int unwrappedWidth = getUnwrappedWidth();
        return new Dimension(unwrappedWidth, wrappedTextHeight(unwrappedWidth));
    }

    private int wrappedTextHeight(int width) {
        return wrappedLineCount(width) * lineHeight();
    }

    private int wrappedLineCount(int width) {
        if (width <= 0) {
            return 1;
        }
        String text = getText();
        if (text.isEmpty()) {
            return 1;
        }

        Font font = getFont();
        FontRenderContext context = fontRenderContext();
        int lines = 0;
        for (String paragraph : text.split("\n", -1)) {
            lines += wrappedParagraphLineCount(paragraph, width, font, context);
        }
        return Math.max(1, lines);
    }

    private static int wrappedParagraphLineCount(String paragraph, int width, Font font,
            FontRenderContext context) {
        if (paragraph.isEmpty()) {
            return 1;
        }
        AttributedCharacterIterator iterator = attributedIterator(paragraph, font);
        BreakIterator lineBreak = BreakIterator.getLineInstance();
        lineBreak.setText(paragraph);
        LineBreakMeasurer measurer = new LineBreakMeasurer(iterator, lineBreak, context);
        int layoutWidth = Math.max(1, width - TEXT_WIDTH_PADDING);
        int lines = 0;
        while (measurer.getPosition() < paragraph.length()) {
            measurer.nextLayout(layoutWidth);
            lines++;
        }
        return lines;
    }

    private static int widestLineAdvance(String paragraph, Font font, FontRenderContext context) {
        if (paragraph.isEmpty()) {
            return 0;
        }
        AttributedCharacterIterator iterator = attributedIterator(paragraph, font);
        LineBreakMeasurer measurer = new LineBreakMeasurer(iterator, context);
        int widest = 0;
        while (measurer.getPosition() < paragraph.length()) {
            widest = Math.max(widest,
                    (int) Math.ceil(measurer.nextLayout(Float.MAX_VALUE).getAdvance()));
        }
        return widest;
    }

    private static AttributedCharacterIterator attributedIterator(String text, Font font) {
        AttributedString attributed = new AttributedString(text);
        attributed.addAttribute(TextAttribute.FONT, font);
        return attributed.getIterator();
    }

    private FontRenderContext fontRenderContext() {
        return getFontMetrics(getFont()).getFontRenderContext();
    }

    private int lineHeight() {
        FontMetrics metrics = getFontMetrics(getFont());
        return metrics.getHeight();
    }

    @Override
    public Dimension getMinimumSize() {
        int height = wrapWidth > 0 ? wrappedTextHeight(wrapWidth) : lineHeight();
        return new Dimension(0, height);
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
