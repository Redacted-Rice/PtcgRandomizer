package redactedrice.ptcgr.randomizer.gui.moduleconfig.dialog;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import redactedrice.ptcgr.configs.modules.ActionArgumentsConfig;
import redactedrice.ptcgr.configs.modules.ActionConfig;
import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.ModuleConfigEndToEndSupport;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.ModuleConfigGuiTestSupport;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.DevModuleEnvironment;
import redactedrice.ptcgr.utils.WarningCollector;
import redactedrice.randomizer.lua.ExecutionRequest;

class ModuleConfigDialogTest extends ModuleConfigGuiTestSupport {
    private DevModuleEnvironment environment;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        environment = ModuleConfigEndToEndSupport.loadDevModules(
                testInfo.getTestMethod().get().getName());
    }

    @AfterEach
    void tearDown() {
        ModuleConfigEndToEndSupport.clearDevModulesProperty();
    }

    @Test
    void editIntegerArgumentsThroughDialogRoundTripsPreset() {
        ActionBank actionBank = environment.actionBank();
        Action action = ModuleConfigEndToEndSupport.newAction(actionBank, "dev_test_int_args");

        ModuleConfigDialog dialog = ModuleConfigEndToEndSupport.openEditableDialog(actionBank, action);
        dialog.argumentEditor("anyInt").setValue(42);
        dialog.argumentEditor("rangeInt").setValue(8);
        dialog.argumentEditor("discreteInt").setValue(30);
        dialog.argumentEditor("enumInt").setValue(5);
        dialog.confirmEdits();

        assertEquals(42, action.getArgument("anyInt"));
        assertEquals(8, action.getArgument("rangeInt"));
        assertEquals(30, action.getArgument("discreteInt"));
        assertEquals(5, action.getArgument("enumInt"));

        ActionConfig preset = ActionConfig.fromAction(action);
        WarningCollector warnings = new WarningCollector(null);
        Action reloaded = preset.toAction(actionBank, warnings);
        assertFalse(warnings.hasWarnings());
        assertEquals(42, reloaded.getArgument("anyInt"));
        assertEquals(8, reloaded.getArgument("rangeInt"));
        assertEquals(30, reloaded.getArgument("discreteInt"));
        assertEquals(5, reloaded.getArgument("enumInt"));

        @SuppressWarnings("unchecked")
        Map<String, Object> yamlArguments =
                (Map<String, Object>) preset.convertToYamlMap().get("arguments");
        assertEquals(42, yamlArguments.get("anyInt"));
        assertEquals(8, yamlArguments.get("rangeInt"));
    }

    @Test
    void editStructuredArgumentsThroughDialogRoundTripsPreset() {
        ActionBank actionBank = environment.actionBank();
        Action action = ModuleConfigEndToEndSupport.newAction(actionBank, "dev_test_list_args");

        ModuleConfigDialog dialog = ModuleConfigEndToEndSupport.openEditableDialog(actionBank, action);
        dialog.argumentEditor("anyStringList").setValue(List.of("alpha", "beta"));
        dialog.argumentEditor("rangeIntList").setValue(List.of(20, 40));
        dialog.confirmEdits();

        assertEquals(List.of("alpha", "beta"), action.getArgument("anyStringList"));
        assertEquals(List.of(20, 40), action.getArgument("rangeIntList"));

        ActionConfig preset = ActionConfig.fromAction(action);
        WarningCollector warnings = new WarningCollector(null);
        Action reloaded = preset.toAction(actionBank, warnings);
        assertFalse(warnings.hasWarnings());
        assertEquals(List.of("alpha", "beta"), reloaded.getArgument("anyStringList"));
        assertEquals(List.of(20, 40), reloaded.getArgument("rangeIntList"));

        Action tableAction = ModuleConfigEndToEndSupport.newAction(actionBank, "dev_test_table_args");
        ModuleConfigDialog tableDialog =
                ModuleConfigEndToEndSupport.openEditableDialog(actionBank, tableAction);
        Map<String, Object> caps = new LinkedHashMap<>();
        caps.put("hp", 150);
        caps.put("damage", 90);
        tableDialog.argumentEditor("caps").setValue(caps);
        tableDialog.confirmEdits();

        assertEquals(150, ((Map<?, ?>) tableAction.getArgument("caps")).get("hp"));
        ActionConfig tablePreset = ActionConfig.fromAction(tableAction);
        Action reloadedTable = tablePreset.toAction(actionBank, new WarningCollector(null));
        assertEquals(150, ((Map<?, ?>) reloadedTable.getArgument("caps")).get("hp"));
    }

    @Test
    void yamlPresetLoadEditThroughDialogAndSave() {
        ActionBank actionBank = environment.actionBank();
        Map<String, Object> yamlNode = new LinkedHashMap<>();
        yamlNode.put("module", "dev_test_int_args");
        yamlNode.put("version", "0.1");
        yamlNode.put("seedOffset", 3);
        yamlNode.put("arguments", Map.of(
                "anyInt", 1,
                "rangeInt", 7,
                "discreteInt", 25,
                "enumInt", 3));

        WarningCollector loadWarnings = new WarningCollector(null);
        ActionConfig preset =
                ActionConfig.readFromLoadedYamlMap(yamlNode, loadWarnings, "test entry");
        assertNotNull(preset);
        assertFalse(loadWarnings.hasWarnings());

        Action action = preset.toAction(actionBank, loadWarnings);
        assertEquals(3, action.getSeedOffset());
        assertEquals(7, action.getArgument("rangeInt"));

        ModuleConfigDialog dialog = ModuleConfigEndToEndSupport.openEditableDialog(actionBank, action);
        dialog.argumentEditor("rangeInt").setValue(9);
        dialog.confirmEdits();

        ActionArgumentsConfig saved = ActionArgumentsConfig.fromAction(action);
        assertEquals(3, saved.getSeedOffset());
        assertEquals(9, saved.getArguments().get("rangeInt"));

        ActionConfig roundTripped = new ActionConfig(preset.getModule(), preset.getVersion(), saved);
        Action reloaded = roundTripped.toAction(actionBank, new WarningCollector(null));
        assertEquals(9, reloaded.getArgument("rangeInt"));
        assertEquals(3, reloaded.getSeedOffset());
    }

    @Test
    void invalidPresetValueOnRealModuleFallsBackWithWarning() {
        ActionBank actionBank = environment.actionBank();
        Map<String, Object> yamlNode = Map.of(
                "module", "dev_test_int_args",
                "version", "0.1",
                "arguments", Map.of("rangeInt", 99));

        WarningCollector warnings = new WarningCollector(null);
        ActionConfig preset =
                ActionConfig.readFromLoadedYamlMap(yamlNode, warnings, "test entry");
        Action action = preset.toAction(actionBank, warnings);

        assertTrue(warnings.hasWarnings());
        assertEquals(5, action.getArgument("rangeInt"));
    }

    @Test
    void enumArgumentsResolveThroughDialogAndExecution() {
        ActionBank actionBank = environment.actionBank();
        Action action = ModuleConfigEndToEndSupport.newAction(actionBank, "dev_test_enum_args");

        ModuleConfigDialog dialog = ModuleConfigEndToEndSupport.openEditableDialog(actionBank, action);
        dialog.argumentEditor("color").setValue("GREEN");
        dialog.argumentEditor("cardType").setValue("MONSTER_WATER");
        dialog.confirmEdits();

        assertEquals("GREEN", action.getArgument("color"));
        assertEquals("MONSTER_WATER", action.getArgument("cardType"));

        ExecutionRequest request = assertDoesNotThrow(action::toExecutionRequest);
        assertEquals("GREEN", request.getArguments().get("color"));
        assertEquals("MONSTER_WATER", request.getArguments().get("cardType"));
    }

    @Test
    void realModuleDefaultsProduceValidExecutionRequest() {
        ActionBank actionBank = environment.actionBank();

        Action listAction = ModuleConfigEndToEndSupport.newAction(actionBank, "dev_test_list_args");
        ExecutionRequest listRequest = assertDoesNotThrow(listAction::toExecutionRequest);
        assertTrue(listRequest.getArguments().containsKey("anyStringList"));
        assertTrue(listRequest.getArguments().get("anyStringList") instanceof List);

        Action tableAction = ModuleConfigEndToEndSupport.newAction(actionBank, "dev_test_table_args");
        ExecutionRequest tableRequest = assertDoesNotThrow(tableAction::toExecutionRequest);
        assertTrue(tableRequest.getArguments().containsKey("caps"));
        assertTrue(tableRequest.getArguments().get("caps") instanceof Map);
    }
}
