-- Randomizes evo lines from extracted line/branch data: shuffle name pools, apply stage/prevEvo, fix proxies.
local pool_utils = require("modules.util.pool_utils")
local randomizer = require("randomizer")
local utils = require("randomizer.utils")
local logger = require("randomizer.logger")

local evo_line_randomize_utils = {
	ALL_TOGETHER_POOL_KEY = 0,
}

function evo_line_randomize_utils.init(context)
	evo_line_randomize_utils.evolutionStage = context.EvolutionStage
	evo_line_randomize_utils.cardType = context.CardType
	evo_line_randomize_utils.EVO_STAGES = {
		context.EvolutionStage.BASIC,
		context.EvolutionStage.STAGE_1,
		context.EvolutionStage.STAGE_2,
	}
end


-------- Builder functions to extract or form pools from data ----------------


-- Builds the pools of card names based on the args
function evo_line_randomize_utils.buildNamePools(sourceCards, grouping, withinType)
	return randomizer.groupFromField(sourceCards,
			function(card)
				return evo_line_randomize_utils.poolKey(card.type, card.stage, card.evoLineMaxStage, grouping,
						withinType)
			end, "name:toString"):applyToEachList("removeDuplicates")
end

-- Creates a list of each cards' data (type, stage, maxStage, prevEvoIdx) in the line
function evo_line_randomize_utils.extractLineData(sourceLine)
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
function evo_line_randomize_utils.extractAllEvoLineData(sourceCards)
	-- For each evo line, map it to the evo line data
	return randomizer.groupBy(sourceCards, "evoLineId"):map(function(evoLineId, sourceLine)
		return {
			-- We don't need original evo line id but keep it for debugging
			evoLineId = evoLineId,
			entries = evo_line_randomize_utils.extractLineData(sourceLine),
		}
	end)
end

function evo_line_randomize_utils.buildEntriesFromEvoLine(evoLine)
	local evolutionStage = evo_line_randomize_utils.evolutionStage
	local stage1Count = evoLine.STAGE_1 or 0
	local stage2Count = evoLine.STAGE_2 or 0
	local lineMaxStage = evolutionStage.BASIC
	local entries = {}

	table.insert(entries, {
		stage = evolutionStage.BASIC,
		maxStage = lineMaxStage,
		prevEvoIdx = nil,
	})

	if stage1Count > 0 then
		local fullStage1Count = math.floor(stage2Count / stage1Count)
		local remainder = stage2Count % stage1Count
		for s1Index = 1, stage1Count do
			local stage1Idx = #entries + 1
			local stage2ForThis = fullStage1Count + (s1Index <= remainder and 1 or 0)
			local branchMaxStage = stage2ForThis > 0 and evolutionStage.STAGE_2 or evolutionStage.STAGE_1
			table.insert(entries, {
				stage = evolutionStage.STAGE_1,
				maxStage = branchMaxStage,
				prevEvoIdx = 1,
			})
			if branchMaxStage:getValue() > lineMaxStage:getValue() then
				lineMaxStage = branchMaxStage
			end

			for _ = 1, stage2ForThis do
				table.insert(entries, {
					stage = evolutionStage.STAGE_2,
					maxStage = evolutionStage.STAGE_2,
					prevEvoIdx = stage1Idx,
				})
			end
		end
	end

	-- Update the base entry with the max stage
	entries[1].maxStage = lineMaxStage
	return randomizer.list(entries)
end


-------- Arg Utils and Small Util/getter like functions ----------------


-- Like pool_utils.sourceCards but includes trainer proxies
function evo_line_randomize_utils.sourceCards(context, source)
	if source == "CURRENT" then
		return context.modified:getRandomizableMonsterCardsWithProxies()
	end
	return context.original:getRandomizableMonsterCardsWithProxies()
end

function evo_line_randomize_utils.isColorlessBasic(card)
	return card.type == evo_line_randomize_utils.cardType.MONSTER_COLORLESS
		and card.stage:getValue() == evo_line_randomize_utils.evolutionStage.BASIC:getValue()
end

-- Used for fixing proxies to make sure they are basics
-- Gets all the cards that are not proxies that have a prev evo
function evo_line_randomize_utils.nonBasicNonProxyByPrevEvo(toModifyCards)
	return toModifyCards
		:filter(function(card)
			return not card.isTrainerProxy
				and card.stage:getValue() ~= evo_line_randomize_utils.evolutionStage.BASIC:getValue()
				and not card.prevEvoName:isEmpty()
		end)
		:groupBy("prevEvoName:toString")
end

-- Filter for finding matching evo lines after they are expanded in the pool
function evo_line_randomize_utils.evoLinesMatch(a, b)
	return (a.BASIC or 1) == (b.BASIC or 1) and (a.STAGE_1 or 0) == (b.STAGE_1 or 0)
			and (a.STAGE_2 or 0) == (b.STAGE_2 or 0)
end

-- Returns the max stage from the counts of the evo line
function evo_line_randomize_utils.lineMaxStageFromCounts(evoLine)
	if evoLine.STAGE_2 and evoLine.STAGE_2 > 0 then
		return evo_line_randomize_utils.evolutionStage.STAGE_2
	end
	if evoLine.STAGE_1 and evoLine.STAGE_1 > 0 then
		return evo_line_randomize_utils.evolutionStage.STAGE_1
	end
	return evo_line_randomize_utils.evolutionStage.BASIC
end

-- Constructs the pool key for an entry based on the args. They key is
-- variable and will reflect the args to effectively create separate
-- subpools for any permutation of the args
function evo_line_randomize_utils.poolKey(cardTypeArg, stage, maxStage, grouping, withinType)
	if grouping == "ALL_TOGETHER" then
		if not withinType then
			-- No key - eveything is in one pool. Just return an arbitrary
			-- key so we can treat all cases as keyed for simplicity
			return evo_line_randomize_utils.ALL_TOGETHER_POOL_KEY
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

function evo_line_randomize_utils.poolCount(namePools, key)
	local pool = namePools:get(key)
	return pool == nil and 0 or pool:size()
end

-- Preferred pool key first, then other maxStage buckets for the same stage when fallback is on.
function evo_line_randomize_utils.poolKeysForDraw(cardType, stage, maxStage, grouping, withinType,
		allowStageFallback)
	-- Start with the full key for the passed args
	local keys = { evo_line_randomize_utils.poolKey(cardType, stage, maxStage, grouping, withinType) }
	-- This only makes sense if we are grouping by stage and max stage
	if allowStageFallback and grouping == "BY_STAGE_AND_MAX_STAGE" then
		-- From stage+1 maxStage to the end - skip any less than or equal to achieve this easily
		for _, otherMaxStage in ipairs(evo_line_randomize_utils.EVO_STAGES) do
			if otherMaxStage:getValue() > stage:getValue() and otherMaxStage ~= maxStage then
				table.insert(keys, evo_line_randomize_utils.poolKey(cardType, stage, otherMaxStage,
						grouping, withinType))
			end
		end
	end
	return keys
end

-- Names left for one type at this stage. With BY_STAGE_AND_MAX_STAGE, covers every
-- valid maxStage bucket for the stage (impossible maxStage < stage buckets are skipped).
function evo_line_randomize_utils.stagePoolCountForType(namePools, sourceType, stage, grouping,
		withinType)
	-- Prefer this stage as maxStage. With fallback that covers every valid bucket for it
	local keys = evo_line_randomize_utils.poolKeysForDraw(sourceType, stage, stage, grouping,
			withinType, true)
	local total = 0
	for _, key in ipairs(keys) do
		total = total + evo_line_randomize_utils.poolCount(namePools, key)
	end
	return total
end

-- Names left for one stage across types/max stage buckets when grouping splits that way.
-- When not withinType, pools are not keyed by type so count once with a nil type.
function evo_line_randomize_utils.stageTotalPoolCount(namePools, stage, grouping, withinType, sourceTypes)
	if not withinType then
		return evo_line_randomize_utils.stagePoolCountForType(namePools, nil, stage, grouping,
				withinType)
	end

	local total = 0
	sourceTypes:each(function(sourceType)
		total = total + evo_line_randomize_utils.stagePoolCountForType(namePools, sourceType, stage,
				grouping, withinType)
	end)
	return total
end

-- First key among keys that still has an unreserved name, or nil
function evo_line_randomize_utils.firstKeyWithCapacity(namePools, keys, reservedCounts, tempReserved)
	for _, key in ipairs(keys) do
		local alreadyReserved = (reservedCounts[key] or 0) + ((tempReserved and tempReserved[key]) or 0)
		if evo_line_randomize_utils.poolCount(namePools, key) - alreadyReserved >= 1 then
			return key
		end
	end
	return nil
end

-- Collects the entries that form a linear, non-branching line from the entry
-- at the passed base index. Returns the entries of all cards in that line
-- (including the base) and the indexes of entries that evolve from the last
-- entry (if any)
function evo_line_randomize_utils.collectLinearEvos(entries, baseIndex)
	local slots = { entries:get(baseIndex) }
	local currentIndex = baseIndex
	local evoIndexes = randomizer.list({})
	while true do
		-- Get all entries that evolve from the current index
		evoIndexes = entries:map(function(entry, idx)
			if entry.prevEvoIdx == currentIndex then
				return idx
			end
		end)
		-- If there is not one, (i.e. none because we reached the end or more than 1
		-- because we hit a branch)
		if evoIndexes:size() ~= 1 then
			break
		end
		-- Otherwise add the entry to the slots and keep going
		currentIndex = evoIndexes:get(1)
		table.insert(slots, entries:get(currentIndex))
	end
	return slots, evoIndexes
end


---------------------------- Randomization Helper functions -------------------------------


-- Try to reserve a type for all slots. Uses a temp reserved map so slots that
-- share a pool key still need one name each. With allowStageFallback, BASIC/STAGE_1
-- slots may reserve against other maxStage buckets for the same stage.
function evo_line_randomize_utils.tryReserveTypeForSlots(slots, cardType, namePools, grouping,
		reservedCounts, allowStageFallback)
	local tempReserved = {}
	for _, entry in ipairs(slots) do
		local keys = evo_line_randomize_utils.poolKeysForDraw(cardType, entry.stage, entry.maxStage,
				grouping, true, allowStageFallback)
		local key = evo_line_randomize_utils.firstKeyWithCapacity(namePools, keys, reservedCounts, tempReserved)
		if key == nil then
			return false
		end
		tempReserved[key] = (tempReserved[key] or 0) + 1
	end

	for key, count in pairs(tempReserved) do
		reservedCounts[key] = (reservedCounts[key] or 0) + count
	end
	for _, entry in ipairs(slots) do
		entry.type = cardType
	end
	return true
end

-- Do the pool(s) have the needed counts for the untyped entries? Used to determine
-- if its valid or if we need to throw it away and try something else.
-- With allowStageFallback, BASIC/STAGE_1 needs share capacity across all maxStage
-- buckets for that stage (STAGE_2 stays exact key only).
function evo_line_randomize_utils.canFillEntries(entries, namePools, grouping, allowStageFallback)
	local neededByBucket = {}
	local availableByBucket = {}

	entries:each(function(entry)
		local keys = evo_line_randomize_utils.poolKeysForDraw(entry.type, entry.stage,
				entry.maxStage, grouping, false, allowStageFallback)
		local bucket = keys[1]
		if #keys > 1 then
			-- Fallback eligible: use one shared bucket per stage
			bucket = "stage:" .. tostring(utils.asTableKey(entry.stage))
		end
		neededByBucket[bucket] = (neededByBucket[bucket] or 0) + 1
		if availableByBucket[bucket] == nil then
			local available = 0
			for _, key in ipairs(keys) do
				available = available + evo_line_randomize_utils.poolCount(namePools, key)
			end
			availableByBucket[bucket] = available
		end
	end)

	for bucket, needed in pairs(neededByBucket) do
		if availableByBucket[bucket] < needed then
			return false
		end
	end
	return true
end

-- draw a name from the pool for the given entry based on the args
function evo_line_randomize_utils.drawNameFromPool(namePools, grouping, withinType, entry,
		allowStageFallback)
	local keys = evo_line_randomize_utils.poolKeysForDraw(entry.type, entry.stage, entry.maxStage,
			grouping, withinType, allowStageFallback)
	-- Keys are expected in priority order
	for _, key in ipairs(keys) do
		local pool = namePools:get(key)
		if pool ~= nil and not pool:isEmpty() then
			return utils.consumeRandomElement(pool.items), key
		end
	end

	error("name pool exhausted while assigning a line slot")
end

-- Prefer prevType half the time, then try other source types, then prevType last if skipped
function evo_line_randomize_utils.pickAndAssignTypeToSlots(slots, prevType, namePools, grouping,
		reservedCounts, sourceTypes, allowStageFallback)
	local checkedPrev = false
	if prevType ~= nil and math.random(1, 2) == 1 then
		if evo_line_randomize_utils.tryReserveTypeForSlots(slots, prevType, namePools, grouping,
				reservedCounts, allowStageFallback)
		then
			return true
		end
		checkedPrev = true
	end

	-- try other source types first, leave prevType for the end if we skipped it
	local potentialTypes = randomizer.list(sourceTypes)
	if prevType ~= nil then
		potentialTypes:removeFirstMatch(prevType)
	end

	while not potentialTypes:isEmpty() do
		local tryType = utils.consumeRandomElement(potentialTypes.items)
		if evo_line_randomize_utils.tryReserveTypeForSlots(slots, tryType, namePools, grouping,
				reservedCounts, allowStageFallback)
		then
			return true
		end
	end

	-- If we tried all the other types and did not yet try the same type, try it
	if not checkedPrev and prevType ~= nil
		and evo_line_randomize_utils.tryReserveTypeForSlots(slots, prevType, namePools, grouping,
				reservedCounts, allowStageFallback)
	then
		return true
	end
	return false
end

-- Assigns types to the entry at the base index and all cards that evolve from it. If there is
-- a branch, it will recursively handle it
function evo_line_randomize_utils.assignTypesFromEntry(entries, baseIndex, prevType, namePools,
		grouping, reservedCounts, sourceTypes, allowStageFallback)
	-- Gather the "linear line" the part without any branches from the base card to before the first
	-- branch (or end of the line)
	local slots, branchIndexes = evo_line_randomize_utils.collectLinearEvos(entries, baseIndex)
	-- Try and assign a type for this whole segment
	if not evo_line_randomize_utils.pickAndAssignTypeToSlots(slots, prevType, namePools, grouping,
			reservedCounts, sourceTypes, allowStageFallback)
	then
		return false
	end

	-- Continue recursively for each evo of the last slot (empty when the line ends)
	-- Type of the first entry is the previous type for higher weighting
	local segmentType = slots[1].type
	local failed = false
	branchIndexes:shuffle():each(function(branchIndex)
		-- If any failed, early abort
		if failed then
			return
		end
		-- Assign this evo branch recursively
		if not evo_line_randomize_utils.assignTypesFromEntry(entries, branchIndex, segmentType,
				namePools, grouping, reservedCounts, sourceTypes, allowStageFallback)
		then
			failed = true
		end
	end)
	return not failed
end

-- Attempt to assign types to each entry in this line. If there are branches, they may
-- not all be the same type or not but its biased towards same type
function evo_line_randomize_utils.assignTypesToLine(entries, namePools, grouping, sourceTypes,
		allowStageFallback)
	local reservedCounts = {}
	return evo_line_randomize_utils.assignTypesFromEntry(entries, 1, nil, namePools, grouping,
			reservedCounts, sourceTypes, allowStageFallback)
end

function evo_line_randomize_utils.applyStageToCards(cardsOfName, stage)
	cardsOfName:each(function(card)
		card.stage = stage
	end)
end

function evo_line_randomize_utils.applyPrevEvoToCards(cardsOfName, prevEvo)
	cardsOfName:each(function(card)
		card.prevEvoName:setText(prevEvo)
	end)
end

-- Errors if a name cannot be drawn from the pool.
function evo_line_randomize_utils.assignEvoLineList(evoLineId, entries, namePools, grouping,
		withinType, toModifyByName, allowStageFallback)
	local branched = evo_line_randomize_utils.lineEntriesHasBranch(entries)
	if branched and evoLineId ~= nil then
		logger.info("evo_line_cards filling lineId=" .. tostring(evoLineId) .. " "
				.. evo_line_randomize_utils.formatLineEntries(entries))
	end

	local idxToName = {}
	entries:each(function(entry, idx)
		local drawnName = evo_line_randomize_utils.drawNameFromPool(namePools, grouping,
				withinType, entry, allowStageFallback)
		idxToName[idx] = drawnName
		evo_line_randomize_utils.applyStageToCards(toModifyByName:get(drawnName), entry.stage)
		if branched and evoLineId ~= nil then
			local prevLabel = "(root)"
			if entry.prevEvoIdx ~= nil then
				prevLabel = tostring(idxToName[entry.prevEvoIdx] or entry.prevEvoIdx)
			end
			logger.info("evo_line_cards lineId=" .. tostring(evoLineId)
					.. " assign #" .. idx .. " name=" .. drawnName
					.. " stage=" .. tostring(entry.stage) .. " prev=" .. prevLabel)
		end
	end)

	entries:each(function(entry, idx)
		local cardName = idxToName[idx]
		local prevEvo = ""
		if entry.prevEvoIdx ~= nil then
			prevEvo = idxToName[entry.prevEvoIdx] or ""
		end
		evo_line_randomize_utils.applyPrevEvoToCards(toModifyByName:get(cardName), prevEvo)
	end)
end

function evo_line_randomize_utils.assignEvoLineData(evoLineData, namePools, grouping, withinType, toModifyByName)
	evoLineData:each(function(line)
		evo_line_randomize_utils.assignEvoLineList(line.evoLineId, line.entries, namePools,
				grouping, withinType, toModifyByName, false)
	end)
end

-- Pick types first, then assign like evo_line_cards
function evo_line_randomize_utils.tryAssignDrawnLine(evoLine, namePools, grouping, withinType,
		sourceTypes, toModifyByName)
	-- Convert the evo line to the entries data
	local entries = evo_line_randomize_utils.buildEntriesFromEvoLine(evoLine)
	-- Custom reshapes lines, so when maxStage buckets matter allow drawing BASIC/STAGE_1
	-- names from other maxStage pools and rewrite maxStage on assign
	local allowStageFallback = grouping == "BY_STAGE_AND_MAX_STAGE"

	-- If we are within type, assign types to the entries while ensuring there are cards of that type for them
	if withinType then
		if not evo_line_randomize_utils.assignTypesToLine(entries, namePools, grouping, sourceTypes,
				allowStageFallback)
		then
			return false
		end
	-- Otherwise, just ensure there is cards for the entries
	elseif not evo_line_randomize_utils.canFillEntries(entries, namePools, grouping, allowStageFallback) then
		return false
	end

	-- Now that we have our shape and know it should fit, assign cards to the entries using the common
	-- code and approach
	evo_line_randomize_utils.assignEvoLineList(nil, entries, namePools, grouping, withinType,
			toModifyByName, allowStageFallback)
	return true
end


----------------- Data cleanup/consistency functions ------------------------


-- Used for fixing proxies to make sure they are basics.
-- Excludes trainer proxies and any basic that a proxy currently evolves from,
-- so swapping into the proxy's slot cannot create a self reference
function evo_line_randomize_utils.potentialNonProxySwapTargets(toModifyCards, colorlessOnly)
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
			if card.stage:getValue() ~= evo_line_randomize_utils.evolutionStage.BASIC:getValue() then
				return false
			end
			if proxyPrevNames[card.name:toString()] then
				return false
			end
			if colorlessOnly and not evo_line_randomize_utils.isColorlessBasic(card) then
				return false
			end
			return true
		end)
		:select("name:toString")
		:removeDuplicates()
end

-- ALL_TOGETHER only. Proxies must stay basic. Search by name, not evo line groups.
function evo_line_randomize_utils.fixAllTogetherProxies(toModifyCards, args, toModifyByName)
	if args.grouping ~= "ALL_TOGETHER" then
		return
	end

	-- Step 0: construct maps of data for convinience
	local potentialSwapTargets = evo_line_randomize_utils.potentialNonProxySwapTargets(toModifyCards, args.withinType)
	local evosByPrevEvo = evo_line_randomize_utils.nonBasicNonProxyByPrevEvo(toModifyCards)

	toModifyCards:each(function(proxy)
		-- If its not a proxy or the proxy is already a basic, we are good
		if not proxy.isTrainerProxy then
			return
		end
		if proxy.stage:getValue() == evo_line_randomize_utils.evolutionStage.BASIC:getValue() then
			return
		end

		-- Step 1: random basic that is not a proxy and not a proxy's current prev evo
		local swapName = utils.consumeRandomElement(utils.deepCopy(potentialSwapTargets.items))

		-- Step 2: swap evo data between the selected card and the proxy
		local proxyName = proxy.name:toString()
		local proxyStage = proxy.stage
		local proxyPrev = proxy.prevEvoName:toString()
		evo_line_randomize_utils.applyStageToCards(toModifyByName:get(proxyName),
				evo_line_randomize_utils.evolutionStage.BASIC)
		evo_line_randomize_utils.applyPrevEvoToCards(toModifyByName:get(proxyName), "")
		evo_line_randomize_utils.applyStageToCards(toModifyByName:get(swapName), proxyStage)
		evo_line_randomize_utils.applyPrevEvoToCards(toModifyByName:get(swapName), proxyPrev)

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
function evo_line_randomize_utils.syncEvoAiData(context, cards)
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

function evo_line_randomize_utils.finalize(context, args, targets, toModifyByName)
	evo_line_randomize_utils.fixAllTogetherProxies(targets, args, toModifyByName)
	evo_line_randomize_utils.syncEvoAiData(context, targets)
end


-------------------- Debugging Functions ----------------------------


-- For debug logging
function evo_line_randomize_utils.lineEntriesHasBranch(entries)
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
function evo_line_randomize_utils.formatLineEntries(entries)
	local stageCounts = {}
	for _, stage in ipairs(evo_line_randomize_utils.EVO_STAGES) do
		stageCounts[stage:getValue()] = 0
	end
	entries:each(function(entry)
		local stageValue = entry.stage:getValue()
		stageCounts[stageValue] = stageCounts[stageValue] + 1
	end)
	local evolutionStage = evo_line_randomize_utils.evolutionStage
	local basic = stageCounts[evolutionStage.BASIC:getValue()]
	local stage1 = stageCounts[evolutionStage.STAGE_1:getValue()]
	local stage2 = stageCounts[evolutionStage.STAGE_2:getValue()]
	return "evoLine = [" .. basic .. ", " .. stage1 .. ", " .. stage2 .. "]"
end

return evo_line_randomize_utils
