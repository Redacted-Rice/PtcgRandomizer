package redactedrice.ptcgr.randomizer.actions;

import redactedrice.randomizer.metadata.LuaModuleMetadata;

public class Action {
    private static int nextId = 0;

    private final int id;
    private final LuaModuleMetadata module;

    public Action(LuaModuleMetadata module) {
        this.id = nextId++;
        this.module = module;
    }

    public int getId() {
        return id;
    }

    public String getCategory() {
        return module.getGroup() != null && !module.getGroup().isEmpty() ? module.getGroup() : "utility";
    }

    public String getName() {
        return module.getName();
    }

    public String getDescription() {
        return module.getDescription();
    }

    public LuaModuleMetadata getModule() {
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
