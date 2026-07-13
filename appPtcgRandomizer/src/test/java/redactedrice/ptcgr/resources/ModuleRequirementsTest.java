package redactedrice.ptcgr.resources;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.ptcgr.constants.PtcgRandomizerVersion;
import redactedrice.randomizer.LuaRandomizerWrapper;
import redactedrice.randomizer.lua.requirements.CoreRequirements;
import redactedrice.randomizer.utils.ErrorTracker;

class ModuleRequirementsTest {
    private File workDir;

    @BeforeEach
    void setUp() {
        workDir = new File("build/module-requirements-test");
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

        LuaRandomizerWrapper wrapper = new LuaRandomizerWrapper(allowedDirectories, searchPaths,
                null, null, requirements);

        ErrorTracker.clearErrors();
        int loaded = wrapper.loadModules();

        assertTrue(loaded > 0, "Expected bundled modules to load");
        assertFalse(ErrorTracker.hasErrors(),
                () -> "Module requirement validation failed: " + ErrorTracker.getErrors());
    }
}
