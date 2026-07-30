package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
        return createForType(argDef.getTypeDefinition(), enumValuesProvider);
    }

    // Builds an editor directly from a TypeDefinition rather than a full ArgumentDefinition.
    // Used for top level module arguments and for TABLE keys - LIST/TABLE elements and values
    // are handled directly by StructuredGridPanels own recursion instead of by recursing through
    // here since they need row/column level control rather than an independent
    // ArgumentValueEditor component.
    static ArgumentValueEditor createForType(TypeDefinition typeDef,
            EnumValuesProvider enumValuesProvider) {
        return visitType(typeDef, new StructuredTypeVisitor<>() {
            @Override
            public ArgumentValueEditor visitList(TypeDefinition type) {
                return new StructuredGridPanel(type, enumValuesProvider);
            }

            @Override
            public ArgumentValueEditor visitTable(TypeDefinition type) {
                return new StructuredGridPanel(type, enumValuesProvider);
            }

            @Override
            public ArgumentValueEditor visitEnum(TypeDefinition type) {
                return createForEnumType(type.getEnumName(), enumValuesProvider);
            }

            @Override
            public ArgumentValueEditor visitBase(TypeDefinition type) {
                switch (type.getBaseType()) {
                    case INTEGER:
                    case DOUBLE:
                        return createForNumeric(type.getBaseType() == ArgumentType.INTEGER,
                                type.getConstraint());
                    case STRING:
                        return createForString(type.getConstraint());
                    case BOOLEAN:
                        return createForBoolean();
                    default:
                        // ENUM base type without a resolvable value list (see createForEnumType)
                        return new UnsupportedValueEditor();
                }
            }
        });
    }

    // Seed value used when a new row is added to a LIST or TABLE, so its editor starts with
    // something valid rather than null, which most editors can't render/save.
    static Object defaultValueFor(TypeDefinition typeDef, EnumValuesProvider enumValuesProvider) {
        return visitType(typeDef, new StructuredTypeVisitor<>() {
            @Override
            public Object visitList(TypeDefinition type) {
                return new ArrayList<>();
            }

            @Override
            public Object visitTable(TypeDefinition type) {
                return new LinkedHashMap<>();
            }

            @Override
            public Object visitEnum(TypeDefinition type) {
                List<String> values = enumValuesProvider != null
                        ? enumValuesProvider.getEnumValues(type.getEnumName())
                        : null;
                return values != null && !values.isEmpty() ? values.get(0) : "";
            }

            @Override
            public Object visitBase(TypeDefinition type) {
                switch (type.getBaseType()) {
                    case STRING:
                        return "";
                    case INTEGER:
                        return 0;
                    case DOUBLE:
                        return 0.0;
                    case BOOLEAN:
                        return Boolean.FALSE;
                    default:
                        return null;
                }
            }
        });
    }

    // Plain, unconstrained integer editor for the seed offset field. Seed offset is a
    // PTCGR specific value (not a module ArgumentDefinition), so it's built directly here.
    public static ArgumentValueEditor forSeedOffset() {
        return new NumberFieldEditor(true, (double) Integer.MIN_VALUE, (double) Integer.MAX_VALUE);
    }

    public static String describeType(ArgumentDefinition argDef) {
        return StructuredTypeText.describeStructuredShape(argDef.getTypeDefinition());
    }

    public static String describeConstraint(ArgumentDefinition argDef) {
        return ArgumentConstraintDescription.describe(argDef);
    }

    private static <T> T visitType(TypeDefinition typeDef, StructuredTypeVisitor<T> visitor) {
        if (typeDef.isList()) {
            return visitor.visitList(typeDef);
        }
        if (typeDef.isTable()) {
            return visitor.visitTable(typeDef);
        }
        if (typeDef.isEnum()) {
            return visitor.visitEnum(typeDef);
        }
        return visitor.visitBase(typeDef);
    }

    private interface StructuredTypeVisitor<T> {
        T visitList(TypeDefinition typeDef);

        T visitTable(TypeDefinition typeDef);

        T visitEnum(TypeDefinition typeDef);

        T visitBase(TypeDefinition typeDef);
    }

    private static ArgumentValueEditor createForNumeric(boolean integer,
            ArgumentConstraint constraint) {
        constraint = ArgumentConstraintDescription
                .forUi(integer ? ArgumentType.INTEGER : ArgumentType.DOUBLE, constraint);
        switch (constraint.getType()) {
            case RANGE:
                return new NumberFieldEditor(integer, constraint.getMin(), constraint.getMax());
            case DISCRETE_RANGE:
                double min = constraint.getMin();
                double max = constraint.getMax();
                double step = constraint.getStep();
                if (DiscreteChoiceEditor.prefersDropdownForDiscreteRange(min, max, step)) {
                    return DiscreteChoiceEditor.forDiscreteRange(integer, min, max, step);
                }
                return new NumberFieldEditor(integer, min, max, step);
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
