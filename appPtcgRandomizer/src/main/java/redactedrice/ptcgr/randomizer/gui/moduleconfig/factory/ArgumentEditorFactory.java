package redactedrice.ptcgr.randomizer.gui.moduleconfig.factory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.ArgumentValueEditor;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.EnumValuesProvider;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.editor.DisplayChoice;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.editor.DiscreteChoiceEditor;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.editor.EnumEditor;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.editor.NumberFieldEditor;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.editor.StringFieldEditor;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.editor.UnsupportedValueEditor;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.StructuredGridPanel;
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
    public static ArgumentValueEditor createForType(TypeDefinition typeDef,
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
                return createForEnumType(type, enumValuesProvider);
            }

            @Override
            public ArgumentValueEditor visitBase(TypeDefinition type) {
                switch (type.getBaseType()) {
                    case INTEGER:
                    case DOUBLE:
                        return createForNumeric(type);
                    case STRING:
                        return createForString(type);
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
    public static Object defaultValueFor(TypeDefinition typeDef,
            EnumValuesProvider enumValuesProvider) {
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
                List<String> values = resolveEnumChoices(type, enumValuesProvider);
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

    private static ArgumentValueEditor createForNumeric(TypeDefinition type) {
        boolean integer = type.getBaseType() == ArgumentType.INTEGER;
        ArgumentConstraint constraint = type.getEnforcedConstraint();
        if (integer && constraint.getType() == ConstraintType.ANY) {
            return new NumberFieldEditor(true, (double) Integer.MIN_VALUE,
                    (double) Integer.MAX_VALUE);
        }
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

    private static ArgumentValueEditor createForString(TypeDefinition type) {
        ArgumentConstraint constraint = type.getEnforcedConstraint();
        if (constraint.getType() == ConstraintType.ENUM) {
            List<Object> allowed = constraint.getAllowedValues();
            return new EnumEditor(allowed != null ? allowed : List.of());
        }
        return new StringFieldEditor();
    }

    private static ArgumentValueEditor createForBoolean() {
        return new EnumEditor(List.of(Boolean.TRUE, Boolean.FALSE));
    }

    // ENUM type values come from an enum registered elsewhere (e.g. via context.registerEnum
    // in a module's onLoad) rather than from the argument's own definition, so they have to be
    // resolved through the provider instead of the constraint. Optional allow/exclude lists on
    // the type definition then filter that registered set.
    private static ArgumentValueEditor createForEnumType(TypeDefinition type,
            EnumValuesProvider enumValuesProvider) {
        List<DisplayChoice> choices = resolveEnumDisplayChoices(type, enumValuesProvider);
        if (choices == null || choices.isEmpty()) {
            // Defensive fallback for an unregistered/misspelled enum name
            return new UnsupportedValueEditor();
        }
        return new EnumEditor(choices);
    }

    private static List<DisplayChoice> resolveEnumDisplayChoices(TypeDefinition type,
            EnumValuesProvider enumValuesProvider) {
        List<String> values = resolveEnumChoices(type, enumValuesProvider);
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        String enumName = type.getEnumName();
        return values.stream()
                .map(value -> new DisplayChoice(value,
                        enumValuesProvider.getEnumValueDisplayName(enumName, value)))
                .toList();
    }

    private static List<String> resolveEnumChoices(TypeDefinition type,
            EnumValuesProvider enumValuesProvider) {
        if (type == null || enumValuesProvider == null) {
            return null;
        }
        List<String> values = enumValuesProvider.getEnumValues(type.getEnumName());
        if (values == null || values.isEmpty()) {
            return values;
        }
        ArgumentConstraint constraint = type.getEnforcedConstraint();
        if (constraint == null || constraint.getType() == ConstraintType.ANY) {
            return values;
        }
        return constraint.filterEnumValues(values,
                enumValuesProvider.getEnumDefinition(type.getEnumName()));
    }
}
