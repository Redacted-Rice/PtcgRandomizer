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
import redactedrice.ptcgr.constants.CardDataSource;
import redactedrice.ptcgr.constants.DuplicateHandling;
import redactedrice.ptcgr.constants.StageGrouping;
import redactedrice.ptcgr.constants.RandomizationApproach;
import redactedrice.ptcgr.constants.romenums.CardType;
import redactedrice.ptcgr.constants.romenums.EnergyType;
import redactedrice.ptcgr.constants.romenums.EvolutionStage;
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
                    null, requirements);

            // Mirrors RandomizerCore.setupLuaRandomizer(): built-in Java enums are registered on
            // the shared context before modules are loaded, the same way RandomizerCore does it,
            // so they're resolvable by name for the config UI's ENUM argument dropdowns.
            registerPtcgEnums(wrapper);

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
    void getFiltersByGroupFromBundledModules() {
        ActionBank actionBank = bundledActionBank(workDir);

        List<String> hpModules = actionBank.get("HP").stream().map(Action::getModuleId).toList();
        assertTrue(hpModules.contains("shuffle_hp"));
        assertFalse(hpModules.contains("hp_by_stage_from_rom"));
        assertFalse(hpModules.contains("randomize_moves"));

        List<String> attackModules =
                actionBank.get("Attacks").stream().map(Action::getModuleId).toList();
        assertTrue(attackModules.contains("randomize_moves"));
        assertFalse(attackModules.contains("shuffle_hp"));

        List<String> supportModules =
                actionBank.get("Support").stream().map(Action::getModuleId).toList();
        assertTrue(supportModules.contains("set_num_moves"));
    }

    @Test
    void getMatchesGroupsCaseInsensitively() {
        ActionBank actionBank = bundledActionBank(workDir);

        List<String> lowerGroup =
                actionBank.get("monsters").stream().map(Action::getModuleId).toList();
        List<String> upperGroup = actionBank.get("HP").stream().map(Action::getModuleId).toList();
        assertTrue(lowerGroup.contains("shuffle_hp"));
        assertTrue(upperGroup.contains("shuffle_hp"));
    }

    @Test
    void getCategoriesWithAllIncludesSortedGroupsFromBundledModules() {
        ActionBank actionBank = bundledActionBank(workDir);

        List<String> groups = actionBank.getCategoriesWithAll();
        assertEquals("All", groups.get(0));
        assertTrue(groups.contains("HP"));
        assertTrue(groups.contains("Attacks"));
        assertEquals(groups.subList(1, groups.size()),
                groups.subList(1, groups.size()).stream().sorted(String.CASE_INSENSITIVE_ORDER)
                        .toList());
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
                null, requirements);
        registerPtcgEnums(wrapper);
        IssueTracker.clear();
        wrapper.loadModules();
        assertFalse(IssueTracker.hasErrors(),
                () -> "Module requirement validation failed: " + IssueTracker.getErrors());
        return new ActionBank(wrapper);
    }

    private static void registerPtcgEnums(LuaRandomizerWrapper wrapper) {
        wrapper.getSharedContext().registerEnum(CardType.class);
        wrapper.getSharedContext().registerEnum(EnergyType.class);
        wrapper.getSharedContext().registerEnum(EvolutionStage.class);
        wrapper.getSharedContext().registerEnum(RandomizationApproach.class);
        wrapper.getSharedContext().registerEnum(CardDataSource.class);
        wrapper.getSharedContext().registerEnum(DuplicateHandling.class);
        wrapper.getSharedContext().registerEnum(StageGrouping.class);
    }
}
