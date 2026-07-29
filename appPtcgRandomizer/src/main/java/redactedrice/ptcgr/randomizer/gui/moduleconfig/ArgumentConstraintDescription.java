package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.util.List;
import java.util.stream.Collectors;

import redactedrice.randomizer.lua.arguments.ArgumentConstraint;
import redactedrice.randomizer.lua.arguments.ArgumentDefinition;
import redactedrice.randomizer.lua.arguments.ArgumentType;
import redactedrice.randomizer.lua.arguments.ConstraintType;
import redactedrice.randomizer.lua.arguments.TypeDefinition;

// Builds short constraint labels for display in the module config dialog, e.g. "0 - 2",
// "Int min - Int max", "true / false", or "EntityType". Kept separate from URJ
// ArgumentConstraint.getDescription() since that one is meant for log/error messages rather
// than compact UI display and we have some special handling to do.
final class ArgumentConstraintDescription {
    // Boolean is always a choice between exactly these two values regardless of what the argument
    // definition's other values as this is the only thing that makes sense for a configurable bool
    static final List<Object> BOOLEAN_VALUES = List.of(Boolean.TRUE, Boolean.FALSE);
    private static final String BOOLEAN_DESCRIPTION =
            BOOLEAN_VALUES.stream().map(String::valueOf).collect(Collectors.joining(" / "));

    private ArgumentConstraintDescription() {}

    static String describe(ArgumentDefinition argDef) {
        TypeDefinition typeDef = argDef.getTypeDefinition();
        if (typeDef.isList() || typeDef.isTable() || typeDef.isEnum()) {
            return "";
        }
        return describe(typeDef.getBaseType(), typeDef.getConstraint());
    }

    // Integer ANY has no min/max in the module definition, but the UI treats it as a full int
    // range so editors and constraint labels use the same RANGE path as explicit bounds. Boolean
    // are similarly coerced/forced but that is done before this method is even called so no
    // logic needs to be here
    static ArgumentConstraint forUi(ArgumentType baseType, ArgumentConstraint constraint) {
        if (baseType == ArgumentType.INTEGER && constraint.getType() == ConstraintType.ANY) {
            return ArgumentConstraint.range((double) Integer.MIN_VALUE, (double) Integer.MAX_VALUE);
        }
        return constraint;
    }

    static String describe(ArgumentType baseType, ArgumentConstraint constraint) {
        if (baseType == ArgumentType.BOOLEAN) {
            return BOOLEAN_DESCRIPTION;
        }
        boolean integer = baseType == ArgumentType.INTEGER;
        constraint = forUi(baseType, constraint);
        switch (constraint.getType()) {
            case RANGE:
                return formatBound(constraint.getMin(), integer) + " - "
                        + formatBound(constraint.getMax(), integer);
            case DISCRETE_RANGE:
                return formatBound(constraint.getMin(), integer) + " - "
                        + formatBound(constraint.getMax(), integer) + " (step "
                        + formatBound(constraint.getStep(), integer) + ")";
            case ENUM:
                // Other primitive types with an inline values table, e.g.
                // constraint = { type = "enum", values = { ... } }
                return "Custom enum";
            case ANY:
            default:
                return "";
        }
    }

    // Special formatting for bound values for ranges
    static String formatBound(Double value, boolean integer) {
        if (value == null) {
            return "";
        }
        if (integer) {
            long asLong = value.longValue();
            if (asLong == Integer.MIN_VALUE) {
                return "Int min";
            }
            if (asLong == Integer.MAX_VALUE) {
                return "Int max";
            }
            return String.valueOf(asLong);
        }
        return String.valueOf(value);
    }
}
