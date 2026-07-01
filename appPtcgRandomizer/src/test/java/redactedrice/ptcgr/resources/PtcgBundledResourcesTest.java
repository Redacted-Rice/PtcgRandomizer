package redactedrice.ptcgr.resources;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PtcgBundledResourcesTest {

    @Test
    void installAllExtractsBundledResources(@TempDir Path tempDir) {
        PtcgBundledResources installer = new PtcgBundledResources(tempDir.toFile());
        installer.installAll();
        PtcgBundledResources.verifyInstalled(tempDir.toFile());
    }

    @Test
    void installAllUsesRuntimeClasspath(@TempDir Path tempDir) throws Exception {
        PtcgBundledResources.main(new String[] {tempDir.toAbsolutePath().toString()});
        assertTrue(Files.exists(tempDir.resolve("randomizer/init.lua")));
    }
}
