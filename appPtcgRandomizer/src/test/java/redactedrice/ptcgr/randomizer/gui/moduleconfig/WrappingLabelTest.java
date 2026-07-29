package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WrappingLabelTest {
    @Test
    void setWrapWidth_increasesHeightForLongText() {
        WrappingLabel label = new WrappingLabel(
                "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");

        int unwrappedHeight = label.getPreferredSize().height;
        label.setWrapWidth(80);
        int wrappedHeight = label.getPreferredSize().height;

        assertTrue(wrappedHeight > unwrappedHeight);
        assertTrue(label.getUnwrappedWidth() > 80);
    }

    @Test
    void constraintLabelWrapsOnlyWhenColumnIsTooNarrow() {
        WrappingLabel label = WrappingLabel.constraints("Int min - Int max");
        int lineHeight = label.getFontMetrics(label.getFont()).getHeight();

        label.setWrapWidth(label.getUnwrappedWidth());
        assertTrue(label.getPreferredSize().height <= lineHeight);

        label.setWrapWidth(40);
        assertTrue(label.getUnwrappedWidth() > 40);
        assertTrue(label.getPreferredSize().height > lineHeight);
    }
}
