package redactedrice.ptcgr.configs.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.randomizer.lua.Module;
import redactedrice.randomizer.lua.arguments.ArgumentDefinition;
import redactedrice.randomizer.lua.arguments.TypeDefinition;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

class ActionArgumentsConfigTest {
    @Test
    void fromActionSavesStoredModuleArguments() {
        Module module = new Module("set_num_moves", "set_num_moves", "", java.util.Set.of("dev"),
                List.of(new ArgumentDefinition("numMoves",
                        TypeDefinition.integer(), 2)),
                new ZeroArgFunction() {
                    @Override
                    public LuaValue call() {
                        return LuaValue.NIL;
                    }
                }, null, "test.lua", 0, true, true, null, "author", "0.9", Map.of(), null, null, null, null, null);
        Action action = new Action(module);
        action.setArgument("numMoves", 1);

        ActionArgumentsConfig saved = ActionArgumentsConfig.fromAction(action);
        Map<String, Object> yamlArgs = saved.convertToYamlMap();
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) yamlArgs.get("arguments");
        assertEquals(1, arguments.get("numMoves"));
    }
}
