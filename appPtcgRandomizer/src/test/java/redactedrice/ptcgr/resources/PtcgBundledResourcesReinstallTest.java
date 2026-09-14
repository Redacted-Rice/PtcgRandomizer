package redactedrice.ptcgr.resources;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PtcgBundledResourcesReinstallTest {
    private static final String ORPHAN_MODULE = "modules/actions/orphan.lua";

    @BeforeEach
    void clearReinstallProperties() {
        System.clearProperty(PtcgBundledResources.REINSTALL_RESOURCES_SYSTEM_PROPERTY);
        System.clearProperty(PtcgBundledResources.CLEAN_REINSTALL_RESOURCES_SYSTEM_PROPERTY);
    }

    @Test
    void shouldReinstallWhenEitherPropertyIsSet() {
        assertFalse(PtcgBundledResources.shouldReinstallResources());

        System.setProperty(PtcgBundledResources.REINSTALL_RESOURCES_SYSTEM_PROPERTY, "true");
        assertTrue(PtcgBundledResources.shouldReinstallResources());
        assertFalse(PtcgBundledResources.isCleanReinstallResourcesEnabled());

        System.clearProperty(PtcgBundledResources.REINSTALL_RESOURCES_SYSTEM_PROPERTY);
        System.setProperty(PtcgBundledResources.CLEAN_REINSTALL_RESOURCES_SYSTEM_PROPERTY, "true");
        assertTrue(PtcgBundledResources.shouldReinstallResources());
        assertTrue(PtcgBundledResources.isCleanReinstallResourcesEnabled());
    }

    @Test
    void lightReinstallKeepsOrphanModules(@TempDir Path tempDir) throws Exception {
        PtcgBundledResources resources = new PtcgBundledResources(tempDir.toFile());
        resources.installAppResources();

        Path orphan = tempDir.resolve(ORPHAN_MODULE);
        Files.createDirectories(orphan.getParent());
        Files.writeString(orphan, "-- custom", StandardCharsets.UTF_8);

        System.setProperty(PtcgBundledResources.REINSTALL_RESOURCES_SYSTEM_PROPERTY, "true");
        resources.installAppResources();

        assertTrue(Files.exists(orphan));
    }

    @Test
    void cleanReinstallRemovesOrphanModules(@TempDir Path tempDir) throws Exception {
        PtcgBundledResources resources = new PtcgBundledResources(tempDir.toFile());
        resources.installAppResources();

        Path orphan = tempDir.resolve(ORPHAN_MODULE);
        Files.createDirectories(orphan.getParent());
        Files.writeString(orphan, "-- stale", StandardCharsets.UTF_8);

        System.setProperty(PtcgBundledResources.CLEAN_REINSTALL_RESOURCES_SYSTEM_PROPERTY, "true");
        resources.installAppResources();

        assertFalse(Files.exists(orphan));
        assertTrue(Files.exists(tempDir.resolve("backups/modules/modules.bck/actions/orphan.lua")));
    }

    @Test
    void matchingMarkerWithoutFlagsSkipsReinstall(@TempDir Path tempDir) throws Exception {
        PtcgBundledResources resources = new PtcgBundledResources(tempDir.toFile());
        resources.installAppResources();

        Path bundledModule = tempDir.resolve("modules/actions/hp_cards_together_and_stage.lua");
        Files.writeString(bundledModule, "-- edited", StandardCharsets.UTF_8);

        resources.installAppResources();

        assertTrue(Files.exists(bundledModule));
        assertTrue(Files.readString(bundledModule, StandardCharsets.UTF_8).contains("edited"));
    }
}
