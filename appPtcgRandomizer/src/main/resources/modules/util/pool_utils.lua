-- Shared pieces for building pools from ROM/CURRENT card data.
local randomizer = require("randomizer")

local pool_utils = {}

-- maps approach to URC useToRandomize pool options. minimize repeats always regenerates
function pool_utils.poolOptions(approach)
	if approach == "MINIMIZE_REPEATS" then
		return { consumable = true, regenerate = true }
	end
	return { consumable = false }
end

-- monster cards from the chosen source set
function pool_utils.sourceCards(context, source)
	if source == "CURRENT" then
		return context.modified:getRandomizableMonsterCards()
	end
	return context.original:getRandomizableMonsterCards()
end

-- composite key for scripts that use max stages. First digit is
-- evo max stage, second digit is card stage
function pool_utils.stageAndMaxStageKey(mc)
	return mc.evoLineMaxStage:getValue() * 10 + mc.stage:getValue()
end

-- flat pool of values pulled from source cards
function pool_utils.buildValuePool(sourceCards, valueGetter, duplicates)
	local values = randomizer.list(sourceCards):select(valueGetter)
	if duplicates == "REMOVE_DUPLICATES" then
		return values:removeDuplicates()
	end
	return values
end

-- values grouped by stage or stageAndMaxStageKey, with optional dedup per bucket
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
