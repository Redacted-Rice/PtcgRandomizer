-- Randomizes evo lines from per-line slot lists: dedupe names, shuffle pools, apply stage then prevEvo.
-- BY_STAGE_AND_MAX_STAGE keeps slot shape and shuffles names within each maxStage/stage pool.
-- BY_STAGE keeps slot shape but shuffles names within each stage pool.
-- ALL_TOGETHER keeps slot shape but ignores stage when picking names.
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

	ALL_TOGETHER_POOL_KEY = 0,
}

function module.sourceCards(context, source)
	if source == "CURRENT" then
		return context.modified:getRandomizableMonsterCardsWithProxies()
	end
	return context.original:getRandomizableMonsterCardsWithProxies()
end

-- Constructs the pool key for an entry based on the args. They key is
-- variable and will reflect the args to effectively create separate
-- subpools for any permutation of the args
function module.poolKey(cardType, stage, maxStage, grouping, withinType)
	if grouping == "ALL_TOGETHER" then
		if not withinType then
			-- No key - eveything is in one pool. Just return an arbitrary
			-- key so we can treat all cases as keyed for simplicity
			return module.ALL_TOGETHER_POOL_KEY
		end
		-- Key is just type
		return cardType:getValue()
	end
	if grouping == "BY_STAGE" then
		if withinType then
			-- key is a combo of type and stage
			return pool_utils.typeStageKeyFromValues(cardType, stage)
		end
		-- Key is just the stage
		return utils.asTableKey(stage)
	end
	if withinType then
		-- key is a combo of type, maxStage, and stage
		return pool_utils.typeStageMaxStageKeyFromValues(cardType, maxStage, stage)
	end
	-- key is just the maxStage and stage
	return pool_utils.stageMaxStageKeyFromValues(maxStage, stage)
end

-- Draws a random item from the pool
function module.drawNameFromPool(namePools, grouping, withinType, entry)
	local pool = namePools:get(module.poolKey(entry.type, entry.stage, entry.maxStage, grouping, withinType))
	return utils.consumeRandomElement(pool.items)
end

-- Creates a list of each cards' data (type, stage, maxStage, prevEvoIdx) in the line
function module.extractLineData(sourceLine)
	local nameToIdx = {}
	local entries = {}

	-- first sort by stage. This just makes it so we can know the prev evo should have been processed
	-- making the logic a bit simpler
	local sortedByStage = sourceLine:sort(function(a, b)
		return a.stage:getValue() < b.stage:getValue()
	end)

	-- group by name then for each group of names create and add one line entry data
	sortedByStage:groupBy("name"):each(function(name, cardsOfName)
		local card = cardsOfName:get(1)
		local prevEvoIdx = nil
		local prevName = card.prevEvoName:toString()
		if prevName ~= "" then
			prevEvoIdx = nameToIdx[prevName]
		end

		table.insert(entries, {
			type = card.type,
			stage = card.stage,
			maxStage = card.evoLineMaxStage,
			prevEvoIdx = prevEvoIdx,
		})
		-- Store the index by name for quick lookup of prev evo
		nameToIdx[name] = #entries
	end)

	return randomizer.list(entries)
end

-- Extract the evo line data from the cards
function module.extractAllEvoLineData(sourceCards)
	-- For each evo line, map it to the evo line data
	return randomizer.groupBy(sourceCards, "evoLineId"):map(function(evoLineId, sourceLine)
		return {
			-- We don't need original evo line id but keep it for debugging
			evoLineId = evoLineId,
			entries = module.extractLineData(sourceLine),
		}
	end)
end

function module.applyStageToCards(cardsOfName, stage, cardName)
	cardsOfName:each(function(card)
		card.stage = stage
	end)
end

function module.applyPrevEvoToCards(cardsOfName, prevEvo, cardName)
	cardsOfName:each(function(card)
		card.prevEvoName:setText(prevEvo)
	end)
end

-- For debug logging
function module.lineEntriesHasBranch(entries)
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
function module.formatLineEntries(entries)
	local stageCounts = {}
	for _, stage in ipairs(module.EVO_STAGES) do
		stageCounts[stage:getValue()] = 0
	end
	entries:each(function(entry)
		local stageValue = entry.stage:getValue()
		stageCounts[stageValue] = stageCounts[stageValue] + 1
	end)
	local basic = stageCounts[module.evolutionStage.BASIC:getValue()]
	local stage1 = stageCounts[module.evolutionStage.STAGE_1:getValue()]
	local stage2 = stageCounts[module.evolutionStage.STAGE_2:getValue()]
	return "shape  = [" .. basic .. ", " .. stage1 .. ", " .. stage2 .. "]"
end

function module.assignEvoLineList(evoLineId, entries, namePools, grouping, withinType, toModifyByName)
	local branched = module.lineEntriesHasBranch(entries)
	if branched then
		logger.info("evo_line_cards filling lineId=" .. tostring(evoLineId) .. " " .. module.formatLineEntries(entries))
	end

	local idxToName = {}
	entries:each(function(entry, idx)
		local drawnName = module.drawNameFromPool(namePools, grouping, withinType, entry)
		idxToName[idx] = drawnName
		module.applyStageToCards(toModifyByName:get(drawnName), entry.stage, drawnName)
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
		module.applyPrevEvoToCards(toModifyByName:get(cardName), prevEvo, cardName)
	end)
end

function module.assignEvoLineData(evoLineData, namePools, grouping, withinType, toModifyByName)
	evoLineData:each(function(line)
		module.assignEvoLineList(line.evoLineId, line.entries, namePools, grouping, withinType, toModifyByName)
	end)
end

function module.isColorlessBasic(card)
	return card.type == module.cardType.MONSTER_COLORLESS
		and card.stage:getValue() == module.evolutionStage.BASIC:getValue()
end

-- Used for fixing proxies to make sure they are basics.
-- Excludes trainer proxies and any basic that a proxy currently evolves from,
-- so swapping into the proxy's slot cannot create a self reference
function module.potentialNonProxySwapTargets(toModifyCards, colorlessOnly)
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
			if card.stage:getValue() ~= module.evolutionStage.BASIC:getValue() then
				return false
			end
			if proxyPrevNames[card.name:toString()] then
				return false
			end
			if colorlessOnly and not module.isColorlessBasic(card) then
				return false
			end
			return true
		end)
		:select("name:toString")
		:removeDuplicates()
end

-- Used for fixing proxies to make sure they are basics
-- Gets all the cards that are not proxies that have a prev evo
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

	-- Step 0: construct maps of data for convinience
	local potentialSwapTargets = module.potentialNonProxySwapTargets(toModifyCards, args.withinType)
	local evosByPrevEvo = module.nonBasicNonProxyByPrevEvo(toModifyCards)

	toModifyCards:each(function(proxy)
		-- If its not a proxy or the proxy is already a basic, we are good
		if not proxy.isTrainerProxy then
			return
		end
		if proxy.stage:getValue() == module.evolutionStage.BASIC:getValue() then
			return
		end

		-- Step 1: random basic that is not a proxy and not a proxy's current prev evo
		local swapName = utils.consumeRandomElement(utils.deepCopy(potentialSwapTargets.items))

		-- Step 2: swap evo data between the selected card and the proxy
		local proxyName = proxy.name:toString()
		local proxyStage = proxy.stage
		local proxyPrev = proxy.prevEvoName:toString()
		module.applyStageToCards(toModifyByName:get(proxyName), module.evolutionStage.BASIC, proxyName)
		module.applyPrevEvoToCards(toModifyByName:get(proxyName), "", proxyName)
		module.applyStageToCards(toModifyByName:get(swapName), proxyStage, swapName)
		module.applyPrevEvoToCards(toModifyByName:get(swapName), proxyPrev, swapName)

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

function module.randomizeEvoLines(context, args)
	module.evolutionStage = context.EvolutionStage
	module.cardType = context.CardType
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

	-- Create the names with the pool key for the given args
	local namePools = randomizer
		.groupFromField(sourceCards, function(card)
			return module.poolKey(card.type, card.stage, card.evoLineMaxStage, args.grouping, args.withinType)
		end, "name:toString")
		:applyToEachList("removeDuplicates")
	local toModifyByName = randomizer.groupBy(targets, "name")
	local evoLineData = module.extractAllEvoLineData(sourceCards)

	module.assignEvoLineData(evoLineData, namePools, args.grouping, args.withinType, toModifyByName)
	module.fixAllTogetherProxies(targets, args, toModifyByName)
end

return module
