package redactedrice.ptcgr.rules;

public final class RulesLoadOptions {
    private final boolean loadExclusions;
    private final boolean loadAssignments;

    private RulesLoadOptions(boolean loadExclusions, boolean loadAssignments) {
        this.loadExclusions = loadExclusions;
        this.loadAssignments = loadAssignments;
    }

    public static RulesLoadOptions all() {
        return new RulesLoadOptions(true, true);
    }

    public static RulesLoadOptions exclusionsOnly() {
        return new RulesLoadOptions(true, false);
    }

    public static RulesLoadOptions assignmentsOnly() {
        return new RulesLoadOptions(false, true);
    }

    public static RulesLoadOptions of(boolean loadExclusions, boolean loadAssignments) {
        return new RulesLoadOptions(loadExclusions, loadAssignments);
    }

    public boolean loadExclusions() {
        return loadExclusions;
    }

    public boolean loadAssignments() {
        return loadAssignments;
    }
}
