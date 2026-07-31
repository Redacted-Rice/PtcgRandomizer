package redactedrice.ptcgr.randomizer.actions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

import redactedrice.randomizer.lua.ExecutionRequest;
import redactedrice.randomizer.lua.Module;
import redactedrice.randomizer.lua.arguments.ArgumentConstraint;
import redactedrice.randomizer.lua.arguments.ArgumentDefinition;
import redactedrice.randomizer.lua.arguments.TypeDefinition;

class ActionTest {

    @Test
    void omittedCollectionDefaultsUseEmptyContainers() {
        Module listModule = testModule(
                List.of(new ArgumentDefinition("tags", TypeDefinition.listOf(TypeDefinition.string()), null)));
        Action listAction = new Action(listModule);
        assertTrue(listAction.getArgument("tags") instanceof List);
        assertTrue(((List<?>) listAction.getArgument("tags")).isEmpty());

        Module tableModule = testModule(List.of(new ArgumentDefinition("weights",
                TypeDefinition.tableOf(TypeDefinition.string(), TypeDefinition.integer()), null)));
        Action tableAction = new Action(tableModule);
        assertTrue(tableAction.getArgument("weights") instanceof Map);
        assertTrue(((Map<?, ?>) tableAction.getArgument("weights")).isEmpty());
    }

    @Test
    void toExecutionRequestOmitsNullScalarDefaults() {
        Module module = testModule(
                List.of(new ArgumentDefinition("optionalFlag", TypeDefinition.string(), null)));

        Action action = new Action(module);

        ExecutionRequest request = assertDoesNotThrow(action::toExecutionRequest);
        assertTrue(request.getArguments().isEmpty());
    }

    @Test
    void setArgumentBlankStringForOptionalStringStoresNull() {
        Module module = testModule(
                List.of(new ArgumentDefinition("optionalFlag", TypeDefinition.string(), null)));
        Action action = new Action(module);

        action.setArgument("optionalFlag", "");
        assertEquals(null, action.getArgument("optionalFlag"));

        action.setArgument("optionalFlag", "   ");
        assertEquals(null, action.getArgument("optionalFlag"));

        ExecutionRequest request = assertDoesNotThrow(action::toExecutionRequest);
        assertTrue(request.getArguments().isEmpty());
    }

    @Test
    void setArgumentNonBlankStringForOptionalStringIsStored() {
        Module module = testModule(
                List.of(new ArgumentDefinition("optionalFlag", TypeDefinition.string(), null)));
        Action action = new Action(module);

        action.setArgument("optionalFlag", "enabled");
        assertEquals("enabled", action.getArgument("optionalFlag"));

        ExecutionRequest request = assertDoesNotThrow(action::toExecutionRequest);
        assertEquals("enabled", request.getArguments().get("optionalFlag"));
    }

    @Test
    void setArgumentNullForTableUsesEmptyMap() {
        Module module = testModule(List.of(new ArgumentDefinition("weights",
                TypeDefinition.tableOf(TypeDefinition.string(), TypeDefinition.integer()), Map.of("a", 1))));

        Action action = new Action(module);
        action.setArgument("weights", null);

        assertTrue(((Map<?, ?>) action.getArgument("weights")).isEmpty());
        ExecutionRequest request = assertDoesNotThrow(action::toExecutionRequest);
        assertEquals(Map.of(), request.getArguments().get("weights"));
    }

    @Test
    void setArgumentAllowsBooleanValueRegardlessOfDeclaredEnumConstraint() {
        Module module = testModule(List.of(new ArgumentDefinition("flag",
                TypeDefinition.bool(ArgumentConstraint.enumValues(List.of(false))), false)));
        Action action = new Action(module);

        action.setArgument("flag", true);
        assertEquals(true, action.getArgument("flag"));
    }

    @Test
    void setArgumentRejectsInvalidValue() {
        Module module = testModule(List.of(new ArgumentDefinition("numMoves",
                TypeDefinition.integer(ArgumentConstraint.range(0, 2)), 2)));
        Action action = new Action(module);

        assertThrows(IllegalArgumentException.class, () -> action.setArgument("numMoves", 9));
    }

    @Test
    void setArgumentCoercesCompatibleValue() {
        Module module = testModule(List.of(new ArgumentDefinition("numMoves",
                TypeDefinition.integer(ArgumentConstraint.range(0, 2)), 2)));
        Action action = new Action(module);

        action.setArgument("numMoves", "1");
        assertEquals(1, action.getArgument("numMoves"));
    }

    @Test
    void mutableArgumentValuesAreNotSharedBetweenActions() {
        List<String> sharedDefault = new ArrayList<>(List.of("a", "b"));
        Module module = testModule(List.of(new ArgumentDefinition("tags",
                TypeDefinition.listOf(TypeDefinition.string()), sharedDefault)));

        Action first = new Action(module);
        Action second = new Action(module);
        assertTrue(first.getArgument("tags") != second.getArgument("tags"));
        @SuppressWarnings("unchecked")
        List<String> firstTags = (List<String>) first.getArgument("tags");
        firstTags.add("c");
        assertEquals(List.of("a", "b"), second.getArgument("tags"));

        Action duplicate = first.copy();
        assertTrue(first.getArgument("tags") != duplicate.getArgument("tags"));
        @SuppressWarnings("unchecked")
        List<String> duplicateTags = (List<String>) duplicate.getArgument("tags");
        duplicateTags.add("d");
        assertEquals(List.of("a", "b", "c"), first.getArgument("tags"));
    }

    private static Module testModule(List<ArgumentDefinition> arguments) {
        return new Module("test_module", "Test Module", "", Set.of("dev"), Set.of(), arguments,
                new ZeroArgFunction() {
                    @Override
                    public LuaValue call() {
                        return LuaValue.NIL;
                    }
                }, null, "test.lua", 0, false, false, null, "author", "1.0", Map.of(), null, null,
                null);
    }
}
