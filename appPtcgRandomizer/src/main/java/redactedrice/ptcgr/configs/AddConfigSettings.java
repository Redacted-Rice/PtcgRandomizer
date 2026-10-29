package redactedrice.ptcgr.configs;

/** Which yaml sections to import or reset in the GUI. */
public final class AddConfigSettings {
    public boolean rules;
    public boolean actions;
    public boolean generalSettings;

    public AddConfigSettings(boolean rules, boolean actions, boolean generalSettings) {
        this.rules = rules;
        this.actions = actions;
        this.generalSettings = generalSettings;
    }

    public boolean isEmpty() {
        return !rules && !actions && !generalSettings;
    }
}
