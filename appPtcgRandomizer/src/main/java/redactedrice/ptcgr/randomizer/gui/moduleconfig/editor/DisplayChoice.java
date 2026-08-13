package redactedrice.ptcgr.randomizer.gui.moduleconfig.editor;

// dropdown item that shows a user friendly label but stores the underlying value
public final class DisplayChoice {
    private final Object value;
    private final String label;

    public DisplayChoice(Object value, String label) {
        this.value = value;
        this.label = label != null && !label.isBlank() ? label : String.valueOf(value);
    }

    public Object getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
