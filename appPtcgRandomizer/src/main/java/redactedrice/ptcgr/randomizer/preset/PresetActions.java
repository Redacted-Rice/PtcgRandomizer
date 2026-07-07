package redactedrice.ptcgr.randomizer.preset;

import java.util.ArrayList;
import java.util.List;

import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.randomizer.lua.Module;

/**
 * Builds runtime objects from loaded preset data.
 */
public final class PresetActions {
    private PresetActions() {}

    public static List<Action> toActions(RandomizerPreset preset, ActionBank actionBank,
            List<String> warnings) {
        List<Action> actions = new ArrayList<>();
        for (ActionPreset actionPreset : preset.getActions()) {
            Action action = toAction(actionPreset, actionBank, warnings);
            if (action != null) {
                actions.add(action);
            }
        }
        return actions;
    }

    public static Action toAction(ActionPreset preset, ActionBank actionBank,
            List<String> warnings) {
        Module module = actionBank.getModule(preset.getModule());
        if (module == null) {
            warnings.add("Missing module \"" + preset.getModule() + "\"; it will be skipped.");
            return null;
        }

        Action action = new Action(module);
        ActionConfig config = preset.getConfig();
        if (config.getSeedOffset() != null) {
            if (module.isSeeded()) {
                action.setSeedOffset(config.getSeedOffset());
            } else {
                warnings.add("Module " + preset.getModule()
                        + " does not use seed offsets; ignoring seedOffset.");
            }
        }
        return action;
    }
}
