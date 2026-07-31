package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import redactedrice.randomizer.lua.arguments.TypeDefinition;

public class StructuredTextTest {

    @Test
    void formatsListAndTableValues() {
        TypeDefinition flatList = TypeDefinition.listOf(TypeDefinition.string());
        assertEquals("common, uncommon, rare",
                StructuredText.formatValue(flatList, List.of("common", "uncommon", "rare")));

        TypeDefinition nestedList = TypeDefinition.listOf(TypeDefinition.listOf(TypeDefinition.integer()));
        assertEquals("(1, 2, 3), (4, 5)",
                StructuredText.formatValue(nestedList, List.of(List.of(1, 2, 3), List.of(4, 5))));

        TypeDefinition flatTable =
                TypeDefinition.tableOf(TypeDefinition.string(), TypeDefinition.integer());
        LinkedHashMap<String, Integer> tableValue = new LinkedHashMap<>();
        tableValue.put("fire", 10);
        tableValue.put("water", 5);
        assertEquals("fire \u2192 10, water \u2192 5",
                StructuredText.formatValue(flatTable, tableValue));

        TypeDefinition nestedTableValue = TypeDefinition.tableOf(TypeDefinition.string(),
                TypeDefinition.listOf(TypeDefinition.integer()));
        assertEquals("fire \u2192 (1, 2, 3)",
                StructuredText.formatValue(nestedTableValue, Map.of("fire", List.of(1, 2, 3))));
    }

    @Test
    void emptyCollectionsShowPlaceholderText() {
        TypeDefinition listType = TypeDefinition.listOf(TypeDefinition.string());
        assertEquals(StructuredText.EMPTY_VALUE, StructuredText.formatValue(listType, List.of()));

        TypeDefinition tableType =
                TypeDefinition.tableOf(TypeDefinition.string(), TypeDefinition.integer());
        assertEquals(StructuredText.EMPTY_VALUE, StructuredText.formatValue(tableType, Map.of()));
    }
}
