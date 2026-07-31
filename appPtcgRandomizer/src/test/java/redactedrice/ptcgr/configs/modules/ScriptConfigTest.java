package redactedrice.ptcgr.configs.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

class ScriptConfigTest {
    @Test
    void convertToYamlMapSavesModuleVersion() {
        ScriptConfig scriptPreset = new ScriptConfig("changedetector_setup", "0.1");
        Map<String, Object> node = scriptPreset.convertToYamlMap();
        assertEquals("changedetector_setup", node.get("module"));
        assertEquals("0.1", node.get("version"));
    }
}
