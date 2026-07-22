package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.util.List;

import redactedrice.randomizer.lua.arguments.ArgumentConstraint;
import redactedrice.randomizer.lua.arguments.ArgumentDefinition;
import redactedrice.randomizer.lua.arguments.ArgumentType;
import redactedrice.randomizer.lua.arguments.TypeDefinition;

// Builds the right widget for an argument definition based on its base type and constraint.
public final class ArgumentEditorFactory {
    private ArgumentEditorFactory() {}

    public static ArgumentValueEditor create(ArgumentDefinition argDef) {
        TypeDefinition typeDef = argDef.getTypeDefinition();
        ArgumentType baseType = typeDef.getBaseType();
        if (baseType != ArgumentType.INTEGER && baseType != ArgumentType.DOUBLE) {
            // Not yet supported: STRING, BOOLEAN, ENUM, LIST, MAP, GROUP
            return new UnsupportedValueEditor();
        }
        return createForNumeric(baseType == ArgumentType.INTEGER, typeDef.getConstraint());
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
}
