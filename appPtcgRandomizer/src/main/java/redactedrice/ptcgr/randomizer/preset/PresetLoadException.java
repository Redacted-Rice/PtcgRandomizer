package redactedrice.ptcgr.randomizer.preset;

/**
 * Thrown when a preset file cannot be parsed.
 */
public class PresetLoadException extends Exception {
    private static final long serialVersionUID = 1L;

    public PresetLoadException(String message) {
        super(message);
    }
}
