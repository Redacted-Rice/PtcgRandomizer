package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;

// 1px grid lines shared by ModuleConfigDialog and StructuredGridPanel.
final class GridSeparators {
    private static final Color LINE_COLOR = new Color(215, 215, 215);
    private static final int LINE_WIDTH = 1;

    private GridSeparators() {}

    static JComponent verticalLine() {
        JPanel line = new JPanel();
        line.setBackground(LINE_COLOR);
        line.setOpaque(true);
        line.setPreferredSize(new Dimension(LINE_WIDTH, 0));
        line.setMinimumSize(new Dimension(LINE_WIDTH, 0));
        line.setMaximumSize(new Dimension(LINE_WIDTH, Integer.MAX_VALUE));
        return line;
    }

    static JComponent horizontalLine() {
        JPanel line = new JPanel();
        line.setBackground(LINE_COLOR);
        line.setOpaque(true);
        line.setPreferredSize(new Dimension(0, LINE_WIDTH));
        line.setMinimumSize(new Dimension(0, LINE_WIDTH));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, LINE_WIDTH));
        return line;
    }
}
