package redactedrice.ptcgr.randomizer.gui.moduleconfig.layout;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class WrappingLabelTest {
    @Test
    void wrapWidthControlsReflowAndUnwrappedMeasurement() {
        WrappingLabel longLabel = new WrappingLabel(
                "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");

        int unwrappedHeight = longLabel.getPreferredSize().height;
        longLabel.setWrapWidth(80);
        assertTrue(longLabel.getPreferredSize().height > unwrappedHeight);
        assertTrue(longLabel.getUnwrappedWidth() > 80);

        WrappingLabel constraintLabel = WrappingLabel.constraints("Int min - Int max");
        int lineHeight = constraintLabel.getFontMetrics(constraintLabel.getFont()).getHeight();

        constraintLabel.setWrapWidth(200);
        assertTrue(constraintLabel.getPreferredSize().height <= lineHeight + 2);

        constraintLabel.setWrapWidth(40);
        assertTrue(constraintLabel.getPreferredSize().height > lineHeight);
    }
}
