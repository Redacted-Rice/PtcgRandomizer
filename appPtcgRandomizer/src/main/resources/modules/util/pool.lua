-- Shared pieces for the common pool args: source, duplicates, approach.
-- Require as modules.util.pool (package path is the parent of the modules dir).
local randomizer = require("randomizer")

local pool = {}

local DEFAULTS = {
	source = "ORIGINAL",
	duplicates = "KEEP_DUPLICATES",
	approach = "MINIMIZE_REPEATS",
}

-- Returns the standard source/duplicates/approach arg defs, then any extraArgs
function pool.standardArgs(extraArgs, overrides)
	overrides = overrides or {}
	local args = {
		{
			name = "source",
			definition = {
				type = "enum",
				constraint = "CardDataSource",
			},
			default = overrides.source or DEFAULTS.source,
		},
		{
			name = "duplicates",
			definition = {
				type = "enum",
				constraint = "DuplicateHandling",
			},
			default = overrides.duplicates or DEFAULTS.duplicates,
		},
		{
			name = "approach",
			definition = {
				type = "enum",
				constraint = "RandomizationApproach",
			},
			default = overrides.approach or DEFAULTS.approach,
		},
	}

	for _, arg in ipairs(extraArgs or {}) do
		args[#args + 1] = arg
	end
	return args
end

-- Maps approach to URC useToRandomize pool options. Minimize repeats always regenerates
function pool.poolOptions(approach)
	if approach == "MINIMIZE_REPEATS" then
		return { consumable = true, regenerate = true }
	end
	return { consumable = false }
end

function pool.sourceData(context, source)
	if source == "MODIFIED" then
		return context.modified
	end
	return context.original
end

function pool.sourceCards(context, source)
	return pool.sourceData(context, source):getRandomizableMonsterCards()
end

function pool.stageAndMaxStageKey(mc)
	return mc.evoLineMaxStage:getValue() * 10 + mc.stage:getValue()
end

function pool.uniqueValues(list)
	return randomizer.groupBy(list, function(value)
		return value
	end):map(function(value, _)
		return value
	end)
end

function pool.buildValuePool(sourceCards, valueGetter, duplicates)
	local values = randomizer.list(sourceCards):select(valueGetter)
	if duplicates == "REMOVE_DUPLICATES" then
		return pool.uniqueValues(values)
	end
	return values
end

function pool.buildGroupedPool(sourceCards, groupKey, valueGetter, duplicates)
	local grouped = randomizer.groupFromField(sourceCards, groupKey, valueGetter)
	if duplicates == "KEEP_DUPLICATES" then
		return grouped
	end

	local selected = {}
	local keyOrder = {}
	grouped:each(function(key, list)
		selected[key] = pool.uniqueValues(list)
		table.insert(keyOrder, key)
	end)
	return randomizer.group(selected, keyOrder)
end

return pool
