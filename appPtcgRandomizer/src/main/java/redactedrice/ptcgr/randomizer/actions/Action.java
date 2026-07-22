package redactedrice.ptcgr.randomizer.actions;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import redactedrice.randomizer.lua.ExecutionRequest;
import redactedrice.randomizer.lua.Module;
import redactedrice.randomizer.lua.arguments.ArgumentDefinition;

public class Action {
    private static int nextId = 0;

    private final int id;
    private final Module module;
    private final Map<String, Object> arguments;
    // Per instance seed offset that default to the module metadata offset
    private int seedOffset;

    public Action(Module module) {
        this.id = nextId++;
        this.module = module;
        this.arguments = defaultArgumentsFromModule(module);
        this.seedOffset = module.getSeedOffset();
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

    public String getModuleId() {
        return module.getId();
    }

    public String getDescription() {
        return module.getDescription();
    }

    public Module getModule() {
        return module;
    }

    public int numConfigs() {
        // Add an option for seeded modules
        int numConfigs = module.getArguments().size();
        if (module.isSeeded()) {
            numConfigs++;
        }
        return numConfigs;
    }

    public Map<String, Object> getArguments() {
        return Collections.unmodifiableMap(arguments);
    }

    public Object getArgument(String name) {
        return arguments.get(name);
    }

    public boolean hasArgument(String name) {
        return arguments.containsKey(name);
    }

    public void setArgument(String name, Object value) {
        if (!arguments.containsKey(name)) {
            throw new IllegalArgumentException(
                    "Unknown argument '" + name + "' for module '" + module.getId() + "'");
        }
        arguments.put(name, value);
    }

    public void setArguments(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            setArgument(entry.getKey(), entry.getValue());
        }
    }

    public int getSeedOffset() {
        return seedOffset;
    }

    public void setSeedOffset(int seedOffset) {
        this.seedOffset = seedOffset;
    }

    public ExecutionRequest toExecutionRequest() {
        if (!module.isSeeded()) {
            return ExecutionRequest.forUnseededModule(module, arguments);
        }
        return ExecutionRequest.forModuleWithSeedOffset(module, arguments, seedOffset);
    }

    public Action copy() {
        // Since modules are immutable metadata, we can return a new Action with the same module
        // but a new ID to allow multiple instances in the selected list
        Action copy = new Action(this.module);
        copy.arguments.putAll(this.arguments);
        copy.seedOffset = this.seedOffset;
        return copy;
    }

    private static Map<String, Object> defaultArgumentsFromModule(Module module) {
        Map<String, Object> defaults = new LinkedHashMap<>();
        for (ArgumentDefinition argDef : module.getArguments()) {
            defaults.put(argDef.getName(), argDef.getDefaultValue());
        }
        return defaults;
    }
}
