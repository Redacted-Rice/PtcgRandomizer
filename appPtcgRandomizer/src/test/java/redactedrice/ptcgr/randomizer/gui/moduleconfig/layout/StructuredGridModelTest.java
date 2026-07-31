package redactedrice.ptcgr.randomizer.gui.moduleconfig.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.LayoutControlCounts;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.RawEntry;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.StructuredGridModel;
import redactedrice.randomizer.lua.arguments.TypeDefinition;

public class StructuredGridModelTest {

    @Test
    void layoutHelpersMatchStructuredGridShape() {
        TypeDefinition listOfString = TypeDefinition.listOf(TypeDefinition.string());
        assertEquals(new LayoutControlCounts(1, 1, 0),
                StructuredGridModel.layoutControlCounts(listOfString));
        assertEquals(2, StructuredGridModel.totalColumns(listOfString));

        TypeDefinition flatTable =
                TypeDefinition.tableOf(TypeDefinition.string(), TypeDefinition.integer());
        assertEquals(4, StructuredGridModel.totalColumns(flatTable));
        assertEquals(new LayoutControlCounts(2, 1, 1),
                StructuredGridModel.layoutControlCounts(flatTable));

        TypeDefinition listOfList =
                TypeDefinition.listOf(TypeDefinition.listOf(TypeDefinition.integer()));
        assertEquals(4, StructuredGridModel.totalColumns(listOfList));

        TypeDefinition tableOfList = TypeDefinition.tableOf(TypeDefinition.string(),
                TypeDefinition.listOf(TypeDefinition.integer()));
        assertEquals(6, StructuredGridModel.totalColumns(tableOfList));
    }

    @Test
    void rowCountsMatchCollectionShape() {
        TypeDefinition listOfString = TypeDefinition.listOf(TypeDefinition.string());
        assertEquals(1, StructuredGridModel.rowCount(listOfString, List.of()));
        assertEquals(2, StructuredGridModel.rowCount(listOfString, List.of("a")));
        assertEquals(4, StructuredGridModel.rowCount(listOfString, List.of("a", "b", "c")));
        assertFalse(StructuredGridModel.showsHorizontalSeparators(listOfString));

        TypeDefinition listOfList =
                TypeDefinition.listOf(TypeDefinition.listOf(TypeDefinition.integer()));
        assertTrue(StructuredGridModel.showsHorizontalSeparators(listOfList));

        List<Object> raw = List.of(List.of(1, 2, 3), List.of(4));
        assertEquals(4,
                StructuredGridModel.entryRowCount(listOfList.getElementType(), List.of(1, 2, 3)));
        assertEquals(2, StructuredGridModel.entryRowCount(listOfList.getElementType(), List.of(4)));
        assertEquals(9, StructuredGridModel.rowCount(listOfList, raw));
        assertEquals(1, StructuredGridModel.entryRowCount(TypeDefinition.integer(), 1));
    }

    @Test
    void toRawAndToPublicRoundTripValues() {
        TypeDefinition listType = TypeDefinition.listOf(TypeDefinition.string());
        List<Object> listRaw = StructuredGridModel.toRaw(listType, List.of("a", "b"));
        assertEquals(List.of("a", "b"), listRaw);
        assertEquals(List.of("a", "b"), StructuredGridModel.toPublic(listType, listRaw));

        TypeDefinition tableType =
                TypeDefinition.tableOf(TypeDefinition.string(), TypeDefinition.integer());
        Map<String, Integer> tableValue = new LinkedHashMap<>();
        tableValue.put("fire", 10);
        tableValue.put("water", 5);
        List<Object> tableRaw = StructuredGridModel.toRaw(tableType, tableValue);
        assertEquals(new RawEntry("fire", 10), tableRaw.get(0));
        assertEquals(new RawEntry("water", 5), tableRaw.get(1));
        assertEquals(tableValue, StructuredGridModel.toPublic(tableType, tableRaw));

        TypeDefinition nestedTableType = TypeDefinition.tableOf(TypeDefinition.string(),
                TypeDefinition.listOf(TypeDefinition.integer()));
        Map<String, Object> nestedValue = new LinkedHashMap<>();
        nestedValue.put("fire", List.of(1, 2));
        List<Object> nestedRaw = StructuredGridModel.toRaw(nestedTableType, nestedValue);
        assertEquals(nestedValue, StructuredGridModel.toPublic(nestedTableType, nestedRaw));

        List<Object> duplicateKeys = List.of(new RawEntry("dup", 1), new RawEntry("dup", 2));
        assertThrows(IllegalArgumentException.class,
                () -> StructuredGridModel.toPublic(tableType, duplicateKeys));
    }
}
