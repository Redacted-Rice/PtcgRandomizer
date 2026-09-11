package redactedrice.ptcgr.configs.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

class ActionConfigTest {
    @Test
    void convertToYamlMapSavesModuleVersion() {
        ActionConfig actionPreset =
                new ActionConfig("hp_cards_together_and_stage", "0.1", ActionArgumentsConfig.empty());
        Map<String, Object> node = actionPreset.convertToYamlMap();
        assertEquals("hp_cards_together_and_stage", node.get("module"));
        assertEquals("0.1", node.get("version"));
    }

    @Test
    void convertToYamlMapWritesUnknownWhenVersionMissing() {
        ActionConfig actionPreset =
                new ActionConfig("hp_cards_together_and_stage", null, ActionArgumentsConfig.empty());
        Map<String, Object> node = actionPreset.convertToYamlMap();
        assertEquals("<unknown>", node.get("version"));
    }
}
