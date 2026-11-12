package redactedrice.ptcgr.regression;

/** Whether the regression harness passes --clean-reinstall-resources to child JVMs. */
public enum RegressionCleanReinstall {
    FIRST_CASE,
    EVERY_CASE,
    NEVER;

    public static RegressionCleanReinstall parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return FIRST_CASE;
        }
        return switch (raw.trim().toLowerCase()) {
            case "first", "first-case" -> FIRST_CASE;
            case "every", "all" -> EVERY_CASE;
            case "never", "none" -> NEVER;
            default -> throw new IllegalArgumentException(
                    "Unknown clean-reinstall mode: " + raw + " (use first, every, or never)");
        };
    }
}
