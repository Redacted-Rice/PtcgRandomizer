-- Randomizes evo lines by stage-count triplet: dedupe names per line, group, shuffle pool, apply.
-- BY_STAGE_AND_MAX_STAGE keeps names and shuffles prevEvo within each stage pool.
-- BY_STAGE keeps stage slots but shuffles names within each stage pool.
-- ALL_TOGETHER keeps stage slots but shuffles names from one shared pool.
local common_field_defs = require("modules.util.common_field_defs")
local pool_utils = require("modules.util.pool_utils")
local randomizer = require("randomizer")
local utils = require("randomizer.utils")

local module
module = {
	id = "evo_line_cards",
	name = "Randomize Evolution Lines (From Cards)",
	description = "Randomizes evolution lines from card data. Grouping controls whether evolutions, stages,"
					.. " or neither are preserved. Need to re run set evo line metadata if needed after this",
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
		common_field_defs.ARG_DEF_WITHIN_TYPE,
		{
			name = "grouping",
			displayName = "Evo Stage Grouping",
			description = "By Stage And Max Stage keeps names and shuffles who they evolve from."
							.. " By Stage keeps line shape and stage but shuffles names within each stage pool."
							.. " All Together keeps line shape but ignores stage when picking names.",
			definition = {
				type = "enum",
				constraint = "StageGrouping",
			},
			default = "BY_STAGE_AND_MAX_STAGE",
		},
	},
	execute = function(context, args)
		return module.randomizeEvoLines(context, args)
	end,
}

-- Returns the number of distinct monsters per evo stage
-- Typically this is one per stage except for branching evos
function module.getStageCounts(line)
	local byName = randomizer.groupBy(line, "name")

	local counts = {}
	for _, stage in ipairs(module.EVO_STAGES) do
		counts[stage:getValue()] = 0
	end

	byName:each(function(name, cardsOfName)
		local card = cardsOfName:get(1)
		local stageValue = card.stage:getValue()
		counts[stageValue] = counts[stageValue] + 1
	end)
	return counts
end

function module.getMaxStage(stageCounts)
	local prevStage = module.evolutionStage.BASIC
	for _, stage in ipairs(module.EVO_STAGES) do
		if stageCounts[stage:getValue()] <= 0 then
			return prevStage
		end
		prevStage = stage
	end
	return prevStage
end

function module.createNamePools(sourceCards, args)
	-- We need to stringify name to make sure it matches up correctly for
	-- remove duplicates
	if args.grouping == "BY_STAGE_AND_MAX_STAGE" then
		-- Uses a composite max stage + stage key for simplicity
		return randomizer.groupFromField(sourceCards, pool_utils.stageAndMaxStageKey, "name:toString")
			:applyToEachList("removeDuplicates")
	elseif args.grouping == "BY_STAGE" then
		return randomizer.groupFromField(sourceCards, "stage", "name:toString")
			:applyToEachList("removeDuplicates")
	else
		return sourceCards:select("name:toString"):removeDuplicates()
	end
end

function module.getFromPool(namePools, grouping, stageEnum, maxStage)
	local pool
	if grouping == "BY_STAGE_AND_MAX_STAGE" then
		pool = namePools:get(pool_utils.stageMaxStageKeyFromValues(maxStage, stageEnum))
	elseif grouping == "BY_STAGE" then
		pool = namePools:get(utils.asTableKey(stageEnum))
	else
		pool = namePools
	end
	return utils.consumeRandomElement(pool.items)
end

function module.applyToCards(cardsOfName, prevEvo, stage)
	cardsOfName:each(function(card)
		card.prevEvoName:setText(prevEvo)
		card.stage = stage
	end)
end

function module.randomizeLine(lineStageCounts, namePools, args, toModifyByName)
	local maxStage = module.getMaxStage(lineStageCounts)
	local prevEvo = ""
	local stage = module.evolutionStage.BASIC

    -- Step 3: Go through each evo stage in the stages and assign cards
    -- for each slot (can be multiple if branching)
	for _, stageEnum in ipairs(module.EVO_STAGES) do
		local slotCount = lineStageCounts[stageEnum:getValue()]
		local lastCardName = ""
		for i = 1, slotCount do
            -- Step 3.a: Get a random card from the pool
			lastCardName = module.getFromPool(namePools, args.grouping, stageEnum, maxStage)
            -- Step 3.b: Apply the card to the cards to modify
            module.applyToCards(toModifyByName:get(lastCardName), prevEvo, stageEnum)
		end
        -- After we finish with the stage, update the prev evo name and next stage
		prevEvo = lastCardName
        stage = stageEnum
	end
end

function module.randomizeEvoLinesWithinType(context, args, sourceCards, toModifyCards)
    -- Step 1: Get all the needed data now so when we modify it, if its the same
    -- source and target, we already have it copied and it won't be impacted

    -- 1.a. Name pools by grouping
	local namePools = module.createNamePools(sourceCards, args)
    -- 1.b. Cards to modify by name for convinience in setting data later
	local toModifyByName = randomizer.groupBy(toModifyCards, "name")
    -- 1.c. Line Stage Data for determining what to generate when randomizing
    local allLineStageCounts = randomizer.groupBy(sourceCards, "evoLineId"):map(function(lineId, sourceLine)
        return module.getStageCounts(sourceLine)
    end)

	-- Step 2: Go through each line stage data and randomize it
	allLineStageCounts:each(function(lineStageCounts)
		module.randomizeLine(lineStageCounts, namePools, args, toModifyByName)
	end)
end

function module.randomizeEvoLines(context, args)
	-- These need to be created with the context as its not available at load time
    module.evolutionStage = context.EvolutionStage
	module.EVO_STAGES = {
		context.EvolutionStage.BASIC,
		context.EvolutionStage.STAGE_1,
		context.EvolutionStage.STAGE_2,
	}

	local sourceCards = randomizer.list(pool_utils.sourceCards(context, args.source))
	local targets = randomizer.list(context.modified:getRandomizableMonsterCards())

	-- Stage 0: Type is just handled by doing it by groups of one type at a time
	if args.withinType then
		-- Its awkward but we need by type for both targets and soruce cards. Group one
		-- loop the other and get the matching keys from the grouped one to pass in tandem
		local targetsByType = randomizer.groupBy(targets, "type")
		randomizer.groupBy(sourceCards, "type"):each(function(type, typedSourceCards)
			local typedTargets = targetsByType:get(type)
			if typedTargets ~= nil then
				module.randomizeEvoLinesWithinType(context, args, typedSourceCards, typedTargets)
			end
		end)
	else
		module.randomizeEvoLinesWithinType(context, args, sourceCards, targets)
	end
end

return module
