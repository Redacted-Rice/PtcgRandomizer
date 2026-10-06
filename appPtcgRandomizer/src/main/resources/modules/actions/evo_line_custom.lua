-- Randomizes evolution lines using weighted custom evo lines
local common_field_defs = require("modules.util.common_field_defs")
local evo_line_randomize_utils = require("modules.util.evo_line_randomize_utils")
local randomizer = require("randomizer")
local utils = require("randomizer.utils")
local logger = require("randomizer.logger")

-- Definition for a single evo line definition containing the counts
-- for each stage. All stages are displayed even if 0 and BASIC is fixed
-- to 1 (vanilla limitation that we can't converge evos but only divergent
-- ones due to single preEvo name. Display it anyways for clarity)
local evoLineDef = {
	type = "table",
	keyDefinition = common_field_defs.KEY_DEF_EVO_STAGE,
	valueDefinition = {
		type = "int",
		constraint = { type = "range", min = 0, max = 100 },
	},
	fixedKeys = { "BASIC", "STAGE_1", "STAGE_2" },
	fixedValues = { BASIC = 1 },
}

-- Single weighted evo line entry
local weightedEvoLineTupleDef = {
	type = "tuple",
	fields = {
		{
			name = "weight",
			definition = {
				type = "int",
				constraint = { type = "range", min = 1, max = 100 },
			},
		},
		{ name = "evoLine", definition = evoLineDef },
	},
}

local evoLinePoolArgDef = {
	name = "evoLinePool",
	displayName = "Evoution Lines Pool",
	description = "Weighted evolution lines. Each evo line lists how many cards appear at STAGE_1 and"
			.. " STAGE_2 (BASIC is always 1). STAGE_1 slots evolve from the basic. STAGE_2 slots are"
			.. " distributed as evenly as possible between STAGE_1 slots. \n"
			.. " For reference when building lines, there are 155 different named monster cards - 80 Basics"
			.. " (27 Basic/Basic, 26 Basic/Stage 1, 17 Basic/Stage 2), 57 Stage 1 (39 Stage 1/Stage 1,"
			.. " 18 Stage 1/Stage 2), and 18 Stage 2 cards.\n"
			.. " Note that if not using ALL_TOGETHER grouping, it will automatically adjust lines based on"
			.. " what is in the pools so the exact numbers may not match the weights depending on how many"
			.. " of each stage are left.",
	definition = {
		type = "list",
		elementDefinition = weightedEvoLineTupleDef,
	},
	-- Semi arbitrary but fairly similar to the rom values but with some more
	-- branches just for funsies
	default = {
		{
			-- Similar for basic onlies - the 3 stage and 2 stage will
			-- still leave some that would evo out so account for them
			-- here too
			-- 80 - (11 + 1 + 1 + 1)(14) - (30 + 1 + 3)(34) = 32
			weight = 32,
			evoLine = { BASIC = 1, STAGE_1 = 0, STAGE_2 = 0 },
		},
		{
			-- first stage will prefer intermediates for 3 chain evos,
			-- but there will be some leftover for 2 chain evos to
			-- account for as well
			-- 57 - (11 + 1 + 1 + 1)(14) - 3 - (2 * 4)(8) = 32
			weight = 34,
			evoLine = { BASIC = 1, STAGE_1 = 1, STAGE_2 = 0 },
		},
		{
			-- 2 stage will fill nicely
			-- 18 - 2 - 2 -3 = 11
			weight = 11,
			evoLine = { BASIC = 1, STAGE_1 = 1, STAGE_2 = 1 },
		},
		{
			-- Energy type stone based evo line
			weight = 1,
			evoLine = { BASIC = 1, STAGE_1 = 3, STAGE_2 = 0 },
		},
		{
			-- Fossil evo line
			weight = 1,
			evoLine = { BASIC = 1, STAGE_1 = 3, STAGE_2 = 2 },
		},
		{
			-- Just one for fun
			weight = 1,
			evoLine = { BASIC = 1, STAGE_1 = 1, STAGE_2 = 2 },
		},
		{
			-- Getting a bit crazy with this one
			weight = 1,
			evoLine = { BASIC = 1, STAGE_1 = 2, STAGE_2 = 3 },
		},
		{
			-- Just to make things a bit more interesting for others too
			weight = 4,
			evoLine = { BASIC = 1, STAGE_1 = 2, STAGE_2 = 0 },
		},
	},
}

-- withinType here means each branch picks one energy type for all its slots
-- This is slighlty different than other usage of withinType but I think at
-- a high level is a similar concept so it makes sense to "reuse" the term
local withinTypeArgDef = {
	name = "withinType",
	displayName = "Within Energy Type (Per Branch)",
	description = "When enabled, each branch picks one energy type. Every slot on that branch uses it."
			.. " Branches may differ from each other and from the basic.",
	definition = {
		type = "boolean",
	},
	default = false,
}

local module
module = {
	id = "evo_line_custom",
	name = "Randomize Evolution Lines (Custom)",
	description = "Randomizes evolution lines with custom defined evo line data. Grouping controls"
			.. " whether evolutions, stages, or neither are preserved. Need to re-run set evo line"
			.. " metadata if needed after this",
	groups = { "Monsters", "Evolutions" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	needs = {
		{ name = "evoLineId", type = "integer" },
		{ name = "evoLineMaxStage", type = "EvolutionStage" },
	},
	arguments = {
		common_field_defs.ARG_DEF_SOURCE,
		common_field_defs.ARG_DEF_RANDOMIZATION_APPROACH,
		common_field_defs.ARG_DEF_EVO_LINE_STAGE_GROUPING,
		withinTypeArgDef,
		evoLinePoolArgDef,
	},
	execute = function(context, args)
		return module.randomizeEvoLines(context, args)
	end,
}

-- Wrapper for a draw pool to keep state together as one object
function module.createPoolDrawState(pool, approach)
	return {
		pool = pool,
		consumable = approach == "MINIMIZE_REPEATS",
		used = randomizer.list({}),
	}
end

-- Step 2: pop a random shape, try it, remove all matching copies if it fails
function module.assignOneLineFromPool(poolState, namePools, grouping, withinType,
		sourceTypes, toModifyByName)
	while true do
		if poolState.pool:isEmpty() then
			-- If we are consuming items and we have some valid used ones, replenish them
			if poolState.consumable and not poolState.used:isEmpty() then
				poolState.pool = poolState.used
				poolState.used = randomizer.list({})
			else
				-- We have exhausted everything, abort
				return false
			end
		end

		-- Pull out a random item from the pool
		local evoLine = utils.consumeRandomElement(poolState.pool.items)
		if evo_line_randomize_utils.tryAssignDrawnLine(evoLine, namePools, grouping, withinType,
				sourceTypes, toModifyByName)
		then
			-- if we are not consuming, add it back
			if not poolState.consumable then
				table.insert(poolState.pool.items, evoLine)
			else
				table.insert(poolState.used.items, evoLine)
			end
			return true
		end

		-- If we failed, remove it from all pools to speed things up
		logger.debug("evo_line_custom discarding shape "
				.. evo_line_randomize_utils.formatEvoLineStagesCounts(evoLine)
				.. " poolLeft=" .. poolState.pool:size()
				.. " used=" .. poolState.used:size())
		poolState.pool = poolState.pool:removeAllMatches(evoLine, evo_line_randomize_utils.evoLinesMatch)
		if poolState.consumable then
			poolState.used = poolState.used:removeAllMatches(evoLine, evo_line_randomize_utils.evoLinesMatch)
		end
	end
end

-- Done one stage at a time in reverse order
-- names left at this stage are one big pool regardless of max stage
-- Will continue until all names are assigned
function module.processStagePass(stage, linePoolByMaxStage, namePools, grouping,
		withinType, sourceTypes, toModifyByName, approach)
	local linePool = linePoolByMaxStage:get(stage:getValue()) or randomizer.list({})
	local poolState = module.createPoolDrawState(linePool, approach)

	while evo_line_randomize_utils.stageTotalPoolCount(namePools, stage, grouping, withinType,
			sourceTypes) > 0 do
		local assigned = module.assignOneLineFromPool(poolState, namePools, grouping,
				withinType, sourceTypes, toModifyByName)
		if not assigned then
			logger.warn("evo_line_custom could not assign remaining " .. tostring(stage)
					.. " names with available line shapes")
			logger.info("evo_line_custom abort " .. tostring(stage)
					.. " remainingNames="
					.. evo_line_randomize_utils.stageTotalPoolCount(namePools, stage, grouping,
							withinType, sourceTypes)
					.. " shapesLeft=" .. poolState.pool:size()
					.. " shapesUsed=" .. poolState.used:size()
					.. " pools=[" .. evo_line_randomize_utils.formatNamePoolCounts(namePools) .. "]")
			break
		end
	end
end

-- Keep drawing lines while any names are left to assign
function module.processAllTogetherPass(linePool, namePools, grouping, withinType,
		sourceTypes, toModifyByName, approach)
	local poolState = module.createPoolDrawState(linePool, approach)

	while namePools:itemCount() > 0 do
		local assigned = module.assignOneLineFromPool(poolState, namePools, grouping,
				withinType, sourceTypes, toModifyByName)
		if not assigned then
			logger.warn("evo_line_custom could not assign remaining names with available line shapes")
			logger.info("evo_line_custom abort ALL_TOGETHER remainingNames="
					.. namePools:itemCount()
					.. " shapesLeft=" .. poolState.pool:size()
					.. " shapesUsed=" .. poolState.used:size()
					.. " pools=[" .. evo_line_randomize_utils.formatNamePoolCounts(namePools) .. "]")
			break
		end
	end
end

function module.randomizeEvoLines(context, args)
	evo_line_randomize_utils.init(context)

	local sourceCards = randomizer.list(evo_line_randomize_utils.sourceCards(context, args.source))
	local targets = randomizer.list(context.modified:getRandomizableMonsterCardsWithProxies())
	local toModifyByName = randomizer.groupBy(targets, "name")

	-- Form name pools from source cards and line pools from script args
	local namePools = evo_line_randomize_utils.buildNamePools(sourceCards, args.grouping,
			args.withinType)
	local sourceTypes = sourceCards:select("type"):removeDuplicates()
	local linePool = randomizer.list(args.evoLinePool or {}):flatMapNTimes("weight",
			function(weighted)
				return weighted.evoLine
			end)

	logger.debug("evo_line_custom source=" .. tostring(args.source) .. " withinType=" ..
			tostring(args.withinType) .. " grouping=" .. tostring(args.grouping) .. " approach=" ..
			tostring(args.approach) .. " cards=" .. sourceCards:size())

	if args.grouping == "ALL_TOGETHER" then
		-- IN a single pass, keep drawing lines while any names are left to assign
		module.processAllTogetherPass(linePool, namePools, args.grouping,
				args.withinType, sourceTypes, toModifyByName, args.approach)
	else
		-- Group line pools by the highest stage in each evo line
		local linePoolByMaxStage = linePool:groupBy(function(evoLine)
			return evo_line_randomize_utils.lineMaxStageFromCounts(evoLine):getValue()
		end)

		-- Go one stage at a time in reverse order to ensure we don't leave any orphans
		local evoStages = evo_line_randomize_utils.EVO_STAGES
		for stageIndex = #evoStages, 1, -1 do
			local stage = evoStages[stageIndex]
			module.processStagePass(stage, linePoolByMaxStage, namePools,
					args.grouping, args.withinType, sourceTypes, toModifyByName, args.approach)
		end
	end

	evo_line_randomize_utils.finalize(context, args, targets, toModifyByName)
end

return module
