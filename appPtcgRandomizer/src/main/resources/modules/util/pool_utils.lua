-- Shared pieces for the common pool args: source, duplicates, approach.
-- Require as modules.util.pool_utils (package path is the parent of the modules dir).
local randomizer = require("randomizer")

local pool_utils = {}

local DEFAULTS = {
	source = "ROM",
	duplicates = "KEEP_DUPLICATES",
	approach = "MINIMIZE_REPEATS",
}

-- Returns the standard source/duplicates/approach arg defs, then any extraArgs
function pool_utils.standardArgs(extraArgs, overrides)
	overrides = overrides or {}
	local args = {
		{
			name = "source",
			displayName = "Pool Source",
			description = "Which card set supplies values for the randomization pool",
			definition = {
				type = "enum",
				constraint = "DataSource",
			},
			default = overrides.source or DEFAULTS.source,
		},
		{
			name = "duplicates",
			displayName = "Duplicate Handling",
			description = "Whether duplicate pool values are kept (weighted by frequency) or reduced to one of each",
			definition = {
				type = "enum",
				constraint = "DuplicateHandling",
			},
			default = overrides.duplicates or DEFAULTS.duplicates,
		},
		{
			name = "approach",
			displayName = "Randomization Approach",
			description = "How values are drawn from the pool. Minimize Repeats consumes values and refills when empty",
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

-- Grouping for scripts that do not need evoLineMaxStage. Max-stage is a separate script.
function pool_utils.groupingArg(default)
	return {
		name = "grouping",
		displayName = "Evo Stage Grouping",
		description = "How source values are grouped into pools. 'All Together' uses a single pool. 'By Stage' groups by the card's evolution stage.",
		definition = {
			type = "enum",
			constraint = "StageGrouping",
			exclude = { "BY_STAGE_AND_MAX_STAGE" },
		},
		default = default or "ALL_TOGETHER",
	}
end

-- Maps approach to URC useToRandomize pool options. Minimize repeats always regenerates
function pool_utils.poolOptions(approach)
	if approach == "MINIMIZE_REPEATS" then
		return { consumable = true, regenerate = true }
	end
	return { consumable = false }
end

function pool_utils.sourceData(context, source)
	if source == "CURRENT" then
		return context.modified
	end
	return context.original
end

function pool_utils.sourceCards(context, source)
	return pool_utils.sourceData(context, source):getRandomizableMonsterCards()
end

function pool_utils.stageAndMaxStageKey(mc)
	return mc.evoLineMaxStage:getValue() * 10 + mc.stage:getValue()
end

function pool_utils.buildValuePool(sourceCards, valueGetter, duplicates)
	local values = randomizer.list(sourceCards):select(valueGetter)
	if duplicates == "REMOVE_DUPLICATES" then
		return values:removeDuplicates()
	end
	return values
end

function pool_utils.buildGroupedPool(sourceCards, groupKey, valueGetter, duplicates)
	local grouped = randomizer.groupFromField(sourceCards, groupKey, valueGetter)
	if duplicates == "KEEP_DUPLICATES" then
		return grouped
	end

	local selected = {}
	local keyOrder = {}
	grouped:each(function(key, list)
		selected[key] = list:removeDuplicates()
		table.insert(keyOrder, key)
	end)
	return randomizer.group(selected, keyOrder)
end

return pool_utils
