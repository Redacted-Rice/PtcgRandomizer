package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import redactedrice.randomizer.lua.arguments.TypeDefinition;

// Type shape labels and compact read only previews for LIST/TABLE argument values.
public final class StructuredText {
    public static final String ARROW = "\u2192";
    public static final String ARROW_SEPARATOR = " " + ARROW + " ";
    public static final String EMPTY_VALUE = "(empty)";

    private StructuredText() {}

    public static String describeStructuredShape(TypeDefinition typeDef) {
        if (typeDef.isList()) {
            return "List of " + describeStructuredShape(typeDef.getElementType());
        }
        if (typeDef.isTable()) {
            return describeStructuredShape(typeDef.getKeyType()) + ARROW_SEPARATOR
                    + describeStructuredShape(typeDef.getValueType());
        }
        return describeScalarShape(typeDef);
    }

    public static String formatValue(TypeDefinition typeDefinition, Object value) {
        if (typeDefinition.isList()) {
            return formatList(typeDefinition, value);
        }
        if (typeDefinition.isTable()) {
            return formatTable(typeDefinition, value);
        }
        return String.valueOf(value);
    }

    private static String formatList(TypeDefinition typeDefinition, Object value) {
        if (!(value instanceof List<?> list) || list.isEmpty()) {
            return EMPTY_VALUE;
        }
        TypeDefinition elementType = typeDefinition.getElementType();
        return list.stream().map(element -> formatNested(elementType, element))
                .collect(Collectors.joining(", "));
    }

    private static String formatTable(TypeDefinition typeDefinition, Object value) {
        if (!(value instanceof Map<?, ?> map) || map.isEmpty()) {
            return EMPTY_VALUE;
        }
        TypeDefinition valueType = typeDefinition.getValueType();
        return map.entrySet().stream()
                .map(entry -> String.valueOf(entry.getKey()) + ARROW_SEPARATOR
                        + formatNested(valueType, entry.getValue()))
                .collect(Collectors.joining(", "));
    }

    private static String formatNested(TypeDefinition typeDefinition, Object value) {
        if (typeDefinition.isList() || typeDefinition.isTable()) {
            return "(" + formatValue(typeDefinition, value) + ")";
        }
        return String.valueOf(value);
    }

    private static String describeEnumType(String enumName) {
        if (enumName != null && !enumName.isBlank()) {
            return enumName;
        }
        return "Custom enum";
    }

    private static String describeScalarShape(TypeDefinition typeDef) {
        if (typeDef.isEnum()) {
            return describeEnumType(typeDef.getEnumName());
        }
        switch (typeDef.getBaseType()) {
            case STRING:
                return "string";
            case INTEGER:
                return "int";
            case DOUBLE:
                return "double";
            case BOOLEAN:
                return "bool";
            default:
                return typeDef.getBaseType().name().toLowerCase();
        }
    }
}
