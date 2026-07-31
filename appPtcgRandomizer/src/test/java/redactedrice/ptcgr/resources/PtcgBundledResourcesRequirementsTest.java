package redactedrice.ptcgr.resources;

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
import redactedrice.randomizer.LuaRandomizerWrapper;
import redactedrice.randomizer.lua.requirements.CoreRequirements;
import redactedrice.randomizer.utils.ErrorTracker;

class PtcgBundledResourcesRequirementsTest {
    private File workDir;

    // Each test gets its own subdirectory (named after the test) rather than sharing one:
    // Lua module loading can leave file handles open on Windows, so reusing/deleting a
    // shared directory across tests in the same JVM run is flaky. These directories live
    // under build so they're cleaned up by clean task rather than JUnit's @TempDir,
    // which fails to delete files that are still held open.
    @BeforeEach
    void setUp(TestInfo testInfo) {
        workDir = new File(
                "build/module-requirements-test/" + testInfo.getTestMethod().get().getName());
        workDir.mkdirs();
    }

    @Test
    void bundledModulesPassRequirementValidation() {
        PtcgBundledResources resources = new PtcgBundledResources(workDir);
        resources.installAll();

        File randomizerDir = resources.getRandomizerDir();
        File modulesDir = resources.getModulesDir();

        List<String> allowedDirectories = new ArrayList<>();
        allowedDirectories.add(randomizerDir.getAbsolutePath());
        allowedDirectories.add(modulesDir.getAbsolutePath());

        List<String> searchPaths = List.of(modulesDir.getAbsolutePath());

        CoreRequirements requirements = new CoreRequirements();
        requirements.addCoreRequirement(PtcgRandomizerVersion.PLATFORM_KEY,
                PtcgRandomizerVersion.VERSION, true);

        LuaRandomizerWrapper wrapper =
                new LuaRandomizerWrapper(allowedDirectories, searchPaths, null, null, requirements);

        ErrorTracker.clearErrors();
        int loaded = wrapper.loadModules();

        assertTrue(loaded > 0, "Expected bundled modules to load");
        assertFalse(ErrorTracker.hasErrors(),
                () -> "Module requirement validation failed: " + ErrorTracker.getErrors());
    }

    @Test
    void devModulesAreNotInstalledByDefault() {
        PtcgBundledResources resources = new PtcgBundledResources(workDir);
        resources.installAll();

        assertFalse(new File(resources.getModulesDir(), "actions/dev_test_int_args.lua").isFile());
        assertFalse(
                new File(resources.getModulesDir(), "actions/dev_test_double_args.lua").isFile());
        assertFalse(
                new File(resources.getModulesDir(), "actions/dev_test_string_args.lua").isFile());
        assertFalse(new File(resources.getModulesDir(), "actions/dev_test_bool_args.lua").isFile());
        assertFalse(new File(resources.getModulesDir(), "actions/dev_test_enum_args.lua").isFile());
        assertFalse(new File(resources.getModulesDir(), "actions/dev_test_list_args.lua").isFile());
        assertFalse(new File(resources.getModulesDir(), "actions/dev_test_table_args.lua").isFile());
    }

    @Test
    void devModulesInstallAlongsideRegularModulesWhenEnabled() {
        System.setProperty("ptcgr.devModules", "true");
        try {
            PtcgBundledResources resources = new PtcgBundledResources(workDir);
            resources.installAll();

            File modulesDir = resources.getModulesDir();
            assertTrue(new File(modulesDir, "actions/dev_test_int_args.lua").isFile());
            assertTrue(new File(modulesDir, "actions/dev_test_double_args.lua").isFile());
            assertTrue(new File(modulesDir, "actions/dev_test_string_args.lua").isFile());
            assertTrue(new File(modulesDir, "actions/dev_test_bool_args.lua").isFile());
            assertTrue(new File(modulesDir, "actions/dev_test_enum_args.lua").isFile());
            assertTrue(new File(modulesDir, "actions/dev_test_list_args.lua").isFile());
            assertTrue(new File(modulesDir, "actions/dev_test_table_args.lua").isFile());
            // Regular modules should still be there too, unaffected by the dev merge
            assertTrue(new File(modulesDir, "actions/randomize_hp.lua").isFile());

            List<String> allowedDirectories = new ArrayList<>();
            allowedDirectories.add(resources.getRandomizerDir().getAbsolutePath());
            allowedDirectories.add(modulesDir.getAbsolutePath());
            List<String> searchPaths = List.of(modulesDir.getAbsolutePath());

            CoreRequirements requirements = new CoreRequirements();
            requirements.addCoreRequirement(PtcgRandomizerVersion.PLATFORM_KEY,
                    PtcgRandomizerVersion.VERSION, true);

            LuaRandomizerWrapper wrapper = new LuaRandomizerWrapper(allowedDirectories, searchPaths,
                    null, null, requirements);

            ErrorTracker.clearErrors();
            int loaded = wrapper.loadModules();

            assertTrue(loaded > 0, "Expected bundled and dev modules to load");
            assertFalse(ErrorTracker.hasErrors(),
                    () -> "Module requirement validation failed: " + ErrorTracker.getErrors());
            assertNotNull(wrapper.getModule("dev_test_int_args"));
            assertNotNull(wrapper.getModule("dev_test_double_args"));
            assertNotNull(wrapper.getModule("dev_test_string_args"));
            assertNotNull(wrapper.getModule("dev_test_bool_args"));
            assertNotNull(wrapper.getModule("dev_test_enum_args"));
            assertNotNull(wrapper.getModule("dev_test_list_args"));
            assertNotNull(wrapper.getModule("dev_test_table_args"));
        } finally {
            System.clearProperty("ptcgr.devModules");
        }
    }
}
