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
    void omittedTableDefaultUsesEmptyMap() {
        Module module = testModule(List.of(new ArgumentDefinition("weights",
                TypeDefinition.tableOf(TypeDefinition.string(), TypeDefinition.integer()), null)));

        Action action = new Action(module);

        assertTrue(action.getArgument("weights") instanceof Map);
        assertTrue(((Map<?, ?>) action.getArgument("weights")).isEmpty());
    }

    @Test
    void omittedListDefaultUsesEmptyList() {
        Module module = testModule(
                List.of(new ArgumentDefinition("tags", TypeDefinition.listOf(TypeDefinition.string()), null)));

        Action action = new Action(module);

        assertTrue(action.getArgument("tags") instanceof List);
        assertTrue(((List<?>) action.getArgument("tags")).isEmpty());
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
    void separateActionsDoNotShareListDefaults() {
        List<String> sharedDefault = new ArrayList<>(List.of("a", "b"));
        Module module = testModule(List.of(new ArgumentDefinition("tags",
                TypeDefinition.listOf(TypeDefinition.string()), sharedDefault)));

        Action first = new Action(module);
        Action second = new Action(module);

        assertEquals(List.of("a", "b"), first.getArgument("tags"));
        assertEquals(List.of("a", "b"), second.getArgument("tags"));
        assertTrue(first.getArgument("tags") != second.getArgument("tags"));

        @SuppressWarnings("unchecked")
        List<String> firstTags = (List<String>) first.getArgument("tags");
        firstTags.add("c");
        assertEquals(List.of("a", "b"), second.getArgument("tags"));
    }

    @Test
    void copyDoesNotShareMutableArgumentValues() {
        List<String> sharedDefault = new ArrayList<>(List.of("a", "b"));
        Module module = testModule(List.of(new ArgumentDefinition("tags",
                TypeDefinition.listOf(TypeDefinition.string()), sharedDefault)));

        Action original = new Action(module);
        Action duplicate = original.copy();

        assertTrue(original.getArgument("tags") != duplicate.getArgument("tags"));
        @SuppressWarnings("unchecked")
        List<String> duplicateTags = (List<String>) duplicate.getArgument("tags");
        duplicateTags.add("c");
        assertEquals(List.of("a", "b"), original.getArgument("tags"));
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
