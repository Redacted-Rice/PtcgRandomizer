package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import redactedrice.randomizer.lua.arguments.TypeDefinition;

// Shared text for structured argument types - constraint labels, read only previews, and inline
// editor separators. Kept separate from Swing layout helpers so non UI code can format types
// without depending on StructuredInlineEditorSupport.
final class StructuredTypeText {
    static final String ARROW = "\u2192";
    static final String ARROW_SEPARATOR = " " + ARROW + " ";
    static final String EMPTY_VALUE = "(empty)";

    private StructuredTypeText() {}

    static String describeListType(TypeDefinition typeDef) {
        return "List of " + describeStructuredShape(typeDef.getElementType());
    }

    static String describeTableType(TypeDefinition typeDef) {
        return describeStructuredShape(typeDef.getKeyType()) + ARROW_SEPARATOR
                + describeStructuredShape(typeDef.getValueType());
    }

    static String describeStructuredShape(TypeDefinition typeDef) {
        if (typeDef.isList()) {
            return describeListType(typeDef);
        }
        if (typeDef.isTable()) {
            return describeTableType(typeDef);
        }
        return describeScalarShape(typeDef);
    }

    static String describeEnumType(String enumName) {
        if (enumName != null && !enumName.isBlank()) {
            return enumName;
        }
        return "custom enum";
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
