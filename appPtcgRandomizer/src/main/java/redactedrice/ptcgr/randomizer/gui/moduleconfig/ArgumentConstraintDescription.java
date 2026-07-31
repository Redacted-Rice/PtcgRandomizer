package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.structured.StructuredText;
import redactedrice.ptcgr.utils.WarningCollector;
import redactedrice.randomizer.lua.Module;
import redactedrice.randomizer.lua.arguments.ArgumentDefinition;
import redactedrice.randomizer.lua.arguments.ArgumentConstraint;
import redactedrice.randomizer.lua.arguments.ArgumentType;
import redactedrice.randomizer.lua.arguments.ConstraintType;
import redactedrice.randomizer.lua.arguments.TypeDefinition;

// Builds short constraint labels for display in the module config dialog, e.g. "0 - 2",
// "Enum", or "Enum → 0 - 10". Unbounded integers have no constraint label. Kept separate
// from URJ ArgumentConstraint.getDescription() since that one is meant for log/error messages
// rather than compact UI display and we have some special handling to do.
public final class ArgumentConstraintDescription {
    private static final String NONE = "none";

    private ArgumentConstraintDescription() {}

    public static String describe(TypeDefinition typeDef) {
        if (!hasAnyConstraint(typeDef)) {
            return "";
        }
        return formatStructuredConstraints(typeDef);
    }

    public static void checkModuleConstraints(Module module, WarningCollector warnings) {
        if (module == null || warnings == null) {
            return;
        }
        for (ArgumentDefinition argDef : module.getArguments()) {
            checkType(module, argDef.getName(), argDef.getTypeDefinition(), warnings);
        }
    }

    private static void checkType(Module module, String path, TypeDefinition typeDef,
            WarningCollector warnings) {
        if (typeDef.isList()) {
            checkType(module, path + "[]", typeDef.getElementType(), warnings);
            return;
        }
        if (typeDef.isTable()) {
            checkType(module, path + " (key)", typeDef.getKeyType(), warnings);
            checkType(module, path + " (value)", typeDef.getValueType(), warnings);
            return;
        }
        if (!typeDef.isPrimitive() || !typeDef.declaresIgnoredConstraint()) {
            return;
        }
        warnings.addWarning(String.format(
                "Module \"%s\" (%s) argument \"%s\": %s type only supports ANY constraint; ignoring %s.",
                module.getName(), module.getId(), path,
                typeDef.getBaseType().name().toLowerCase(),
                typeDef.getConstraint().getDescription()));
    }

    private static boolean hasAnyConstraint(TypeDefinition typeDef) {
        if (typeDef.isList()) {
            return hasAnyConstraint(typeDef.getElementType());
        }
        if (typeDef.isTable()) {
            return hasAnyConstraint(typeDef.getKeyType())
                    || hasAnyConstraint(typeDef.getValueType());
        }
        if (typeDef.isEnum()) {
            return true;
        }
        return !describePrimitiveConstraint(typeDef.getBaseType(), typeDef.getConstraint())
                .isEmpty();
    }

    private static String formatStructuredConstraints(TypeDefinition typeDef) {
        if (typeDef.isList()) {
            return formatStructuredConstraints(typeDef.getElementType());
        }
        if (typeDef.isTable()) {
            return formatLayer(typeDef.getKeyType()) + StructuredText.ARROW_SEPARATOR
                    + formatLayer(typeDef.getValueType());
        }
        return formatLayer(typeDef);
    }

    private static String formatLayer(TypeDefinition typeDef) {
        if (typeDef.isList() || typeDef.isTable()) {
            return formatStructuredConstraints(typeDef);
        }
        String constraint = describeLeafConstraint(typeDef);
        return constraint.isEmpty() ? NONE : constraint;
    }

    private static String describeLeafConstraint(TypeDefinition typeDef) {
        if (typeDef.isEnum()) {
            return "Enum";
        }
        if (typeDef.isList() || typeDef.isTable()) {
            return describe(typeDef);
        }
        return describePrimitiveConstraint(typeDef.getBaseType(), typeDef.getConstraint());
    }

    private static String describePrimitiveConstraint(ArgumentType baseType,
            ArgumentConstraint constraint) {
        if (baseType == ArgumentType.BOOLEAN) {
            return "";
        }
        if (baseType == ArgumentType.STRING && isNumericConstraint(constraint)) {
            return "";
        }
        if (constraint.getType() == ConstraintType.ANY) {
            return "";
        }
        boolean integer = baseType == ArgumentType.INTEGER;
        switch (constraint.getType()) {
            case RANGE:
                return formatBound(constraint.getMin(), integer) + " - "
                        + formatBound(constraint.getMax(), integer);
            case DISCRETE_RANGE:
                return formatBound(constraint.getMin(), integer) + " - "
                        + formatBound(constraint.getMax(), integer) + " (step "
                        + formatBound(constraint.getStep(), integer) + ")";
            case ENUM:
                return "Enum";
            default:
                return "";
        }
    }

    private static boolean isNumericConstraint(ArgumentConstraint constraint) {
        ConstraintType type = constraint.getType();
        return type == ConstraintType.RANGE || type == ConstraintType.DISCRETE_RANGE;
    }

    private static String formatBound(Double value, boolean integer) {
        if (value == null) {
            return "";
        }
        if (integer) {
            return String.valueOf(value.longValue());
        }
        return String.valueOf(value);
    }
}
