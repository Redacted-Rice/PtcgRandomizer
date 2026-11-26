-- Shared pieces for building pools from ROM/CURRENT card data.
local enums_def = require("modules.util.enums_def")
local randomizer = require("randomizer")

local pool_utils = {}

-- maps approach to URC useToRandomize pool options. minimize repeats always regenerates
function pool_utils.poolOptions(approach)
	if approach == enums_def.RANDOMIZATION_APPROACH_MINIMIZE_REPEATS then
		return { consumesSelected = true, autoRegenerates = true }
	end
	return { consumesSelected = false }
end

-- Card sourced pools with full duplicate weight and MINIMIZE_REPEATS should not run dry run out of items
function pool_utils.shouldErrorOnExhausted(args)
	if args == nil then
		return false
	end
	if args.approach ~= enums_def.RANDOMIZATION_APPROACH_MINIMIZE_REPEATS then
		return false
	end
	if args.duplicates ~= enums_def.DUPLICATE_HANDLING_KEEP then
		return false
	end
	return args.source == enums_def.DATA_SOURCE_ROM
		or args.source == enums_def.DATA_SOURCE_CURRENT
end

-- Only for modules that randomize values from ROM/CURRENT card pools (source + duplicates on args)
function pool_utils.poolOptionsForCardSource(args)
	local options = pool_utils.poolOptions(args.approach)
	options.errorOnExhausted = pool_utils.shouldErrorOnExhausted(args)
	return options
end

-- monster cards from the chosen source set
function pool_utils.sourceCards(source)
	if source == enums_def.DATA_SOURCE_CURRENT then
		return context.modified.cards:getRandomizableMonsterCards()
	end
	return context.original.cards:getRandomizableMonsterCards()
end

-- composite key for scripts that use max stages. First digit is
-- evo max stage, second digit is card stage
function pool_utils.stageMaxStageKeyFromValues(maxStage, stage)
	return maxStage:getValue() * 10 + stage:getValue()
end

function pool_utils.stageAndMaxStageKey(mc)
	return pool_utils.stageMaxStageKeyFromValues(mc.evoLineMaxStage, mc.stage)
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
	return scope == enums_def.TYPE_POOL_WITHIN_TYPE
			or scope == enums_def.TYPE_POOL_WITHIN_SUBTYPE
end

function pool_utils.stageGroupingSplitsIntoPools(grouping)
	return grouping == enums_def.STAGE_GROUPING_BY_STAGE
			or grouping == enums_def.STAGE_GROUPING_BY_STAGE_AND_MAX_STAGE
end

function pool_utils.getTypeGroupingKey(card, scope)
	if scope == enums_def.TYPE_POOL_WITHIN_TYPE then
		return card.type:convertToEnergyType():getAbbreviation()
	end
	if scope == enums_def.TYPE_POOL_WITHIN_SUBTYPE then
		local weaknessResistanceFlags = context.WeaknessResistanceFlags
		return pool_utils.combineKeys(
				card.type:convertToEnergyType():getAbbreviation(),
				weaknessResistanceFlags.abbreviationKey(card.weakness),
				weaknessResistanceFlags.abbreviationKey(card.resistance))
	end
	return nil
end

-- flat pool of values pulled from source cards
function pool_utils.buildValuePool(sourceCards, valueGetter, duplicates)
	local values = randomizer.list(sourceCards):select(valueGetter)
	if duplicates == enums_def.DUPLICATE_HANDLING_REMOVE then
		return values:removeDuplicates()
	end
	return values
end

-- values grouped by stage or stageAndMaxStageKey, with optional dedup per bucket
function pool_utils.buildGroupedPool(sourceCards, groupKey, valueGetter, duplicates)
	local grouped = randomizer.groupFromField(sourceCards, groupKey, valueGetter)
	if duplicates == enums_def.DUPLICATE_HANDLING_KEEP then
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
