package redactedrice.ptcgr.randomizer.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import redactedrice.randomizer.LuaRandomizerWrapper;
import redactedrice.randomizer.context.EnumDefinition;
import redactedrice.randomizer.context.EnumRegistry;
import redactedrice.randomizer.lua.Module;
import redactedrice.randomizer.lua.ModuleRegistry;

public class ActionBank {
    private static final String FILTER_ALL = "All";

    private HashMap<Integer, Action> allActions;
    private HashMap<String, HashMap<Integer, Action>> actionsByCategory;
    private LuaRandomizerWrapper luaRandomizer;

    public ActionBank(LuaRandomizerWrapper luaRandomizer) {
        this.luaRandomizer = luaRandomizer;
        this.allActions = new HashMap<>();
        this.actionsByCategory = new HashMap<>();
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

            String category = action.getCategory();
            HashMap<Integer, Action> categoryMap = actionsByCategory.get(category);
            if (categoryMap == null) {
                categoryMap = new HashMap<>();
                actionsByCategory.put(category, categoryMap);
            }
            categoryMap.put(action.getId(), action);
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
    public List<String> getEnumValues(String enumName) {
        if (luaRandomizer == null || enumName == null || enumName.isBlank()) {
            return null;
        }
        EnumDefinition enumDefinition = luaRandomizer.getEnumDefinition(enumName);
        return enumDefinition != null ? enumDefinition.getValues() : null;
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

    public Collection<Action> get(String category) {
        return get(category, null);
    }

    public Collection<Action> get(String category, String modifiesField) {
        Collection<Action> actions;
        if (category == null || FILTER_ALL.equals(category)) {
            actions = allActions.values();
        } else {
            HashMap<Integer, Action> found = actionsByCategory.get(category);
            actions = found != null ? found.values() : Collections.emptyList();
        }

        if (modifiesField != null && !FILTER_ALL.equals(modifiesField)) {
            actions = actions.stream().filter(action -> action.getModifies().contains(modifiesField))
                    .collect(Collectors.toList());
        }
        return sortByName(actions);
    }

    public List<String> getCategoriesWithAll() {
        List<String> categories =
                actionsByCategory.keySet().stream().sorted().collect(Collectors.toList());
        categories.add(0, FILTER_ALL);
        return categories;
    }

    public List<String> getModifiesWithAll() {
        TreeSet<String> fields = new TreeSet<>();
        for (Action action : allActions.values()) {
            fields.addAll(action.getModifies());
        }
        List<String> modifies = new ArrayList<>(fields);
        modifies.add(0, FILTER_ALL);
        return modifies;
    }

    private static List<Action> sortByName(Collection<Action> actions) {
        return actions.stream()
                .sorted(Comparator.comparing(Action::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }
}
