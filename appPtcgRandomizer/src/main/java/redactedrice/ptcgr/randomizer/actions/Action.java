package redactedrice.ptcgr.randomizer.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import redactedrice.ptcgr.rom.Rom;

public abstract class Action {
	private static int nextId = 0;
	
	private int id;
	private String category;
	private String subcategory;
	private StringLambda nameLambda;
	private StringLambda descriptionLambda;
	private Map<String, DynamicConfig<?>> configs;
	
	protected Action(String category, String name, String description) {
		this.id = nextId++;
		this.category = category;
		this.subcategory = "";
		this.nameLambda = configs -> name;
		this.descriptionLambda = configs -> description;
		this.configs = new HashMap<>();
	}
	
	protected Action(String category, StringLambda name, StringLambda description) {
		this.id = nextId++;
		this.category = category;
		this.subcategory = "";
		this.nameLambda = name;
		this.descriptionLambda = description;
		this.configs = new HashMap<>();
	}
	
	abstract public void Perform(Rom rom); 
	
	public void addConfig(String name, DynamicConfig<?> config) {
		configs.put(name, config);
	}
	
	public int getId() {
		return id;
	}

	public String getCategory() {
		return category;
	}
	
	public String getSubcategory() {
		return subcategory;
	}
	
	public String getName() {
		return nameLambda.getString(configs);
	}
	
	public String getDescription() {
		return descriptionLambda.getString(configs);
	}

	public int numConfigs() {
		return configs.size();
	}
	
	public Set<String> getConfigNames() {
		return configs.keySet();
	}
	
	public DynamicConfig<?> getConfig(String name) {
		return configs.get(name);
	}
}
