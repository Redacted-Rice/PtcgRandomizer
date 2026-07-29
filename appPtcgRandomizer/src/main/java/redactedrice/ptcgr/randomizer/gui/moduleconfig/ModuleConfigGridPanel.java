package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.ModuleConfigColumnWidths.ColumnSpec;

// GridBagLayout table whose data columns share computed min/max widths. Implements Scrollable so
// the view tracks the viewport while above the column minimum width, and keeps its width (with a
// horizontal scrollbar) once the viewport would go narrower than the column minimums.
final class ModuleConfigGridPanel extends JPanel implements Scrollable {
    private static final long serialVersionUID = 1L;

    private final List<ColumnWidthPanel> argumentColumnPanels = new ArrayList<>();
    private final List<ColumnWidthPanel> typeColumnPanels = new ArrayList<>();
    private final List<ColumnWidthPanel> constraintsColumnPanels = new ArrayList<>();
    private final List<ColumnWidthPanel> valueColumnPanels = new ArrayList<>();

    private final ColumnSpec[] columnSpecs;
    private final int horizontalChrome;
    private int valueColumnMinimumWidth;

    ModuleConfigGridPanel(ColumnSpec[] columnSpecs, int horizontalChrome) {
        super(new GridBagLayout());
        this.columnSpecs = columnSpecs.clone();
        this.horizontalChrome = horizontalChrome;
        this.valueColumnMinimumWidth = columnSpecs[3].minWidth();
    }

    void registerColumnPanel(int columnIndex, ColumnWidthPanel panel) {
        switch (columnIndex) {
            case ModuleConfigDialog.ARGUMENT_COLUMN -> argumentColumnPanels.add(panel);
            case ModuleConfigDialog.TYPE_COLUMN -> typeColumnPanels.add(panel);
            case ModuleConfigDialog.CONSTRAINTS_COLUMN -> constraintsColumnPanels.add(panel);
            case ModuleConfigDialog.VALUE_COLUMN -> {
                valueColumnPanels.add(panel);
                valueColumnMinimumWidth =
                        Math.max(valueColumnMinimumWidth, panel.getDeclaredMinWidth());
            }
            default -> throw new IllegalArgumentException("Not a data column: " + columnIndex);
        }
    }

    int minimumContentWidth() {
        return ModuleConfigColumnWidths.minimumContentWidth(effectiveColumnSpecs(),
                horizontalChrome);
    }

    int valueColumnMinimumWidth() {
        return valueColumnMinimumWidth;
    }

    private ColumnSpec[] effectiveColumnSpecs() {
        ColumnSpec[] specs = columnSpecs.clone();
        specs[3] = ColumnSpec.minOnly(valueColumnMinimumWidth, columnSpecs[3].weight());
        return specs;
    }

    @Override
    public Dimension getPreferredSize() {
        int[] naturalWidths = measureNaturalWidths();
        int availableWidth = resolveAvailableWidth(naturalWidths);
        applyColumnWidths(availableWidth, naturalWidths);
        Dimension size = super.getPreferredSize();
        size.width = Math.max(size.width, minimumContentWidth());
        return size;
    }

    @Override
    public void doLayout() {
        int[] naturalWidths = measureNaturalWidths();
        applyColumnWidths(resolveAvailableWidth(naturalWidths), naturalWidths);
        super.doLayout();
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation,
            int direction) {
        if (orientation == SwingConstants.VERTICAL) {
            return visibleRect.height;
        }
        return visibleRect.width;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        JViewport viewport = viewportAncestor();
        if (viewport == null) {
            return false;
        }
        int viewportWidth = viewport.getWidth();
        return viewportWidth > 0 && viewportWidth >= minimumContentWidth();
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    private JViewport viewportAncestor() {
        Container ancestor = SwingUtilities.getAncestorOfClass(JViewport.class, this);
        return ancestor instanceof JViewport viewport ? viewport : null;
    }

    private int resolveAvailableWidth(int[] naturalWidths) {
        int width = getWidth();
        if (width <= 0) {
            return ModuleConfigColumnWidths.openingContentWidth(naturalWidths,
                    effectiveColumnSpecs(), horizontalChrome);
        }
        return width;
    }

    private void applyColumnWidths(int availableWidth, int[] naturalWidths) {
        int[] columnWidths = ModuleConfigColumnWidths.compute(availableWidth, naturalWidths,
                effectiveColumnSpecs(), horizontalChrome);
        setLayoutColumnWidth(argumentColumnPanels, columnWidths[0]);
        setLayoutColumnWidth(typeColumnPanels, columnWidths[1]);
        setLayoutColumnWidth(constraintsColumnPanels, columnWidths[2]);
        setLayoutColumnWidth(valueColumnPanels, columnWidths[3]);
    }

    private int[] measureNaturalWidths() {
        return new int[] {
                maxContentWidth(argumentColumnPanels),
                maxContentWidth(typeColumnPanels),
                maxContentWidth(constraintsColumnPanels),
                maxContentWidth(valueColumnPanels),
        };
    }

    private static int maxContentWidth(List<ColumnWidthPanel> panels) {
        int max = 0;
        for (ColumnWidthPanel panel : panels) {
            max = Math.max(max, panel.getContentWidth());
        }
        return max;
    }

    private static void setLayoutColumnWidth(List<ColumnWidthPanel> panels, int width) {
        for (ColumnWidthPanel panel : panels) {
            panel.setLayoutColumnWidth(width);
        }
    }
}
