package redactedrice.ptcgr.randomizer.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import redactedrice.ptcgr.constants.PtcgRandomizerVersion;
import redactedrice.ptcgr.constants.romenums.CardType;
import redactedrice.ptcgr.resources.PtcgBundledResources;
import redactedrice.randomizer.LuaRandomizerWrapper;
import redactedrice.randomizer.context.EnumDefinition;
import redactedrice.randomizer.lua.Module;
import redactedrice.randomizer.lua.arguments.ArgumentDefinition;
import redactedrice.randomizer.lua.requirements.CoreRequirements;
import redactedrice.randomizer.utils.IssueTracker;
import redactedrice.randomizer.utils.RandomizerBundledResources;

class ActionBankTest {
    private File workDir;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        workDir = new File("build/action-bank-test/" + testInfo.getTestMethod().get().getName());
        workDir.mkdirs();
    }

    @Test
    void getEnumValuesResolvesJavaDefinedCardTypeFromSharedContext() {
        System.setProperty("ptcgr.devModules", "true");
        try {
            PtcgBundledResources.main(new String[] {workDir.getAbsolutePath()});

            File modulesDir = new File(workDir, PtcgBundledResources.MODULES_DIR_NAME);
            File randomizerDir = RandomizerBundledResources.getInstalledDir(workDir);
            List<String> allowedDirectories = new ArrayList<>();
            allowedDirectories.add(randomizerDir.getAbsolutePath());
            allowedDirectories.add(modulesDir.getAbsolutePath());
            List<String> searchPaths = List.of(modulesDir.getAbsolutePath());

            CoreRequirements requirements = new CoreRequirements();
            requirements.addCoreRequirement(PtcgRandomizerVersion.PLATFORM_KEY,
                    PtcgRandomizerVersion.VERSION, true);

            LuaRandomizerWrapper wrapper = new LuaRandomizerWrapper(allowedDirectories, searchPaths,
                    null, null, requirements);

            // Mirrors RandomizerCore.setupLuaRandomizer(): built-in Java enums are registered on
            // the shared context before modules are loaded, the same way RandomizerCore does it,
            // so they're resolvable by name for the config UI's ENUM argument dropdowns.
            wrapper.getSharedContext().registerEnum(CardType.class);

            IssueTracker.clear();
            wrapper.loadModules();
            assertFalse(IssueTracker.hasErrors(),
                    () -> "Module requirement validation failed: " + IssueTracker.getErrors());

            ActionBank actionBank = new ActionBank(wrapper);

            Module module = wrapper.getModule("dev_test_enum_args");
            assertNotNull(module);

            ArgumentDefinition cardTypeArg = module.getArguments().stream()
                    .filter(arg -> "cardType".equals(arg.getName())).findFirst().orElse(null);
            assertNotNull(cardTypeArg, "Expected a 'cardType' argument on dev_test_enum_args");
            assertEquals("CardType", cardTypeArg.getTypeDefinition().getEnumName());

            List<String> enumValues = actionBank.getEnumValues("CardType");
            assertNotNull(enumValues);
            assertTrue(enumValues.contains("MONSTER_FIRE"));

            EnumDefinition enumDefinition = wrapper.getEnumDefinition("CardType");
            assertNotNull(enumDefinition, "CardType should be registered on the shared context");

            Object converted = cardTypeArg.convertAndValidate("MONSTER_FIRE",
                    wrapper.getSharedContext().getEnumRegistry());
            assertEquals("MONSTER_FIRE", converted);
        } finally {
            System.clearProperty("ptcgr.devModules");
        }
    }

    @Test
    void getSortsActionsAlphabeticallyFromBundledModules() {
        ActionBank actionBank = bundledActionBank(workDir);

        List<String> names = actionBank.get("All").stream().map(Action::getName).toList();
        assertFalse(names.isEmpty());
        assertEquals(names.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList(), names);
    }

    @Test
    void getFiltersByGroupAndModifiedFieldFromBundledModules() {
        ActionBank actionBank = bundledActionBank(workDir);

        List<String> hpModules =
                actionBank.get("All", "hp").stream().map(Action::getModuleId).toList();
        assertTrue(hpModules.contains("shuffle_hp"));
        assertTrue(hpModules.contains("hp_by_stage_from_rom"));
        assertFalse(hpModules.contains("randomize_moves"));

        List<String> moveModules =
                actionBank.get("moves", "moves").stream().map(Action::getModuleId).toList();
        assertTrue(moveModules.contains("randomize_moves"));
        assertFalse(moveModules.contains("shuffle_hp"));
    }

    @Test
    void getModifiesWithAllIncludesSortedFieldsFromBundledModules() {
        ActionBank actionBank = bundledActionBank(workDir);

        List<String> modifies = actionBank.getModifiesWithAll();
        assertEquals("All", modifies.get(0));
        assertTrue(modifies.contains("hp"));
        assertTrue(modifies.contains("moves"));
        assertEquals(modifies.subList(1, modifies.size()),
                modifies.subList(1, modifies.size()).stream().sorted().toList());
    }

    private static ActionBank bundledActionBank(File workDir) {
        workDir.mkdirs();
        PtcgBundledResources.main(new String[] {workDir.getAbsolutePath()});

        File modulesDir = new File(workDir, PtcgBundledResources.MODULES_DIR_NAME);
        File randomizerDir = RandomizerBundledResources.getInstalledDir(workDir);
        List<String> allowedDirectories = new ArrayList<>();
        allowedDirectories.add(randomizerDir.getAbsolutePath());
        allowedDirectories.add(modulesDir.getAbsolutePath());
        List<String> searchPaths = List.of(modulesDir.getAbsolutePath());

        CoreRequirements requirements = new CoreRequirements();
        requirements.addCoreRequirement(PtcgRandomizerVersion.PLATFORM_KEY,
                PtcgRandomizerVersion.VERSION, true);

        LuaRandomizerWrapper wrapper = new LuaRandomizerWrapper(allowedDirectories, searchPaths,
                null, null, requirements);
        IssueTracker.clear();
        wrapper.loadModules();
        assertFalse(IssueTracker.hasErrors(),
                () -> "Module requirement validation failed: " + IssueTracker.getErrors());
        return new ActionBank(wrapper);
    }
}
