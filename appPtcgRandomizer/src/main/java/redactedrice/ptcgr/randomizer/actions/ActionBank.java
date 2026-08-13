package redactedrice.ptcgr.randomizer.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.EnumValuesProvider;
import redactedrice.randomizer.LuaRandomizerWrapper;
import redactedrice.randomizer.context.EnumDefinition;
import redactedrice.randomizer.context.EnumRegistry;
import redactedrice.randomizer.lua.Module;
import redactedrice.randomizer.lua.ModuleRegistry;

public class ActionBank implements EnumValuesProvider {
    private static final String FILTER_ALL = "All";

    private HashMap<Integer, Action> allActions;
    private HashMap<String, HashMap<Integer, Action>> actionsByGroup;
    private Map<String, String> groupDisplayLabels;
    private LuaRandomizerWrapper luaRandomizer;

    public ActionBank(LuaRandomizerWrapper luaRandomizer) {
        this.luaRandomizer = luaRandomizer;
        this.allActions = new HashMap<>();
        this.actionsByGroup = new HashMap<>();
        this.groupDisplayLabels = new LinkedHashMap<>();
        loadModules();
    }

    private void loadModules() {
        if (luaRandomizer == null) {
            return;
        }
        List<Module> modules = luaRandomizer.getAvailableModules();
        for (Module module : modules) {
            Action action = new Action(module, getEnumRegistry());
            allActions.put(action.getId(), action);

            for (String group : action.getGroups()) {
                String key = filterKey(group);
                actionsByGroup.computeIfAbsent(key, unused -> new HashMap<>())
                        .put(action.getId(), action);
                groupDisplayLabels.putIfAbsent(key, group);
            }
        }
    }

    public Action get(int id) {
        return allActions.get(id);
    }

    public Module getModule(String moduleId) {
        if (luaRandomizer == null) {
            return null;
        }
        return luaRandomizer.getModule(moduleId);
    }

    // Resolves the values for an enum registered by a module's onLoad (e.g. via
    // context.registerEnum). Used to populate ENUM base type argument dropdowns in the config UI.
    @Override
    public List<String> getEnumValues(String enumName) {
        if (luaRandomizer == null || enumName == null || enumName.isBlank()) {
            return null;
        }
        EnumDefinition enumDefinition = luaRandomizer.getEnumDefinition(enumName);
        return enumDefinition != null ? enumDefinition.getValues() : null;
    }

    @Override
    public String getEnumValueDisplayName(String enumName, String canonicalValue) {
        if (luaRandomizer == null || enumName == null || enumName.isBlank()
                || canonicalValue == null) {
            return canonicalValue;
        }
        EnumDefinition enumDefinition = luaRandomizer.getEnumDefinition(enumName);
        return enumDefinition != null ? enumDefinition.getValueDisplayName(canonicalValue)
                : canonicalValue;
    }

    public EnumRegistry getEnumRegistry() {
        if (luaRandomizer == null) {
            return new EnumRegistry();
        }
        return luaRandomizer.getSharedContext().getEnumRegistry();
    }

    public Module getScript(String scriptId) {
        if (luaRandomizer == null) {
            return null;
        }
        return luaRandomizer.getScript(scriptId);
    }

    public List<Module> getPreScripts() {
        return getAllScripts(ModuleRegistry.SCRIPT_TIMING_PRE);
    }

    public List<Module> getPostScripts() {
        return getAllScripts(ModuleRegistry.SCRIPT_TIMING_POST);
    }

    private List<Module> getAllScripts(String timing) {
        if (luaRandomizer == null) {
            return List.of();
        }
        return luaRandomizer.getModuleRegistry().getAllScripts(timing);
    }

    public Collection<Action> get() {
        return get(null);
    }

    public Collection<Action> get(String group) {
        Collection<Action> actions;
        if (group == null || FILTER_ALL.equals(group)) {
            actions = allActions.values();
        } else {
            HashMap<Integer, Action> found = actionsByGroup.get(filterKey(group));
            actions = found != null ? found.values() : Collections.emptyList();
        }
        return sortByName(actions);
    }

    public List<String> getCategoriesWithAll() {
        List<String> categories = new ArrayList<>(groupDisplayLabels.values());
        categories.sort(String.CASE_INSENSITIVE_ORDER);
        categories.add(0, FILTER_ALL);
        return categories;
    }

    private static String filterKey(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static List<Action> sortByName(Collection<Action> actions) {
        return actions.stream()
                .sorted(Comparator.comparing(Action::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }
}
