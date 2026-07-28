package redactedrice.ptcgr.randomizer.gui.moduleconfig;

// Computes shared widths for the three major module-config columns. Columns grow
// proportionally by weight until bounded columns hit their max; remaining slack goes
// to columns that can still expand (typically the value column).
final class ModuleConfigColumnWidths {
    record ColumnSpec(int minWidth, int maxWidth, double weight) {
        ColumnSpec {
            if (minWidth < 0 || maxWidth < minWidth || weight < 0) {
                throw new IllegalArgumentException("Invalid column spec");
            }
        }

        static ColumnSpec bounded(int minWidth, int maxWidth, double weight) {
            return new ColumnSpec(minWidth, maxWidth, weight);
        }

        static ColumnSpec minOnly(int minWidth, double weight) {
            return new ColumnSpec(minWidth, Integer.MAX_VALUE, weight);
        }
    }

    private ModuleConfigColumnWidths() {}

    static int horizontalChrome(int dataColumnCount, int separatorColumnCount, int cellPaddingH,
            int lineWidth) {
        return separatorColumnCount * lineWidth + dataColumnCount * (2 * cellPaddingH);
    }

    static int[] compute(int availableWidth, int[] naturalWidths, ColumnSpec[] specs,
            int horizontalChrome) {
        if (naturalWidths.length != specs.length) {
            throw new IllegalArgumentException("naturalWidths and specs must match");
        }

        int[] widths = computeOpeningWidths(naturalWidths, specs);

        if (availableWidth <= 0) {
            return widths;
        }

        int contentBudget = availableWidth - horizontalChrome;
        int openingSum = sum(widths);
        if (contentBudget > openingSum) {
            distributeSlack(widths, specs, contentBudget - openingSum);
        }
        return widths;
    }

    static int openingContentWidth(int[] naturalWidths, ColumnSpec[] specs, int horizontalChrome) {
        return horizontalChrome + sum(computeOpeningWidths(naturalWidths, specs));
    }

    static int[] computeOpeningWidths(int[] naturalWidths, ColumnSpec[] specs) {
        int[] widths = new int[specs.length];
        for (int i = 0; i < specs.length; i++) {
            widths[i] = openingWidth(naturalWidths[i], specs[i]);
        }
        return widths;
    }

    private static int openingWidth(int naturalWidth, ColumnSpec spec) {
        if (spec.maxWidth < Integer.MAX_VALUE) {
            return (spec.minWidth + spec.maxWidth) / 2;
        }
        return Math.max(spec.minWidth, naturalWidth);
    }

    private static void distributeSlack(int[] widths, ColumnSpec[] specs, double slack) {
        while (slack > 0.5) {
            double totalWeight = 0;
            for (int i = 0; i < widths.length; i++) {
                if (widths[i] < specs[i].maxWidth) {
                    totalWeight += specs[i].weight;
                }
            }
            if (totalWeight == 0) {
                break;
            }

            double consumed = 0;
            for (int i = 0; i < widths.length; i++) {
                if (widths[i] >= specs[i].maxWidth) {
                    continue;
                }
                int growth = (int) Math.floor(slack * (specs[i].weight / totalWeight));
                if (growth <= 0) {
                    continue;
                }
                int updated = Math.min(specs[i].maxWidth, widths[i] + growth);
                consumed += updated - widths[i];
                widths[i] = updated;
            }

            if (consumed <= 0) {
                addRemainderToLastExpandableColumn(widths, specs, slack);
                break;
            }
            slack -= consumed;
        }
    }

    private static void addRemainderToLastExpandableColumn(int[] widths, ColumnSpec[] specs,
            double slack) {
        for (int i = widths.length - 1; i >= 0; i--) {
            if (widths[i] < specs[i].maxWidth) {
                widths[i] += (int) Math.ceil(slack);
                return;
            }
        }
    }

    private static int sum(int[] values) {
        int total = 0;
        for (int value : values) {
            total += value;
        }
        return total;
    }
}
