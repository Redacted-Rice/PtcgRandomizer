package redactedrice.ptcgr.randomizer.actions;

import redactedrice.randomizer.lua.Module;

public class Action {
    private static int nextId = 0;

    private final int id;
    private final Module module;

    public Action(Module module) {
        this.id = nextId++;
        this.module = module;
    }

    public int getId() {
        return id;
    }

    public String getCategory() {
        // TODO: Refactor to support multiple groups. For now just take the first
        if (module.getGroups() != null && !module.getGroups().isEmpty()) {
            return module.getGroups().iterator().next();
        }
        // Shouldn't be empty but just in case
        return "utility";
    }

    public String getName() {
        return module.getName();
    }

    public String getDescription() {
        return module.getDescription();
    }

    public Module getModule() {
        return module;
    }

    public int numConfigs() {
        return module.getArguments().size();
    }

    public Action copy() {
        // Since modules are immutable metadata, we can return a new Action with the same module
        // but a new ID to allow multiple instances in the selected list
        return new Action(this.module);
    }
}
