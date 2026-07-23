package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.util.List;

import redactedrice.randomizer.lua.arguments.ArgumentConstraint;
import redactedrice.randomizer.lua.arguments.ArgumentDefinition;
import redactedrice.randomizer.lua.arguments.ArgumentType;
import redactedrice.randomizer.lua.arguments.ConstraintType;
import redactedrice.randomizer.lua.arguments.TypeDefinition;

// Builds the right widget for an argument definition based on its base type and constraint.
public final class ArgumentEditorFactory {
    private ArgumentEditorFactory() {}

    // Convenience overload for callers that don't need to resolve the ENUM base type
    public static ArgumentValueEditor create(ArgumentDefinition argDef) {
        return create(argDef, null);
    }

    public static ArgumentValueEditor create(ArgumentDefinition argDef,
            EnumValuesProvider enumValuesProvider) {
        TypeDefinition typeDef = argDef.getTypeDefinition();
        if (typeDef.isEnum()) {
            return createForEnumType(typeDef.getEnumName(), enumValuesProvider);
        }
        switch (typeDef.getBaseType()) {
            case INTEGER:
            case DOUBLE:
                return createForNumeric(typeDef.getBaseType() == ArgumentType.INTEGER,
                        typeDef.getConstraint());
            case STRING:
                return createForString(typeDef.getConstraint());
            case BOOLEAN:
                return createForBoolean();
            default:
                // Not yet supported: LIST, MAP, GROUP
                return new UnsupportedValueEditor();
        }
    }

    // Plain, unconstrained integer editor for the seed offset field. Seed offset is a
    // PTCGR specific value (not a module ArgumentDefinition), so it's built directly here.
    public static ArgumentValueEditor forSeedOffset() {
        return new NumberFieldEditor(true, (double) Integer.MIN_VALUE, (double) Integer.MAX_VALUE);
    }

    public static String describeSeedOffset() {
        return "";
    }

    public static String describeConstraint(ArgumentDefinition argDef) {
        return ArgumentConstraintDescription.describe(argDef);
    }

    private static ArgumentValueEditor createForNumeric(boolean integer,
            ArgumentConstraint constraint) {
        constraint = ArgumentConstraintDescription
                .forUi(integer ? ArgumentType.INTEGER : ArgumentType.DOUBLE, constraint);
        switch (constraint.getType()) {
            case RANGE:
                return new NumberFieldEditor(integer, constraint.getMin(), constraint.getMax());
            case DISCRETE_RANGE:
                return DiscreteChoiceEditor.forDiscreteRange(integer, constraint.getMin(),
                        constraint.getMax(), constraint.getStep());
            case ENUM:
                List<Object> allowed = constraint.getAllowedValues();
                return DiscreteChoiceEditor.forEnumValues(integer,
                        allowed != null ? allowed : List.of());
            case ANY:
            default:
                return new NumberFieldEditor(false, null, null);
        }
    }

    // Strings support ANY and ENUM. If its not an enum, its ANY
    private static ArgumentValueEditor createForString(ArgumentConstraint constraint) {
        if (constraint.getType() == ConstraintType.ENUM) {
            List<Object> allowed = constraint.getAllowedValues();
            return new EnumEditor(allowed != null ? allowed : List.of());
        }
        return new StringFieldEditor();
    }

    // Boolean is always an enum
    private static ArgumentValueEditor createForBoolean() {
        return new EnumEditor(ArgumentConstraintDescription.BOOLEAN_VALUES);
    }

    // ENUM type values come from an enum registered elsewhere (e.g. via context.registerEnum
    // in a module's onLoad) rather than from the argument's own definition, so they have to be
    // resolved through the provider instead of the constraint.
    private static ArgumentValueEditor createForEnumType(String enumName,
            EnumValuesProvider enumValuesProvider) {
        List<String> values =
                enumValuesProvider != null ? enumValuesProvider.getEnumValues(enumName) : null;
        if (values == null || values.isEmpty()) {
            // Defensive fallback for an unregistered/misspelled enum name
            return new UnsupportedValueEditor();
        }
        return new EnumEditor(values);
    }
}
