package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

final class NumericChoiceMatching {
    private NumericChoiceMatching() {}

    static double applyBoundsAndStep(double value, Double min, Double max, Double step) {
        Double exceededBound = exceededBound(value, min, max);
        if (exceededBound != null) {
            value = exceededBound;
        }
        return snapToStep(value, min, max, step);
    }

    static Number nearestChoice(Number value, boolean integer, List<Number> choices) {
        if (choices.isEmpty()) {
            return normalize(integer, value);
        }

        Number target = normalize(integer, value);
        Number nearest = choices.get(0);
        double bestDistance = Math.abs(nearest.doubleValue() - target.doubleValue());
        for (int i = 1; i < choices.size(); i++) {
            Number choice = choices.get(i);
            double distance = Math.abs(choice.doubleValue() - target.doubleValue());
            if (distance < bestDistance || distance == bestDistance
                    && choice.doubleValue() > nearest.doubleValue()) {
                nearest = choice;
                bestDistance = distance;
            }
        }
        return nearest;
    }

    private static double snapToStep(double value, Double min, Double max, Double step) {
        if (step == null || step <= 0 || min == null) {
            return value;
        }

        BigDecimal minBd = BigDecimal.valueOf(min);
        BigDecimal stepBd = BigDecimal.valueOf(step);
        BigDecimal snapped = minBd.add(offsetSteps(value, minBd, stepBd));

        if (max != null) {
            BigDecimal maxBd = BigDecimal.valueOf(max);
            if (snapped.compareTo(maxBd) > 0) {
                snapped = maxBd;
            }
        }
        if (snapped.compareTo(minBd) < 0) {
            snapped = minBd;
        }
        return snapped.doubleValue();
    }

    private static BigDecimal offsetSteps(double value, BigDecimal minBd, BigDecimal stepBd) {
        BigDecimal offset = BigDecimal.valueOf(value).subtract(minBd);
        long stepIndex = offset.divide(stepBd, 0, RoundingMode.HALF_UP).longValue();
        return stepBd.multiply(BigDecimal.valueOf(stepIndex));
    }

    private static Double exceededBound(double number, Double min, Double max) {
        if (Double.isNaN(number)) {
            return min != null ? min : max;
        }
        if (Double.isInfinite(number)) {
            if (number > 0) {
                return max;
            }
            return min;
        }
        if (min != null && number < min) {
            return min;
        }
        if (max != null && number > max) {
            return max;
        }
        return null;
    }

    static Number normalize(boolean integer, Number value) {
        return integer ? (Number) value.intValue() : (Number) value.doubleValue();
    }
}
