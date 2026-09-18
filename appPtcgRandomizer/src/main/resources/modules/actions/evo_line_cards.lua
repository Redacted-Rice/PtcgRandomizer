-- Randomizes evo lines by stage-count triplet: dedupe names per line, group, shuffle pool, apply.
-- BY_STAGE_AND_MAX_STAGE keeps names and shuffles prevEvo within each stage pool.
-- BY_STAGE keeps stage slots but shuffles names within each stage pool.
-- ALL_TOGETHER keeps stage slots but shuffles names from one shared pool.
local common_field_defs = require("modules.util.common_field_defs")
local pool_utils = require("modules.util.pool_utils")
local randomizer = require("randomizer")
local utils = require("randomizer.utils")
local logger = require("randomizer.logger")

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

function module.sourceCards(context, source)
	if source == "CURRENT" then
		return context.modified:getRandomizableMonsterCardsWithProxies()
	end
	return context.original:getRandomizableMonsterCardsWithProxies()
end

-- Returns the number of distinct monsters per evo stage
-- Typically this is one per stage except for branching evos
function module.getStageCounts(line)
	local byName = randomizer.groupBy(line, "name")

	local counts = {}
	for _, stage in ipairs(module.EVO_STAGES) do
		counts[stage:getValue()] = 0
	end

	byName:each(function(_, cardsOfName)
		local card = cardsOfName:get(1)
		local stageValue = card.stage:getValue()
		counts[stageValue] = counts[stageValue] + 1
	end)
	return counts
end

function module.getLineMaxStage(sourceLine)
	local lineMaxStage = nil
	sourceLine:each(function(card)
		local cardMaxStage = card.evoLineMaxStage
		if cardMaxStage ~= nil and (lineMaxStage == nil or cardMaxStage:getValue() > lineMaxStage:getValue()) then
			lineMaxStage = cardMaxStage
		end
	end)
	return lineMaxStage or module.evolutionStage.BASIC
end

function module.createNamePools(sourceCards, args)
	-- We need to stringify name to make sure it matches up correctly for
	-- remove duplicates
	if args.grouping == "BY_STAGE_AND_MAX_STAGE" then
		-- Uses a composite max stage + stage key for simplicity
		return randomizer
			.groupFromField(sourceCards, pool_utils.stageAndMaxStageKey, "name:toString")
			:applyToEachList("removeDuplicates")
	elseif args.grouping == "BY_STAGE" then
		return randomizer.groupFromField(sourceCards, "stage", "name:toString"):applyToEachList("removeDuplicates")
	else
		return sourceCards:select("name:toString"):removeDuplicates()
	end
end

function module.getFromPool(namePools, grouping, stageEnum, lineMaxStage)
	local pool
	if grouping == "BY_STAGE_AND_MAX_STAGE" then
		pool = namePools:get(pool_utils.stageMaxStageKeyFromValues(lineMaxStage, stageEnum))
	elseif grouping == "BY_STAGE" then
		pool = namePools:get(utils.asTableKey(stageEnum))
	else
		pool = namePools
	end
	if pool == nil or pool:isEmpty() then
		error("No name pool for stage " .. tostring(stageEnum))
	end
	return utils.consumeRandomElement(pool.items)
end

function module.applyToCards(cardsOfName, prevEvo, stage, lastCardName)
	if cardsOfName == nil then
		logger.warn("evo_line_cards apply skipped, no target cards for name " .. tostring(lastCardName))
		return
	end
	cardsOfName:each(function(card)
		card.prevEvoName:setText(prevEvo)
		card.stage = stage
	end)
end

-- Currently for logging only. Eventually will probably add branch ids
-- to solve some issues with branching lines
function module.stageCountsHasBranch(stageCounts)
	local basic = stageCounts[module.evolutionStage.BASIC:getValue()]
	local stage1 = stageCounts[module.evolutionStage.STAGE_1:getValue()]
	local stage2 = stageCounts[module.evolutionStage.STAGE_2:getValue()]
	return basic > 1 or stage1 > 1 or stage2 > 1
end

-- For logging evo lineshape for debug
function module.formatStageCounts(stageCounts)
	local basic = stageCounts[module.evolutionStage.BASIC:getValue()]
	local stage1 = stageCounts[module.evolutionStage.STAGE_1:getValue()]
	local stage2 = stageCounts[module.evolutionStage.STAGE_2:getValue()]
	return "shape  = [" .. basic .. ", " .. stage1 .. ", " .. stage2 .. "]"
end

-- map card slot index onto previous stage slots as evenly as possible
function module.prevForSlot(slotIndex, slotCount, prevStageNames)
	if #prevStageNames == 0 then
		return ""
	end
	local prevEvoIndex = math.floor((slotIndex - 1) * #prevStageNames / slotCount) + 1
	return prevStageNames[prevEvoIndex]
end

function module.randomizeLine(evoLineId, sourceLine, lineStageCounts, lineMaxStage, namePools, args, toModifyByName)
	local prevStageNames = {}
	local branched = module.stageCountsHasBranch(lineStageCounts)

	if branched then
		logger.info(
			"evo_line_cards filling lineId="
				.. tostring(evoLineId)
				.. " shape "
				.. module.formatStageCounts(lineStageCounts)
				.. " maxStage="
				.. tostring(lineMaxStage)
		)
	end

	-- Step 3: Go through each evo stage in the stages and assign cards
	-- for each slot (can be multiple if branching)
	for _, stageEnum in ipairs(module.EVO_STAGES) do
		local slotCount = lineStageCounts[stageEnum:getValue()]
		local stageNames = {}
		for slotIndex = 1, slotCount do
			local prevEvo = module.prevForSlot(slotIndex, slotCount, prevStageNames)
			-- Step 3.a: Get a random card from the pool
			local drawnName = module.getFromPool(namePools, args.grouping, stageEnum, lineMaxStage)
			-- Step 3.b: Apply the card to the cards to modify
			module.applyToCards(toModifyByName:get(drawnName), prevEvo, stageEnum, drawnName)
			table.insert(stageNames, drawnName)
			if branched then
				logger.info(
					"evo_line_cards lineId="
						.. tostring(evoLineId)
						.. " assign "
						.. tostring(stageEnum)
						.. " #"
						.. slotIndex
						.. " name="
						.. drawnName
						.. " prev="
						.. (prevEvo == "" and "(root)" or prevEvo)
				)
			end
		end
		if branched then
			logger.info(
				"evo_line_cards lineId="
					.. tostring(evoLineId)
					.. " after "
					.. tostring(stageEnum)
					.. " slots=["
					.. table.concat(stageNames, ", ")
					.. "]"
			)
		end
		prevStageNames = stageNames
	end
end

-- Used for fixing proxies to make sure they are basics
function module.basicNonProxyNames(toModifyCards)
	return toModifyCards
		:filter(function(card)
			return not card.isTrainerProxy and card.stage:getValue() == module.evolutionStage.BASIC:getValue()
		end)
		:select("name:toString")
		:removeDuplicates()
end

-- Used for fixing proxies to make sure they are basics
function module.nonBasicNonProxyByPrevEvo(toModifyCards)
	return toModifyCards
		:filter(function(card)
			return not card.isTrainerProxy
				and card.stage:getValue() ~= module.evolutionStage.BASIC:getValue()
				and not card.prevEvoName:isEmpty()
		end)
		:groupBy("prevEvoName:toString")
end

-- ALL_TOGETHER only. Proxies must stay basic. Search by name, not evo line groups.
function module.fixAllTogetherProxies(toModifyCards, args, toModifyByName)
	if args.grouping ~= "ALL_TOGETHER" then
		return
	end

	toModifyCards:each(function(proxy)
		-- If its not a proxy or the proxy is already a basic, we are good
		if not proxy.isTrainerProxy then
			return
		end
		if proxy.stage:getValue() == module.evolutionStage.BASIC:getValue() then
			return
		end

		-- Step 0: construct maps of data for convinience
		-- Step 0.a: list of all basic, non proxy names regardless of max stage
		local basicNames = module.basicNonProxyNames(toModifyCards)
		-- Step 0.b: non basic, non proxy cards grouped by prev evo name
		local byPrevEvo = module.nonBasicNonProxyByPrevEvo(toModifyCards)

		-- Step 1: random basic that is not a proxy, any max evo stage
		if basicNames:isEmpty() then
			logger.warn("evo_line_cards no basic available to swap proxy " .. proxy.name:toString())
			return
		end
		local swapName = utils.consumeRandomElement(utils.deepCopy(basicNames.items))

		-- Step 2: swap evo data between the selected card and the proxy
		local proxyName = proxy.name:toString()
		local proxyStage = proxy.stage
		local proxyPrev = proxy.prevEvoName:toString()
		module.applyToCards(toModifyByName:get(proxyName), "", module.evolutionStage.BASIC, proxyName)
		module.applyToCards(toModifyByName:get(swapName), proxyPrev, proxyStage, swapName)

		-- Step 3: swap prev evo on next stage cards for each side of the swap
		local proxyEvos = byPrevEvo:get(proxyName)
		if proxyEvos ~= nil then
			proxyEvos:each(function(card)
				card.prevEvoName:setText(swapName)
			end)
		end
		local swapEvos = byPrevEvo:get(swapName)
		if swapEvos ~= nil then
			swapEvos:each(function(card)
				card.prevEvoName:setText(proxyName)
			end)
		end

		logger.debug("evo_line_cards swapped proxy evo " .. proxyName .. " <-> " .. swapName)
	end)
end

function module.randomizeEvoLinesWithinType(context, args, sourceCards, toModifyCards)
	-- Step 1: Get all the needed data now so when we modify it, if its the same
	-- source and target, we already have it copied and it won't be impacted

	-- 1.a. Name pools by grouping
	local namePools = module.createNamePools(sourceCards, args)
	-- 1.b. Cards to modify by name for convinience in setting data later
	local toModifyByName = randomizer.groupBy(toModifyCards, "name")
	-- Step 2: Snapshot line shape + max stage before any pool draws
	local lineData = randomizer.groupBy(sourceCards, "evoLineId"):map(function(evoLineId, sourceLine)
		return {
			evoLineId = evoLineId,
			sourceLine = sourceLine,
			stageCounts = module.getStageCounts(sourceLine),
			lineMaxStage = module.getLineMaxStage(sourceLine),
		}
	end)

	lineData:each(function(line)
		module.randomizeLine(
			line.evoLineId,
			line.sourceLine,
			line.stageCounts,
			line.lineMaxStage,
			namePools,
			args,
			toModifyByName
		)
	end)
	module.fixAllTogetherProxies(toModifyCards, args, toModifyByName)
end

function module.randomizeEvoLines(context, args)
	-- These need to be created with the context as its not available at load time
	module.evolutionStage = context.EvolutionStage
	module.EVO_STAGES = {
		context.EvolutionStage.BASIC,
		context.EvolutionStage.STAGE_1,
		context.EvolutionStage.STAGE_2,
	}

	local sourceCards = randomizer.list(module.sourceCards(context, args.source))
	local targets = randomizer.list(context.modified:getRandomizableMonsterCardsWithProxies())

	logger.debug(
		"evo_line_cards source="
			.. tostring(args.source)
			.. " withinType="
			.. tostring(args.withinType)
			.. " grouping="
			.. tostring(args.grouping)
			.. " cards="
			.. sourceCards:size()
	)

	-- Stage 0: Type is just handled by doing it by groups of one type at a time
	if args.withinType then
		-- Its awkward but we need by type for both targets and soruce cards. Group one
		-- loop the other and get the matching keys from the grouped one to pass in tandem
		local targetsByType = randomizer.groupBy(targets, "type")
		local sourceByType = randomizer.groupBy(sourceCards, "type")
		local passesRun = 0
		local passesSkipped = 0
		sourceByType:each(function(type, typedSourceCards)
			local typedTargets = targetsByType:get(type)
			if typedTargets ~= nil then
				passesRun = passesRun + 1
				module.randomizeEvoLinesWithinType(context, args, typedSourceCards, typedTargets)
			else
				passesSkipped = passesSkipped + 1
				logger.warn("evo_line_cards skipped type=" .. tostring(type) .. " (no matching target group)")
			end
		end)
		logger.debug("evo_line_cards withinType passesRun=" .. passesRun .. " passesSkipped=" .. passesSkipped)
	else
		module.randomizeEvoLinesWithinType(context, args, sourceCards, targets)
	end
end

return module
