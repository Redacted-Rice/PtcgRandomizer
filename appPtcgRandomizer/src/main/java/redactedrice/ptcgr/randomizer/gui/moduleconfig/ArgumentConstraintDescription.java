package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import redactedrice.randomizer.lua.arguments.ArgumentConstraint;
import redactedrice.randomizer.lua.arguments.ArgumentDefinition;
import redactedrice.randomizer.lua.arguments.ArgumentType;
import redactedrice.randomizer.lua.arguments.ConstraintType;
import redactedrice.randomizer.lua.arguments.TypeDefinition;

// Builds short constraint labels for display in the module config dialog, e.g. "0 - 2",
// "Int min - Int max", or "EntityType". Kept separate from URJ
// ArgumentConstraint.getDescription() since that one is meant for log/error messages rather
// than compact UI display and we have some special handling to do.
final class ArgumentConstraintDescription {
    private ArgumentConstraintDescription() {}

    static String describe(ArgumentDefinition argDef) {
        TypeDefinition typeDef = argDef.getTypeDefinition();
        if (typeDef.isEnum()) {
            return describeEnumType(typeDef.getEnumName());
        }
        return describe(typeDef.getBaseType(), typeDef.getConstraint());
    }

    // Integer ANY has no min/max in the module definition, but the UI treats it as a full int
    // range so editors and constraint labels use the same RANGE path as explicit bounds.
    static ArgumentConstraint forUi(ArgumentType baseType, ArgumentConstraint constraint) {
        if (baseType == ArgumentType.INTEGER && constraint.getType() == ConstraintType.ANY) {
            return ArgumentConstraint.range((double) Integer.MIN_VALUE, (double) Integer.MAX_VALUE);
        }
        return constraint;
    }

    // Right now focused on integer or double only. Will need to expand or create alternate
    // versions when more types are supported
    static String describe(ArgumentType baseType, ArgumentConstraint constraint) {
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
                // Primitive numeric types with an inline values table, e.g.
                // constraint = { type = "enum", values = { ... } }
                return "custom enum";
            case ANY:
            default:
                return "";
        }
    }

    // Enum base type: show the registered enum name when supplied. Otherwise use
    // generic label
    private static String describeEnumType(String enumName) {
        if (enumName != null && !enumName.isBlank()) {
            return enumName;
        }
        return "custom enum";
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
