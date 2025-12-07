package redactedrice.ptcgr.randomizer.actions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import redactedrice.randomizer.LuaRandomizerWrapper;
import redactedrice.randomizer.lua.Module;

public class ActionBank {
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
        List<Module> modules = luaRandomizer.getAvailableModules();
        for (Module module : modules) {
            Action action = new Action(module);
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

    public Collection<Action> get() {
        return get(null);
    }

    public Collection<Action> get(String category) {
        if (category == null || ActionCategories.CATEGORY_ALL.equals(category)) {
            return allActions.values();
        }
        HashMap<Integer, Action> found = actionsByCategory.get(category);
        return found != null ? found.values() : Collections.emptyList();
    }

    public List<String> getCategoriesWithAll() {
        List<String> categories = actionsByCategory.keySet().stream().sorted()
                .collect(Collectors.toList());
        categories.add(0, ActionCategories.CATEGORY_ALL);
        return categories;
    }
}
