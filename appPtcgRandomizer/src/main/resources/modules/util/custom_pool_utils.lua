-- Pool validation and group building for user defined value lists (HP, retreat, etc).
-- Require as modules.util.custom_pool_utils
local randomizer = require("randomizer")

local custom_pool_utils = {}

-- enum display name, or the raw key if there is no label
function custom_pool_utils.displayName(enumTable, canonical)
	local names = enumTable and enumTable.displayNames
	if names ~= nil and names[canonical] ~= nil and names[canonical] ~= "" then
		return names[canonical]
	end
	return tostring(canonical)
end

-- unknown stages sort last so BASIC / STAGE_1 / STAGE_2 stay in evo order
function custom_pool_utils.stageSortValue(context, stageName)
	local stage = context.EvolutionStage[stageName]
	if stage == nil then
		return 1000
	end
	return stage:getValue()
end

-- stable evo order for error messages and validation walks
function custom_pool_utils.sortedStageNames(context, nameSet)
	local names = {}
	for name in pairs(nameSet) do
		table.insert(names, name)
	end
	table.sort(names, function(a, b)
		local va = custom_pool_utils.stageSortValue(context, a)
		local vb = custom_pool_utils.stageSortValue(context, b)
		if va ~= vb then
			return va < vb
		end
		return tostring(a) < tostring(b)
	end)
	return names
end

-- comma separated label list for validation error text
function custom_pool_utils.joinLabels(labels)
	return table.concat(labels, ", ")
end

-- poolsName is the name of the pools group, e.g. hpPools, retreatPools
-- listPoolName is the name of the list pool, e.g. hpPool, retreatPool
-- valueKind is the kind of values in the list pool, e.g. HP values, retreat cost values
-- returns a table of pool helpers wired to those names for error text
function custom_pool_utils.create(poolsName, listPoolName, valueKind)
	local utils = {}

	-- numeric enum value for a stage or max stage key string
	function utils.stageValue(context, stageName)
		local stage = context.EvolutionStage[stageName]
		if stage == nil then
			error("Unknown EvolutionStage in " .. poolsName .. ": " .. tostring(stageName))
		end
		return stage:getValue()
	end

	-- abort early when a pool list is missing or empty
	function utils.requireNonEmptyList(values, label)
		if values == nil or #values == 0 then
			error(label .. " must be a non-empty list of " .. valueKind)
		end
	end

	-- wrap a user supplied value list as a randomizer list pool.
	-- duplicate entries stay in the list so repeats can weight the pool
	function utils.listPool(values)
		utils.requireNonEmptyList(values, listPoolName)
		return randomizer.list(values)
	end

	-- report extras, then abort on missing so the user sees both in one run
	function utils.reportPoolMapping(missingLabels, extraLabels)
		if #extraLabels > 0 then
			logger.warn(poolsName .. " has unused mappings that no cards use: "
				.. custom_pool_utils.joinLabels(extraLabels))
		end
		if #missingLabels > 0 then
			error(poolsName .. " is missing mappings used by cards: "
				.. custom_pool_utils.joinLabels(missingLabels))
		end
	end

	-- cards look up pools by stage. missing keys abort, extra keys just warn
	function utils.validateStagePools(context, pools, cards)
		local provided = {}
		for stageName in pairs(pools or {}) do
			provided[tostring(stageName)] = true
		end

		local used = {}
		randomizer.list(cards):each(function(mc)
			used[tostring(mc.stage)] = true
		end)

		local missingLabels = {}
		for _, name in ipairs(custom_pool_utils.sortedStageNames(context, used)) do
			if not provided[name] then
				table.insert(missingLabels,
					custom_pool_utils.displayName(context.EvoStage, name))
			end
		end

		local extraLabels = {}
		for _, name in ipairs(custom_pool_utils.sortedStageNames(context, provided)) do
			if not used[name] then
				table.insert(extraLabels,
					custom_pool_utils.displayName(context.EvoStage, name))
			end
		end

		utils.reportPoolMapping(missingLabels, extraLabels)
	end

	-- same as validateStagePools but nested by evo-line max stage then card stage
	function utils.validateStageMaxStagePools(context, pools, cards)
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
		for maxStageName, byStage in pairs(pools or {}) do
			local inner = {}
			for stageName in pairs(byStage or {}) do
				inner[tostring(stageName)] = true
			end
			provided[tostring(maxStageName)] = inner
		end

		local missingLabels = {}
		for _, maxName in ipairs(custom_pool_utils.sortedStageNames(context, used)) do
			local usedInner = used[maxName]
			local providedInner = provided[maxName] or {}
			for _, stageName in ipairs(custom_pool_utils.sortedStageNames(context, usedInner)) do
				if not providedInner[stageName] then
					table.insert(missingLabels,
						custom_pool_utils.displayName(context.NumEvoStages, maxName)
						.. " -> "
						.. custom_pool_utils.displayName(context.EvoStage, stageName))
				end
			end
		end

		local extraLabels = {}
		for _, maxName in ipairs(custom_pool_utils.sortedStageNames(context, provided)) do
			local providedInner = provided[maxName]
			local usedInner = used[maxName] or {}
			for _, stageName in ipairs(custom_pool_utils.sortedStageNames(context, providedInner)) do
				if not usedInner[stageName] then
					table.insert(extraLabels,
						custom_pool_utils.displayName(context.NumEvoStages, maxName)
						.. " -> "
						.. custom_pool_utils.displayName(context.EvoStage, stageName))
				end
			end
		end

		utils.reportPoolMapping(missingLabels, extraLabels)
	end

	-- validate then build a group keyed by stage name (matches card.stage lookup)
	function utils.buildStagePoolGroup(context, pools, cards)
		utils.validateStagePools(context, pools, cards)

		local selected = {}
		local keyOrder = {}

		for stageName, values in pairs(pools or {}) do
			utils.requireNonEmptyList(values, string.format("%s[%s]", poolsName, tostring(stageName)))
			if context.EvolutionStage[stageName] == nil then
				error("Unknown EvolutionStage in " .. poolsName .. ": " .. tostring(stageName))
			end
			selected[stageName] = randomizer.list(values)
			table.insert(keyOrder, stageName)
		end

		return randomizer.group(selected, keyOrder)
	end

	-- validate then build a group keyed by maxStage*10+stage (matches pool_utils.stageAndMaxStageKey)
	function utils.buildStageMaxStagePoolGroup(context, pools, cards)
		utils.validateStageMaxStagePools(context, pools, cards)

		local selected = {}
		local keyOrder = {}

		for maxStageName, byStage in pairs(pools or {}) do
			local maxStageValue = utils.stageValue(context, maxStageName)
			for stageName, values in pairs(byStage or {}) do
				utils.requireNonEmptyList(values, string.format("%s[%s][%s]", poolsName,
					tostring(maxStageName), tostring(stageName)))
				local key = maxStageValue * 10 + utils.stageValue(context, stageName)
				selected[key] = randomizer.list(values)
				table.insert(keyOrder, key)
			end
		end

		return randomizer.group(selected, keyOrder)
	end

	return utils
end

custom_pool_utils.hp = custom_pool_utils.create("hpPools", "hpPool", "HP values")
custom_pool_utils.retreat = custom_pool_utils.create("retreatPools", "retreatPool", "retreat cost values")

return custom_pool_utils
