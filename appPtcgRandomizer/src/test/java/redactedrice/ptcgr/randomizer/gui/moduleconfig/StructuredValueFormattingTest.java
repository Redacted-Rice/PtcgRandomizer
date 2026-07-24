package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import redactedrice.randomizer.lua.arguments.TypeDefinition;

class StructuredValueFormattingTest {

    @Test
    void flatListSkipsBrackets() {
        TypeDefinition type = TypeDefinition.listOf(TypeDefinition.string());
        assertEquals("common, uncommon, rare",
                StructuredValueFormatting.format(type, List.of("common", "uncommon", "rare")));
    }

    @Test
    void nestedListWrapsEachSublistInParens() {
        TypeDefinition type = TypeDefinition.listOf(TypeDefinition.listOf(TypeDefinition.integer()));
        assertEquals("(1, 2, 3), (4, 5)",
                StructuredValueFormatting.format(type, List.of(List.of(1, 2, 3), List.of(4, 5))));
    }

    @Test
    void flatTableUsesArrowPairs() {
        TypeDefinition type =
                TypeDefinition.tableOf(TypeDefinition.string(), TypeDefinition.integer());
        LinkedHashMap<String, Integer> value = new LinkedHashMap<>();
        value.put("fire", 10);
        value.put("water", 5);
        assertEquals("fire \u2192 10, water \u2192 5",
                StructuredValueFormatting.format(type, value));
    }

    @Test
    void tableWithNestedListValueWrapsListInParens() {
        TypeDefinition type = TypeDefinition.tableOf(TypeDefinition.string(),
                TypeDefinition.listOf(TypeDefinition.integer()));
        assertEquals("fire \u2192 (1, 2, 3)",
                StructuredValueFormatting.format(type, Map.of("fire", List.of(1, 2, 3))));
    }

    @Test
    void emptyListShowsPlaceholderText() {
        TypeDefinition type = TypeDefinition.listOf(TypeDefinition.string());
        assertEquals(StructuredTypeText.EMPTY_VALUE, StructuredValueFormatting.format(type, List.of()));
    }

    @Test
    void emptyTableShowsPlaceholderText() {
        TypeDefinition type =
                TypeDefinition.tableOf(TypeDefinition.string(), TypeDefinition.integer());
        assertEquals(StructuredTypeText.EMPTY_VALUE, StructuredValueFormatting.format(type, Map.of()));
    }
}
