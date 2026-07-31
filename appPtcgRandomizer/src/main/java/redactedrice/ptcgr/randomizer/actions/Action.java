package redactedrice.ptcgr.randomizer.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import redactedrice.randomizer.context.EnumRegistry;
import redactedrice.randomizer.lua.ExecutionRequest;
import redactedrice.randomizer.lua.Module;
import redactedrice.randomizer.lua.arguments.ArgumentDefinition;
import redactedrice.randomizer.lua.arguments.ArgumentType;
import redactedrice.randomizer.lua.arguments.TypeDefinition;

public class Action {
    private static int nextId = 0;

    private final int id;
    private final Module module;
    private final EnumRegistry enumRegistry;
    private final Map<String, Object> arguments;
    // Per instance seed offset that default to the module metadata offset
    private int seedOffset;

    public Action(Module module) {
        this(module, new EnumRegistry());
    }

    public Action(Module module, EnumRegistry enumRegistry) {
        this.id = nextId++;
        this.module = module;
        this.enumRegistry = enumRegistry != null ? enumRegistry : new EnumRegistry();
        this.arguments = defaultArgumentsFromModule(module, this.enumRegistry);
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
        ArgumentDefinition argDef = findArgumentDefinition(name);
        if (argDef == null) {
            throw new IllegalArgumentException(
                    "Unknown argument '" + name + "' for module '" + module.getId() + "'");
        }

        Object normalized = normalizeUnsetScalarValue(value, argDef.getTypeDefinition());
        Object stored;
        if (normalized == null) {
            stored = emptyValueForType(argDef.getTypeDefinition());
        } else {
            stored = copyArgumentValue(
                    argDef.convertAndValidate(normalized, enumRegistry),
                    argDef.getTypeDefinition());
        }
        arguments.put(name, stored);
    }

    public int getSeedOffset() {
        return seedOffset;
    }

    public void setSeedOffset(int seedOffset) {
        this.seedOffset = seedOffset;
    }

    public ExecutionRequest toExecutionRequest() {
        Map<String, Object> executionArguments = argumentsForExecution();
        if (!module.isSeeded()) {
            return ExecutionRequest.forUnseededModule(module, executionArguments);
        }
        return ExecutionRequest.forModuleWithSeedOffset(module, executionArguments, seedOffset);
    }

    public Action copy() {
        // Since modules are immutable metadata, we can return a new Action with the same module
        // but a new ID to allow multiple instances in the selected list
        Action copy = new Action(this.module, this.enumRegistry);
        for (ArgumentDefinition argDef : module.getArguments()) {
            String name = argDef.getName();
            copy.arguments.put(name,
                    copyArgumentValue(arguments.get(name), argDef.getTypeDefinition()));
        }
        copy.seedOffset = this.seedOffset;
        return copy;
    }

    private static Map<String, Object> defaultArgumentsFromModule(Module module,
            EnumRegistry enumRegistry) {
        Map<String, Object> defaults = new LinkedHashMap<>();
        for (ArgumentDefinition argDef : module.getArguments()) {
            defaults.put(argDef.getName(), defaultValueFor(argDef, enumRegistry));
        }
        return defaults;
    }

    // Modules may omit defaults in Lua and Map.copyOf rejects null values so collection
    // arguments use empty containers and unset scalars are omitted at execution time.
    private static Object defaultValueFor(ArgumentDefinition argDef, EnumRegistry enumRegistry) {
        Object defaultValue = argDef.getDefaultValue();
        if (defaultValue == null) {
            return emptyValueForType(argDef.getTypeDefinition());
        }
        return copyArgumentValue(
                argDef.convertAndValidate(defaultValue, enumRegistry),
                argDef.getTypeDefinition());
    }

    private static Object copyArgumentValue(Object value, TypeDefinition typeDef) {
        if (value == null) {
            return null;
        }
        if (typeDef.isList()) {
            TypeDefinition elementType = typeDef.getElementType();
            List<Object> copy = new ArrayList<>();
            for (Object element : (List<?>) value) {
                copy.add(copyArgumentValue(element, elementType));
            }
            return copy;
        }
        if (typeDef.isTable()) {
            TypeDefinition valueType = typeDef.getValueType();
            Map<Object, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                copy.put(entry.getKey(), copyArgumentValue(entry.getValue(), valueType));
            }
            return copy;
        }
        return value;
    }

    private ArgumentDefinition findArgumentDefinition(String name) {
        for (ArgumentDefinition argDef : module.getArguments()) {
            if (argDef.getName().equals(name)) {
                return argDef;
            }
        }
        return null;
    }

    private static Object emptyValueForType(TypeDefinition typeDef) {
        if (typeDef.isTable()) {
            return new LinkedHashMap<>();
        }
        if (typeDef.isList()) {
            return new ArrayList<>();
        }
        return null;
    }

    // Blank scalar strings from the config UI (and YAML) mean "unset", same as null, so optional
    // arguments keep being omitted at execution time rather than sent as "" (truthy in Lua).
    private static Object normalizeUnsetScalarValue(Object value, TypeDefinition typeDef) {
        if (value instanceof String str && str.isBlank() && isScalarString(typeDef)) {
            return null;
        }
        return value;
    }

    private static boolean isScalarString(TypeDefinition typeDef) {
        return !typeDef.isList() && !typeDef.isTable() && !typeDef.isEnum()
                && typeDef.getBaseType() == ArgumentType.STRING;
    }

    private Map<String, Object> argumentsForExecution() {
        Map<String, Object> executionArguments = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : arguments.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                executionArguments.put(entry.getKey(), value);
            }
        }
        return executionArguments;
    }
}
