package redactedrice.ptcgr.randomizer.scripttests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ScriptTestCaseFilesTest {

    @Test
    void selectCasesRejectsPathLikeNames(@TempDir Path tempDir) throws Exception {
        Files.writeString(tempDir.resolve("test_one.lua"), "return { module = \"x\" }");

        assertThrows(IllegalArgumentException.class,
                () -> ScriptTestCaseFiles.selectCases(tempDir, "../test_one.lua"));
        assertThrows(IllegalArgumentException.class,
                () -> ScriptTestCaseFiles.selectCases(tempDir, "cards/test_one.lua"));
        assertThrows(IllegalArgumentException.class,
                () -> ScriptTestCaseFiles.selectCases(tempDir, "cards\\test_one.lua"));
    }

    @Test
    void selectCasesRejectsNonCaseFileName(@TempDir Path tempDir) throws Exception {
        Files.writeString(tempDir.resolve("test_one.lua"), "return { module = \"x\" }");
        Files.writeString(tempDir.resolve("helper.lua"), "return {}");

        assertThrows(IllegalArgumentException.class,
                () -> ScriptTestCaseFiles.selectCases(tempDir, "helper"));
    }

    @Test
    void selectCasesListsAllWhenRequestedNameMissing(@TempDir Path tempDir) throws Exception {
        Files.writeString(tempDir.resolve("test_one.lua"), "return { module = \"x\" }");
        Files.writeString(tempDir.resolve("helper.lua"), "return {}");

        List<Path> expected = List.of(tempDir.resolve("test_one.lua"));
        assertEquals(expected, ScriptTestCaseFiles.selectCases(tempDir, null));
        assertEquals(expected, ScriptTestCaseFiles.selectCases(tempDir, ""));
        assertEquals(expected, ScriptTestCaseFiles.selectCases(tempDir, "   "));
    }

    @Test
    void selectCasesRejectsMissingOrNonDirectoryTestsDir(@TempDir Path tempDir) throws Exception {
        Path missing = tempDir.resolve("missing");
        Path notADir = tempDir.resolve("file.txt");
        Files.writeString(notADir, "x");

        assertThrows(IllegalArgumentException.class,
                () -> ScriptTestCaseFiles.selectCases(null, "test_one"));
        assertThrows(IllegalArgumentException.class,
                () -> ScriptTestCaseFiles.selectCases(missing, null));
        assertThrows(IllegalArgumentException.class,
                () -> ScriptTestCaseFiles.selectCases(notADir, null));
    }

    @Test
    void selectCasesResolvesSingleCaseByName(@TempDir Path tempDir) throws Exception {
        Files.writeString(tempDir.resolve("test_one.lua"), "return { module = \"x\" }");

        assertEquals(List.of(tempDir.resolve("test_one.lua")),
                ScriptTestCaseFiles.selectCases(tempDir, "test_one"));
        assertEquals(List.of(tempDir.resolve("test_one.lua")),
                ScriptTestCaseFiles.selectCases(tempDir, "test_one.lua"));
        assertEquals(List.of(tempDir.resolve("test_one.lua")),
                ScriptTestCaseFiles.selectCases(tempDir, "TEST_ONE"));
        assertEquals(List.of(tempDir.resolve("test_one.lua")),
                ScriptTestCaseFiles.selectCases(tempDir, "TEST_ONE.LUA"));
    }

    @Test
    void selectCasesRejectsDuplicateBasenamesInSubfolders(@TempDir Path tempDir) throws Exception {
        Path first = tempDir.resolve("a");
        Path second = tempDir.resolve("b");
        Files.createDirectories(first);
        Files.createDirectories(second);
        Files.writeString(first.resolve("test_same.lua"), "return { module = \"x\" }");
        Files.writeString(second.resolve("test_same.lua"), "return { module = \"x\" }");

        assertThrows(IllegalArgumentException.class,
                () -> ScriptTestCaseFiles.selectCases(tempDir, "test_same"));
    }

    @Test
    void isLuaCaseRequiresTestPrefix(@TempDir Path tempDir) throws Exception {
        Files.writeString(tempDir.resolve("test_case.lua"), "return { module = \"x\" }");
        Files.writeString(tempDir.resolve("helper.lua"), "return {}");
        Files.writeString(tempDir.resolve("01_case.lua"), "return { module = \"x\" }");

        assertTrue(ScriptTestCaseFiles.isLuaCase(tempDir.resolve("test_case.lua")));
        assertFalse(ScriptTestCaseFiles.isLuaCase(tempDir.resolve("helper.lua")));
        assertFalse(ScriptTestCaseFiles.isLuaCase(tempDir.resolve("01_case.lua")));
    }

    @Test
    void selectCasesFindsTestsInSubfolders(@TempDir Path tempDir) throws Exception {
        Path nested = tempDir.resolve("cards");
        Files.createDirectories(nested);
        Files.writeString(tempDir.resolve("test_root.lua"), "return { module = \"x\" }");
        Files.writeString(nested.resolve("test_nested.lua"), "return { module = \"x\" }");
        Files.writeString(nested.resolve("helper.lua"), "return {}");

        List<Path> cases = ScriptTestCaseFiles.selectCases(tempDir, null);
        assertEquals(2, cases.size());
        assertTrue(cases.contains(tempDir.resolve("test_root.lua")));
        assertTrue(cases.contains(nested.resolve("test_nested.lua")));
        assertEquals(List.of(nested.resolve("test_nested.lua")),
                ScriptTestCaseFiles.selectCases(tempDir, "test_nested"));
    }
}
