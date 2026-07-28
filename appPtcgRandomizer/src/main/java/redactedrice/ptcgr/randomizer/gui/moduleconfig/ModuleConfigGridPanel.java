package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.ModuleConfigColumnWidths.ColumnSpec;

// GridBagLayout table whose three data columns share computed min/max widths.
final class ModuleConfigGridPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final List<ColumnWidthPanel> argumentColumnPanels = new ArrayList<>();
    private final List<ColumnWidthPanel> constraintsColumnPanels = new ArrayList<>();
    private final List<ColumnWidthPanel> valueColumnPanels = new ArrayList<>();

    private final ColumnSpec[] columnSpecs;
    private final int horizontalChrome;

    ModuleConfigGridPanel(ColumnSpec[] columnSpecs, int horizontalChrome) {
        super(new GridBagLayout());
        this.columnSpecs = columnSpecs.clone();
        this.horizontalChrome = horizontalChrome;
    }

    void registerColumnPanel(int columnIndex, ColumnWidthPanel panel) {
        switch (columnIndex) {
            case ModuleConfigDialog.ARGUMENT_COLUMN -> argumentColumnPanels.add(panel);
            case ModuleConfigDialog.CONSTRAINTS_COLUMN -> constraintsColumnPanels.add(panel);
            case ModuleConfigDialog.VALUE_COLUMN -> valueColumnPanels.add(panel);
            default -> throw new IllegalArgumentException("Not a data column: " + columnIndex);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        applyColumnWidths(0);
        return super.getPreferredSize();
    }

    @Override
    public void doLayout() {
        applyColumnWidths(getWidth());
        super.doLayout();
    }

    private void applyColumnWidths(int availableWidth) {
        int[] columnWidths = ModuleConfigColumnWidths.compute(availableWidth,
                measureNaturalWidths(), columnSpecs, horizontalChrome);
        setLayoutColumnWidth(argumentColumnPanels, columnWidths[0]);
        setLayoutColumnWidth(constraintsColumnPanels, columnWidths[1]);
        setLayoutColumnWidth(valueColumnPanels, columnWidths[2]);
    }

    private int[] measureNaturalWidths() {
        return new int[] {
                maxContentWidth(argumentColumnPanels),
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
