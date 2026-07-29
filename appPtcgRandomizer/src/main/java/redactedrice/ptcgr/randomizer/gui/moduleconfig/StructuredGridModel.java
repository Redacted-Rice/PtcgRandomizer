package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import redactedrice.randomizer.lua.arguments.TypeDefinition;

// Pure data model for laying out a LIST/TABLE arguments value as a single flattened
// grid. One column per nesting level, one row per leaf entry, with a nested LIST/TABLEs own
// entries occupying a contiguous span of rows under its parent entry's remove/key cell. See
// StructuredGridPanel for the Swing renderer that turns this into a GridBagLayout.
//
// Values are tracked internally as "raw" entries - a List<Object> either way, where a LIST's raw
// entries are just its element values and a TABLEs raw entries are RawEntry(key, value) pairs.
// This intentionally allows transient duplicate table keys (e.g. right after "+ Add", before the
// user renames the new key) - toPublic is where duplicates are finally rejected.
final class StructuredGridModel {
    private StructuredGridModel() {}

    record RawEntry(Object key, Object value) {
    }

    record LayoutControlCounts(int entryBoxes, int removeButtons, int tableLevels) {
    }

    // Counts key/value entry fields, remove buttons, and TABLE levels (each adds an arrow column)
    // required by a type's structured grid layout.
    static LayoutControlCounts layoutControlCounts(TypeDefinition type) {
        if (!type.isList() && !type.isTable()) {
            return new LayoutControlCounts(1, 0, 0);
        }
        TypeDefinition child = type.isTable() ? type.getValueType() : type.getElementType();
        if (type.isTable()) {
            LayoutControlCounts childCounts = layoutControlCounts(child);
            return new LayoutControlCounts(1 + childCounts.entryBoxes,
                    1 + childCounts.removeButtons(), 1 + childCounts.tableLevels());
        }
        if (child.isComplex()) {
            LayoutControlCounts childCounts = layoutControlCounts(child);
            return new LayoutControlCounts(childCounts.entryBoxes(), 1 + childCounts.removeButtons(),
                    childCounts.tableLevels());
        }
        return new LayoutControlCounts(1, 1, 0);
    }

    // Total grid columns a type needs. Content columns first then one trailing "remove"
    // column on the right. TABLE levels add key/arrow before the value/nested band. Nested
    // LIST/TABLE levels add a separator column before their inner band.
    static int totalColumns(TypeDefinition type) {
        int contentColumns;
        TypeDefinition child = type.isTable() ? type.getValueType() : type.getElementType();
        if (type.isTable()) {
            contentColumns = 2;
        } else {
            contentColumns = 0;
        }
        if (child.isComplex()) {
            contentColumns += 1 + totalColumns(child);
        } else {
            contentColumns += 1;
        }
        return contentColumns + 1;
    }

    static int removeColumn(int colOffset, TypeDefinition type) {
        return colOffset + totalColumns(type) - 1;
    }

    // Horizontal rules between entries and above "+ Add" appear only when this level's elements
    // or values are themselves LIST/TABLE — not for flat primitive rows.
    static boolean showsHorizontalSeparators(TypeDefinition type) {
        TypeDefinition child = type.isTable() ? type.getValueType() : type.getElementType();
        return child.isComplex();
    }

    // Converts a public value (List for LIST, Map for TABLE) into the internal raw shape used for
    // structural bookkeeping, recursing into nested LIST/TABLE values.
    static List<Object> toRaw(TypeDefinition type, Object publicValue) {
        List<Object> raw = new ArrayList<>();
        if (type.isList()) {
            TypeDefinition elementType = type.getElementType();
            List<?> list = publicValue instanceof List<?> l ? l : List.of();
            for (Object element : list) {
                raw.add(elementType.isComplex() ? toRaw(elementType, element) : element);
            }
        } else if (type.isTable()) {
            TypeDefinition valueType = type.getValueType();
            Map<?, ?> map = publicValue instanceof Map<?, ?> m ? m : Map.of();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object value = entry.getValue();
                raw.add(new RawEntry(entry.getKey(),
                        valueType.isComplex() ? toRaw(valueType, value) : value));
            }
        }
        return raw;
    }

    // Converts internal raw entries back into the public shape (List/Map), throwing if a TABLE
    // level ended up with duplicate keys.
    static Object toPublic(TypeDefinition type, List<Object> rawEntries) {
        if (type.isTable()) {
            TypeDefinition valueType = type.getValueType();
            LinkedHashMap<Object, Object> result = new LinkedHashMap<>();
            for (Object rawEntry : rawEntries) {
                RawEntry pair = (RawEntry) rawEntry;
                if (result.containsKey(pair.key())) {
                    throw new IllegalArgumentException("Duplicate table key: " + pair.key());
                }
                result.put(pair.key(), toPublicValue(valueType, pair.value()));
            }
            return result;
        }
        TypeDefinition elementType = type.getElementType();
        List<Object> result = new ArrayList<>();
        for (Object rawEntry : rawEntries) {
            result.add(toPublicValue(elementType, rawEntry));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Object toPublicValue(TypeDefinition childType, Object rawValue) {
        return childType.isComplex() ? toPublic(childType, (List<Object>) rawValue) : rawValue;
    }

    // Total grid rows a collection occupies once rendered. Every entry's own rows, optional
    // separator rows between siblings and above "+ Add" when this level holds nested
    // LIST/TABLE values and one trailing "+ Add" row.
    static int rowCount(TypeDefinition type, List<Object> rawEntries) {
        TypeDefinition childType = type.isTable() ? type.getValueType() : type.getElementType();
        int rows = 0;
        for (Object rawEntry : rawEntries) {
            Object childValue = type.isTable() ? ((RawEntry) rawEntry).value() : rawEntry;
            rows += entryRowCount(childType, childValue);
        }
        if (showsHorizontalSeparators(type)) {
            if (rawEntries.size() > 1) {
                rows += rawEntries.size() - 1;
            }
            if (!rawEntries.isEmpty()) {
                rows += 1;
            }
        }
        return rows + 1;
    }

    // Grid rows a single LIST element / TABLE value occupies - 1 for a scalar/enum leaf, or
    // however many rows its own nested collection needs (including its "+ Add" row).
    @SuppressWarnings("unchecked")
    static int entryRowCount(TypeDefinition childType, Object childValue) {
        if (childType.isComplex()) {
            return rowCount(childType, (List<Object>) childValue);
        }
        return 1;
    }
}
