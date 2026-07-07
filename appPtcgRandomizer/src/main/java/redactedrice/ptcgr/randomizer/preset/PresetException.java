package redactedrice.ptcgr.randomizer.preset;

/**
 * Thrown when a preset file cannot be parsed.
 */
public class PresetException extends Exception {
    private static final long serialVersionUID = 1L;

    public PresetException(String message) {
        super(message);
    }
}
