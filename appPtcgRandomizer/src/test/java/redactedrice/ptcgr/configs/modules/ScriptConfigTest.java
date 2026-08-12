package redactedrice.ptcgr.configs.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.randomizer.lua.Module;
import redactedrice.randomizer.utils.IssueTracker;

class ScriptConfigTest {
    @Test
    void convertToYamlMapSavesModuleVersion() {
        ScriptConfig scriptPreset = new ScriptConfig("changedetector_setup", "0.1");
        Map<String, Object> node = scriptPreset.convertToYamlMap();
        assertEquals("changedetector_setup", node.get("module"));
        assertEquals("0.1", node.get("version"));
    }

    @Test
    void checkRequiredPassesWhenSavedScriptsMatchApp() {
        Module setup = scriptWithVersion("changedetector_setup", "0.1", "randomize");
        Module detect = scriptWithVersion("changedetector_detect", "0.2", "module");
        ActionBank actionBank = testActionBank(List.of(setup), List.of(detect));

        IssueTracker.clear();
        ScriptConfig.checkRequired("prescripts",
                List.of(new ScriptConfig("changedetector_setup", "0.1")), actionBank);
        ScriptConfig.checkRequired("postscripts",
                List.of(new ScriptConfig("changedetector_detect", "0.2")), actionBank);

        assertFalse(IssueTracker.hasWarnings());
    }

    @Test
    void checkRequiredWarnsWhenSavedScriptMissingFromApp() {
        ActionBank actionBank = testActionBank(List.of(), List.of());

        IssueTracker.clear();
        ScriptConfig.checkRequired("prescripts",
                List.of(new ScriptConfig("missing_prescript", "0.1")), actionBank);

        assertTrue(IssueTracker.getWarnings().stream()
                .anyMatch(w -> w.contains("missing_prescript") && w.contains("not loaded")));
    }

    @Test
    void checkRequiredWarnsOnVersionMismatch() {
        Module setup = scriptWithVersion("changedetector_setup", "0.2", "randomize");
        ActionBank actionBank = testActionBank(List.of(setup), List.of());

        IssueTracker.clear();
        ScriptConfig.checkRequired("prescripts",
                List.of(new ScriptConfig("changedetector_setup", "0.0")), actionBank);

        assertTrue(IssueTracker.getWarnings().stream()
                .anyMatch(w -> w.contains("changedetector_setup") && w.contains("0.0")));
    }

    private static ActionBank testActionBank(List<Module> preScripts, List<Module> postScripts) {
        return new ActionBank(null) {
            @Override
            public Module getScript(String id) {
                for (Module script : preScripts) {
                    if (script.getId().equals(id)) {
                        return script;
                    }
                }
                for (Module script : postScripts) {
                    if (script.getId().equals(id)) {
                        return script;
                    }
                }
                return null;
            }

            @Override
            public List<Module> getPreScripts() {
                return preScripts;
            }

            @Override
            public List<Module> getPostScripts() {
                return postScripts;
            }
        };
    }

    private static Module scriptWithVersion(String id, String version, String when) {
        return new Module(id, id, "", Set.of(), List.of(), new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.NIL;
            }
        }, null, "test.lua", 0, false, false, when, "author", version, Map.of(), null, null, null,
                null, null);
    }
}
