-- Shared helpers for custom HP pool randomizers.
-- Require as modules.util.hp_custom_utils
local randomizer = require("randomizer")

local hp_custom_utils = {}

hp_custom_utils.HP_LIST_ELEMENT = {
	type = "int",
	constraint = { type = "discrete_range", min = 10, max = 120, step = 10 },
}

function hp_custom_utils.approachArg()
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
function hp_custom_utils.evoStageKeyDef()
	return {
		type = "enum",
		constraint = "EvoStage",
	}
end

-- Evo-line max stage keys with friendly UI labels
function hp_custom_utils.evoLineStagesKeyDef()
	return {
		type = "enum",
		constraint = "EvoLineStages",
	}
end

function hp_custom_utils.stageValue(context, stageName)
	local stage = context.EvolutionStage[stageName]
	if stage == nil then
		error("Unknown EvolutionStage in hpPools: " .. tostring(stageName))
	end
	return stage:getValue()
end

function hp_custom_utils.requireNonEmptyList(values, label)
	if values == nil or #values == 0 then
		error(label .. " must be a non-empty list of HP values")
	end
end

function hp_custom_utils.listPool(values)
	hp_custom_utils.requireNonEmptyList(values, "hpPool")
	return randomizer.list(values)
end

local function displayName(enumTable, canonical)
	local names = enumTable and enumTable.displayNames
	if names ~= nil and names[canonical] ~= nil and names[canonical] ~= "" then
		return names[canonical]
	end
	return tostring(canonical)
end

function hp_custom_utils.stageLabel(context, stageName)
	return displayName(context.EvoStage, stageName)
end

function hp_custom_utils.maxStageLabel(context, maxStageName)
	return displayName(context.EvoLineStages, maxStageName)
end

-- Unknown names sort last so BASIC / STAGE_1 / STAGE_2 stay in evo order
local function stageSortValue(context, stageName)
	local stage = context.EvolutionStage[stageName]
	if stage == nil then
		return 1000
	end
	return stage:getValue()
end

local function sortedStageNames(context, nameSet)
	local names = {}
	for name in pairs(nameSet) do
		table.insert(names, name)
	end
	table.sort(names, function(a, b)
		local va = stageSortValue(context, a)
		local vb = stageSortValue(context, b)
		if va ~= vb then
			return va < vb
		end
		return tostring(a) < tostring(b)
	end)
	return names
end

local function joinLabels(labels)
	return table.concat(labels, ", ")
end

-- Report extras, then abort on missing so the user sees both in one run
local function reportPoolMapping(missingLabels, extraLabels)
	if #extraLabels > 0 then
		logger.warn("hpPools has unused mappings that no cards use: " .. joinLabels(extraLabels))
	end
	if #missingLabels > 0 then
		error("hpPools is missing mappings used by cards: " .. joinLabels(missingLabels))
	end
end

-- Cards look up pools by stage. Missing keys abort, extra keys are unused so just warn.
function hp_custom_utils.validateStagePools(context, hpPools, cards)
	local provided = {}
	for stageName in pairs(hpPools or {}) do
		provided[tostring(stageName)] = true
	end

	local used = {}
	randomizer.list(cards):each(function(mc)
		used[tostring(mc.stage)] = true
	end)

	local missingLabels = {}
	for _, name in ipairs(sortedStageNames(context, used)) do
		if not provided[name] then
			table.insert(missingLabels, hp_custom_utils.stageLabel(context, name))
		end
	end

	local extraLabels = {}
	for _, name in ipairs(sortedStageNames(context, provided)) do
		if not used[name] then
			table.insert(extraLabels, hp_custom_utils.stageLabel(context, name))
		end
	end

	reportPoolMapping(missingLabels, extraLabels)
end

function hp_custom_utils.validateStageMaxStagePools(context, hpPools, cards)
	local used = {}
	randomizer.list(cards):each(function(mc)
		local maxName = tostring(mc.evoLineMaxStage)
		local stageName = tostring(mc.stage)
		if used[maxName] == nil then
			used[maxName] = {}
		end
		used[maxName][stageName] = true
	end)

	local provided = {}
	for maxStageName, byStage in pairs(hpPools or {}) do
		local inner = {}
		for stageName in pairs(byStage or {}) do
			inner[tostring(stageName)] = true
		end
		provided[tostring(maxStageName)] = inner
	end

	local missingLabels = {}
	for _, maxName in ipairs(sortedStageNames(context, used)) do
		local usedInner = used[maxName]
		local providedInner = provided[maxName] or {}
		for _, stageName in ipairs(sortedStageNames(context, usedInner)) do
			if not providedInner[stageName] then
				table.insert(missingLabels, hp_custom_utils.maxStageLabel(context, maxName)
					.. " -> " .. hp_custom_utils.stageLabel(context, stageName))
			end
		end
	end

	local extraLabels = {}
	for _, maxName in ipairs(sortedStageNames(context, provided)) do
		local providedInner = provided[maxName]
		local usedInner = used[maxName] or {}
		for _, stageName in ipairs(sortedStageNames(context, providedInner)) do
			if not usedInner[stageName] then
				table.insert(extraLabels, hp_custom_utils.maxStageLabel(context, maxName)
					.. " -> " .. hp_custom_utils.stageLabel(context, stageName))
			end
		end
	end

	reportPoolMapping(missingLabels, extraLabels)
end

function hp_custom_utils.buildStagePoolGroup(context, hpPools, cards)
	hp_custom_utils.validateStagePools(context, hpPools, cards)

	local selected = {}
	local keyOrder = {}

	for stageName, values in pairs(hpPools or {}) do
		hp_custom_utils.requireNonEmptyList(values, string.format("hpPools[%s]", tostring(stageName)))
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

function hp_custom_utils.buildStageMaxStagePoolGroup(context, hpPools, cards)
	hp_custom_utils.validateStageMaxStagePools(context, hpPools, cards)

	local selected = {}
	local keyOrder = {}

	for maxStageName, byStage in pairs(hpPools or {}) do
		local maxStageValue = hp_custom_utils.stageValue(context, maxStageName)
		for stageName, values in pairs(byStage or {}) do
			hp_custom_utils.requireNonEmptyList(values, string.format(
				"hpPools[%s][%s]", tostring(maxStageName), tostring(stageName)))
			local key = maxStageValue * 10 + hp_custom_utils.stageValue(context, stageName)
			selected[key] = randomizer.list(values)
			table.insert(keyOrder, key)
		end
	end

	return randomizer.group(selected, keyOrder)
end

return hp_custom_utils
