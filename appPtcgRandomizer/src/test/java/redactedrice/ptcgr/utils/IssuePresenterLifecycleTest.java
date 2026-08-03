package redactedrice.ptcgr.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.randomizer.utils.IssueTracker;

class IssuePresenterLifecycleTest {
    @BeforeEach
    void setUp() {
        IssueTracker.clear();
    }

    @AfterEach
    void tearDown() {
        IssueTracker.clear();
    }

    @Test
    void finishPhaseDisplaysNothingWhenEmptyAndClears() {
        IssuePresenter.finishPhase(null, "randomize");
        assertFalse(IssueTracker.hasIssues());
    }

    @Test
    void finishPhaseClearsErrorsAndWarningsAfterPhase() {
        IssueTracker.addWarning("soft");
        IssueTracker.addError("hard");
        assertTrue(IssueTracker.hasErrors());
        assertTrue(IssueTracker.hasWarnings());

        // null parent skips dialog interaction in headless-friendly path via IOUtils;
        // still clears the store
        IssuePresenter.finishPhase(null, "randomize");

        assertFalse(IssueTracker.hasIssues());
        assertEquals(0, IssueTracker.getErrorCount());
        assertEquals(0, IssueTracker.getWarningCount());
    }

    @Test
    void displayWarningsClearsOnlyWarnings() {
        IssueTracker.addWarning("soft");
        IssueTracker.addError("hard");

        IssuePresenter.displayWarnings(null, "config load");

        assertFalse(IssueTracker.hasWarnings());
        assertTrue(IssueTracker.hasErrors());
        assertEquals(List.of("hard"), IssueTracker.getErrors());
    }
}
