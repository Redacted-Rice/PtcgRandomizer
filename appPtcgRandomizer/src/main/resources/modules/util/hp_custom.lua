-- Shared helpers for custom HP pool randomizers.
-- Require as modules.util.hp_custom
local randomizer = require("randomizer")
local pool = require("modules.util.pool")

local hp_custom = {}

hp_custom.HP_LIST_ELEMENT = {
	type = "int",
	constraint = { type = "discrete_range", min = 10, max = 120, step = 10 },
}

function hp_custom.approachArg()
	return {
		name = "approach",
		displayName = "Randomization Approach",
		description = "How values are drawn from the pool. Minimize Repeats consumes values and refills when empty",
		definition = {
			type = "enum",
			constraint = "RandomizationApproach",
		},
		default = "MINIMIZE_REPEATS",
	}
end

-- Evo stage keys (BASIC / STAGE_1 / STAGE_2) with friendly UI labels
function hp_custom.evoStageKeyDef()
	return {
		type = "enum",
		constraint = "EvoStage",
	}
end

-- Evo-line max stage keys with friendly UI labels
function hp_custom.evoLineStagesKeyDef()
	return {
		type = "enum",
		constraint = "EvoLineStages",
	}
end

function hp_custom.stageValue(context, stageName)
	local stage = context.EvolutionStage[stageName]
	if stage == nil then
		error("Unknown EvolutionStage in hpPools: " .. tostring(stageName))
	end
	return stage:getValue()
end

function hp_custom.requireNonEmptyList(values, label)
	if values == nil or #values == 0 then
		error(label .. " must be a non-empty list of HP values")
	end
end

function hp_custom.listPool(values)
	hp_custom.requireNonEmptyList(values, "hpPool")
	return randomizer.list(values)
end

function hp_custom.buildStagePoolGroup(context, hpPools)
	local selected = {}
	local keyOrder = {}

	for stageName, values in pairs(hpPools or {}) do
		hp_custom.requireNonEmptyList(values, string.format("hpPools[%s]", tostring(stageName)))
		-- Canonical stage name strings (BASIC / STAGE_1 / STAGE_2). Matches
		-- groupBy / useToRandomize keys after asTableKey stringifies card.stage
		if context.EvolutionStage[stageName] == nil then
			error("Unknown EvolutionStage in hpPools: " .. tostring(stageName))
		end
		selected[stageName] = randomizer.list(values)
		table.insert(keyOrder, stageName)
	end

	return randomizer.group(selected, keyOrder)
end

function hp_custom.buildStageMaxStagePoolGroup(context, hpPools)
	local selected = {}
	local keyOrder = {}

	for maxStageName, byStage in pairs(hpPools or {}) do
		local maxStageValue = hp_custom.stageValue(context, maxStageName)
		for stageName, values in pairs(byStage or {}) do
			hp_custom.requireNonEmptyList(values, string.format(
				"hpPools[%s][%s]", tostring(maxStageName), tostring(stageName)))
			local key = maxStageValue * 10 + hp_custom.stageValue(context, stageName)
			selected[key] = randomizer.list(values)
			table.insert(keyOrder, key)
		end
	end

	return randomizer.group(selected, keyOrder)
end

function hp_custom.randomize(context, args, valuePool, targetKey)
	local targets = context.modified:getRandomizableMonsterCards()
	local options = pool.poolOptions(args.approach)

	if targetKey then
		valuePool:useToRandomize(targets, targetKey, "setHp", options)
	else
		valuePool:useToRandomize(targets, "setHp", options)
	end
end

return hp_custom
