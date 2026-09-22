-- Randomizes evo lines from extracted line/branch data: shuffle name pools, apply stage/prevEvo, fix proxies.
local pool_utils = require("modules.util.pool_utils")
local randomizer = require("randomizer")
local utils = require("randomizer.utils")
local logger = require("randomizer.logger")

local ALL_TOGETHER_POOL_KEY = 0

local evolutionStage
local cardType
local EVO_STAGES

local function initContext(context)
	evolutionStage = context.EvolutionStage
	cardType = context.CardType
	EVO_STAGES = {
		context.EvolutionStage.BASIC,
		context.EvolutionStage.STAGE_1,
		context.EvolutionStage.STAGE_2,
	}
end

-- Constructs the pool key for an entry based on the args. They key is
-- variable and will reflect the args to effectively create separate
-- subpools for any permutation of the args
local function poolKey(cardTypeArg, stage, maxStage, grouping, withinType)
	if grouping == "ALL_TOGETHER" then
		if not withinType then
			-- No key - eveything is in one pool. Just return an arbitrary
			-- key so we can treat all cases as keyed for simplicity
			return ALL_TOGETHER_POOL_KEY
		end
		-- Key is just type
		return cardTypeArg:getValue()
	end
	if grouping == "BY_STAGE" then
		if withinType then
			-- key is a combo of type and stage
			return pool_utils.typeStageKeyFromValues(cardTypeArg, stage)
		end
		-- Key is just the stage
		return utils.asTableKey(stage)
	end
	if withinType then
		-- key is a combo of type, maxStage, and stage
		return pool_utils.typeStageMaxStageKeyFromValues(cardTypeArg, maxStage, stage)
	end
	-- key is just the maxStage and stage
	return pool_utils.stageMaxStageKeyFromValues(maxStage, stage)
end

-- Draws a random item from the pool
local function drawNameFromPool(namePools, grouping, withinType, entry)
	local pool = namePools:get(poolKey(entry.type, entry.stage, entry.maxStage, grouping, withinType))
	return utils.consumeRandomElement(pool.items)
end

local function applyStageToCards(cardsOfName, stage, cardName)
	cardsOfName:each(function(card)
		card.stage = stage
	end)
end

local function applyPrevEvoToCards(cardsOfName, prevEvo, cardName)
	cardsOfName:each(function(card)
		card.prevEvoName:setText(prevEvo)
	end)
end

-- For debug logging
local function lineEntriesHasBranch(entries)
	local seenStages = {}
	local branched = false
	entries:each(function(entry)
		if branched then
			return
		end
		local stageValue = entry.stage:getValue()
		if seenStages[stageValue] then
			branched = true
			return
		end
		seenStages[stageValue] = true
	end)
	return branched
end

-- For debug logging
local function formatLineEntries(entries)
	local stageCounts = {}
	for _, stage in ipairs(EVO_STAGES) do
		stageCounts[stage:getValue()] = 0
	end
	entries:each(function(entry)
		local stageValue = entry.stage:getValue()
		stageCounts[stageValue] = stageCounts[stageValue] + 1
	end)
	local basic = stageCounts[evolutionStage.BASIC:getValue()]
	local stage1 = stageCounts[evolutionStage.STAGE_1:getValue()]
	local stage2 = stageCounts[evolutionStage.STAGE_2:getValue()]
	return "shape  = [" .. basic .. ", " .. stage1 .. ", " .. stage2 .. "]"
end

local function assignEvoLineList(evoLineId, entries, namePools, grouping, withinType, toModifyByName)
	local branched = lineEntriesHasBranch(entries)
	if branched then
		logger.info("evo_line_cards filling lineId=" .. tostring(evoLineId) .. " " .. formatLineEntries(entries))
	end

	local idxToName = {}
	entries:each(function(entry, idx)
		local drawnName = drawNameFromPool(namePools, grouping, withinType, entry)
		idxToName[idx] = drawnName
		applyStageToCards(toModifyByName:get(drawnName), entry.stage, drawnName)
		if branched then
			local prevLabel = "(root)"
			if entry.prevEvoIdx ~= nil then
				prevLabel = tostring(idxToName[entry.prevEvoIdx] or entry.prevEvoIdx)
			end
			logger.info(
				"evo_line_cards lineId="
					.. tostring(evoLineId)
					.. " assign #"
					.. idx
					.. " name="
					.. drawnName
					.. " stage="
					.. tostring(entry.stage)
					.. " prev="
					.. prevLabel
			)
		end
	end)

	entries:each(function(entry, idx)
		local cardName = idxToName[idx]
		local prevEvo = ""
		if entry.prevEvoIdx ~= nil then
			prevEvo = idxToName[entry.prevEvoIdx] or ""
		end
		applyPrevEvoToCards(toModifyByName:get(cardName), prevEvo, cardName)
	end)
end

local function assignEvoLineData(evoLineData, namePools, grouping, withinType, toModifyByName)
	evoLineData:each(function(line)
		assignEvoLineList(line.evoLineId, line.entries, namePools, grouping, withinType, toModifyByName)
	end)
end

local function isColorlessBasic(card)
	return card.type == cardType.MONSTER_COLORLESS
		and card.stage:getValue() == evolutionStage.BASIC:getValue()
end

-- Used for fixing proxies to make sure they are basics.
-- Excludes trainer proxies and any basic that a proxy currently evolves from,
-- so swapping into the proxy's slot cannot create a self reference
local function potentialNonProxySwapTargets(toModifyCards, colorlessOnly)
	local proxyPrevNames = {}
	toModifyCards:each(function(card)
		if card.isTrainerProxy and not card.prevEvoName:isEmpty() then
			proxyPrevNames[card.prevEvoName:toString()] = true
		end
	end)

	return toModifyCards
		:filter(function(card)
			if card.isTrainerProxy then
				return false
			end
			if card.stage:getValue() ~= evolutionStage.BASIC:getValue() then
				return false
			end
			if proxyPrevNames[card.name:toString()] then
				return false
			end
			if colorlessOnly and not isColorlessBasic(card) then
				return false
			end
			return true
		end)
		:select("name:toString")
		:removeDuplicates()
end

-- Used for fixing proxies to make sure they are basics
-- Gets all the cards that are not proxies that have a prev evo
local function nonBasicNonProxyByPrevEvo(toModifyCards)
	return toModifyCards
		:filter(function(card)
			return not card.isTrainerProxy
				and card.stage:getValue() ~= evolutionStage.BASIC:getValue()
				and not card.prevEvoName:isEmpty()
		end)
		:groupBy("prevEvoName:toString")
end

-- ALL_TOGETHER only. Proxies must stay basic. Search by name, not evo line groups.
local function fixAllTogetherProxies(toModifyCards, args, toModifyByName)
	if args.grouping ~= "ALL_TOGETHER" then
		return
	end

	-- Step 0: construct maps of data for convinience
	local potentialSwapTargets = potentialNonProxySwapTargets(toModifyCards, args.withinType)
	local evosByPrevEvo = nonBasicNonProxyByPrevEvo(toModifyCards)

	toModifyCards:each(function(proxy)
		-- If its not a proxy or the proxy is already a basic, we are good
		if not proxy.isTrainerProxy then
			return
		end
		if proxy.stage:getValue() == evolutionStage.BASIC:getValue() then
			return
		end

		-- Step 1: random basic that is not a proxy and not a proxy's current prev evo
		local swapName = utils.consumeRandomElement(utils.deepCopy(potentialSwapTargets.items))

		-- Step 2: swap evo data between the selected card and the proxy
		local proxyName = proxy.name:toString()
		local proxyStage = proxy.stage
		local proxyPrev = proxy.prevEvoName:toString()
		applyStageToCards(toModifyByName:get(proxyName), evolutionStage.BASIC, proxyName)
		applyPrevEvoToCards(toModifyByName:get(proxyName), "", proxyName)
		applyStageToCards(toModifyByName:get(swapName), proxyStage, swapName)
		applyPrevEvoToCards(toModifyByName:get(swapName), proxyPrev, swapName)

		-- Step 3: swap prev evo on next stage cards for each side of the swap
		local proxyEvos = evosByPrevEvo:get(proxyName)
		if proxyEvos ~= nil then
			proxyEvos:each(function(card)
				card.prevEvoName:setText(swapName)
			end)
		end
		local swapEvos = evosByPrevEvo:get(swapName)
		if swapEvos ~= nil then
			swapEvos:each(function(card)
				card.prevEvoName:setText(proxyName)
			end)
		end

		logger.debug("evo_line_cards swapped proxy evo " .. proxyName .. " <-> " .. swapName)
	end)
end

-- After we have randomized evo lines, set HAS_EVOLUTION as appropriate and
-- clear ENCOURAGE_EVO when the name cannot evolve.
local function syncEvoAiData(context, cards)
	local hasEvolution = context.CardAiFlags.HAS_EVOLUTION
	local encourageEvo = context.CardAiInfo.ENCOURAGE_EVO
	local noAiInfo = context.CardAiInfo.NONE

	-- Get all the prev evo names
	local namesThatEvolve = cards:groupBy("prevEvoName:toString")
	-- Go through each name of cards and see if it can evolve or not
	-- If it can, set HAS_EVOLUTION. If it can't, remove HAS_EVOLUTION and clear ENCOURAGE_EVO.
	cards:groupBy("name:toString"):each(function(name, cardsOfName)
		local canEvolve = namesThatEvolve:get(name) ~= nil
		cardsOfName:each(function(card)
			if canEvolve then
				card.aiFlags:add(hasEvolution)
			else
				card.aiFlags:remove(hasEvolution)
				if card.aiInfo == encourageEvo then
					card.aiInfo = noAiInfo
				end
			end
		end)
	end)
end

local evo_line_randomize_utils = {}

function evo_line_randomize_utils.randomize(context, args, sourceCards, evoLineData, targets)
	initContext(context)

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

	-- Create the names with the pool key for the given args
	local namePools = randomizer
		.groupFromField(sourceCards, function(card)
			return poolKey(card.type, card.stage, card.evoLineMaxStage, args.grouping, args.withinType)
		end, "name:toString")
		:applyToEachList("removeDuplicates")
	local toModifyByName = randomizer.groupBy(targets, "name")

	assignEvoLineData(evoLineData, namePools, args.grouping, args.withinType, toModifyByName)
	fixAllTogetherProxies(targets, args, toModifyByName)
	syncEvoAiData(context, targets)
end

return evo_line_randomize_utils
