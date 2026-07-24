package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import redactedrice.randomizer.lua.arguments.TypeDefinition;

// Renders LIST/TABLE values as compact read only preview text. Lists are comma separated with
// no surrounding brackets. nested list/table values are wrapped in parens. Tables use "key → val"
// pairs separated by commas, with the same paren wrapping for nested complex values.
final class StructuredValueFormatting {
    private StructuredValueFormatting() {}

    static String format(TypeDefinition typeDefinition, Object value) {
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
            return StructuredTypeText.EMPTY_VALUE;
        }
        TypeDefinition elementType = typeDefinition.getElementType();
        return list.stream().map(element -> formatNested(elementType, element))
                .collect(Collectors.joining(", "));
    }

    private static String formatTable(TypeDefinition typeDefinition, Object value) {
        if (!(value instanceof Map<?, ?> map) || map.isEmpty()) {
            return StructuredTypeText.EMPTY_VALUE;
        }
        TypeDefinition valueType = typeDefinition.getValueType();
        return map.entrySet().stream()
                .map(entry -> String.valueOf(entry.getKey()) + StructuredTypeText.ARROW_SEPARATOR
                        + formatNested(valueType, entry.getValue()))
                .collect(Collectors.joining(", "));
    }

    private static String formatNested(TypeDefinition typeDefinition, Object value) {
        if (typeDefinition.isList() || typeDefinition.isTable()) {
            return "(" + format(typeDefinition, value) + ")";
        }
        return String.valueOf(value);
    }
}
