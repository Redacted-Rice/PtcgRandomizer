package redactedrice.ptcgr.randomizer.gui.moduleconfig;

// Computes shared widths for the module config columns. Opening widths use the widest cell
// in each column (clamped to min/max); extra space grows columns proportionally by weight.
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
        if (contentBudget <= sum(minimumColumnWidths(specs))) {
            return minimumColumnWidths(specs);
        }

        int openingSum = sum(widths);
        if (contentBudget > openingSum) {
            distributeSlack(widths, specs, contentBudget - openingSum);
        } else if (contentBudget < openingSum) {
            shrinkToFit(widths, specs, openingSum - contentBudget);
        }
        return widths;
    }

    static int openingContentWidth(int[] naturalWidths, ColumnSpec[] specs, int horizontalChrome) {
        return horizontalChrome + sum(computeOpeningWidths(naturalWidths, specs));
    }

    static int minimumContentWidth(ColumnSpec[] specs, int horizontalChrome) {
        int total = 0;
        for (ColumnSpec spec : specs) {
            total += spec.minWidth;
        }
        return horizontalChrome + total;
    }

    static int[] computeOpeningWidths(int[] naturalWidths, ColumnSpec[] specs) {
        int[] widths = new int[specs.length];
        for (int i = 0; i < specs.length; i++) {
            widths[i] = openingWidth(naturalWidths[i], specs[i]);
        }
        return widths;
    }

    private static int openingWidth(int naturalWidth, ColumnSpec spec) {
        return Math.min(spec.maxWidth, Math.max(spec.minWidth, naturalWidth));
    }

    private static int[] minimumColumnWidths(ColumnSpec[] specs) {
        int[] widths = new int[specs.length];
        for (int i = 0; i < specs.length; i++) {
            widths[i] = specs[i].minWidth;
        }
        return widths;
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

    private static void shrinkToFit(int[] widths, ColumnSpec[] specs, double excess) {
        while (excess > 0.5) {
            double totalWeight = 0;
            for (int i = 0; i < widths.length; i++) {
                if (widths[i] > specs[i].minWidth) {
                    totalWeight += specs[i].weight;
                }
            }
            if (totalWeight == 0) {
                break;
            }

            double consumed = 0;
            for (int i = 0; i < widths.length; i++) {
                if (widths[i] <= specs[i].minWidth) {
                    continue;
                }
                int shrink = (int) Math.floor(excess * (specs[i].weight / totalWeight));
                if (shrink <= 0) {
                    continue;
                }
                int updated = Math.max(specs[i].minWidth, widths[i] - shrink);
                consumed += widths[i] - updated;
                widths[i] = updated;
            }

            if (consumed <= 0) {
                shaveRemainderFromWidestColumns(widths, specs, excess);
                break;
            }
            excess -= consumed;
        }
    }

    private static void shaveRemainderFromWidestColumns(int[] widths, ColumnSpec[] specs,
            double excess) {
        while (excess > 0.5) {
            int index = -1;
            int mostAboveMin = 0;
            for (int i = 0; i < widths.length; i++) {
                int aboveMin = widths[i] - specs[i].minWidth;
                if (aboveMin > mostAboveMin) {
                    mostAboveMin = aboveMin;
                    index = i;
                }
            }
            if (index < 0) {
                return;
            }
            int shave = (int) Math.min(Math.ceil(excess), mostAboveMin);
            widths[index] -= shave;
            excess -= shave;
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
