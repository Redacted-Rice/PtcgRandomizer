-- Shared pieces for building pools from ROM/CURRENT card data.
local common_field_defs = require("modules.util.common_field_defs")
local randomizer = require("randomizer")

local pool_utils = {}

-- maps approach to URC useToRandomize pool options. minimize repeats always regenerates
function pool_utils.poolOptions(approach)
	if approach == "MINIMIZE_REPEATS" then
		return { consumable = true, regenerate = true }
	end
	return { consumable = false }
end

-- Stateful pool for incremental draws
function pool_utils.statefulPool(list, approach)
	return randomizer.Pool.new(list, pool_utils.poolOptions(approach))
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
function pool_utils.stageMaxStageKeyFromValues(maxStage, stage)
	return maxStage:getValue() * 10 + stage:getValue()
end

function pool_utils.stageAndMaxStageKey(mc)
	return pool_utils.stageMaxStageKeyFromValues(mc.evoLineMaxStage, mc.stage)
end

function pool_utils.init(context)
	pool_utils.weaknessResistanceAbbreviationKey = function(flagSet)
		return context.WeaknessResistanceFlags.abbreviationKey(flagSet)
	end
end

function pool_utils.combineKeys(...)
	local parts = {}
	for index = 1, select("#", ...) do
		local part = select(index, ...)
		if part ~= nil then
			table.insert(parts, tostring(part))
		end
	end
	if #parts == 0 then
		return nil
	end
	return table.concat(parts, ":")
end

function pool_utils.typeGroupingSplitsIntoPools(scope)
	return scope == common_field_defs.TYPE_POOL_WITHIN_TYPE
			or scope == common_field_defs.TYPE_POOL_WITHIN_SUBTYPE
end

function pool_utils.stageGroupingSplitsIntoPools(grouping)
	return grouping == common_field_defs.STAGE_GROUPING_BY_STAGE
			or grouping == common_field_defs.STAGE_GROUPING_BY_STAGE_AND_MAX_STAGE
end

function pool_utils.getTypeGroupingKey(card, scope)
	if scope == common_field_defs.TYPE_POOL_WITHIN_TYPE then
		return card.type:convertToEnergyType():getAbbreviation()
	end
	if scope == common_field_defs.TYPE_POOL_WITHIN_SUBTYPE then
		local abbrevKey = pool_utils.weaknessResistanceAbbreviationKey
		return pool_utils.combineKeys(
				card.type:convertToEnergyType():getAbbreviation(),
				abbrevKey(card.weakness),
				abbrevKey(card.resistance))
	end
	return nil
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
