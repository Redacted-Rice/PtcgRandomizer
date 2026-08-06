package redactedrice.ptcgr.randomizer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaValue;

import redactedrice.ptcgr.resources.PtcgBundledResources;
import redactedrice.randomizer.lua.sandbox.LuaSandbox;

class ChangeDetectorSetupTest {
    private File workDir;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        workDir = new File("build/change-detector-setup-test/" + testInfo.getTestMethod().get().getName());
        workDir.mkdirs();
    }

    @Test
    void moveSummaryLayoutTestsPassInLua() throws IOException {
        PtcgBundledResources resources = new PtcgBundledResources(workDir);
        resources.installAll();

        File randomizerDir = resources.getRandomizerDir();
        File modulesDir = resources.getModulesDir();
        File luaTestDir = new File(workDir, "lua-tests");
        luaTestDir.mkdirs();

        File testScript = new File(luaTestDir, "changedetector_setup_test.lua");
        try (InputStream input =
                getClass().getResourceAsStream("/lua/changedetector_setup_test.lua")) {
            if (input == null) {
                throw new IOException("Missing classpath resource lua/changedetector_setup_test.lua");
            }
            Files.copy(input, testScript.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        List<String> allowedDirectories = List.of(randomizerDir.getAbsolutePath(),
                modulesDir.getAbsolutePath(), luaTestDir.getAbsolutePath());

        LuaSandbox sandbox = new LuaSandbox(allowedDirectories);
        sandbox.set("MODULES_DIR",
                LuaValue.valueOf(modulesDir.getAbsolutePath().replace('\\', '/')));

        assertDoesNotThrow(() -> {
            LuaValue result = sandbox.executeFile(testScript.getAbsolutePath());
            assertTrue(result instanceof LuaBoolean && result.toboolean(),
                    "expected lua test script to return true");
        });
    }
}
