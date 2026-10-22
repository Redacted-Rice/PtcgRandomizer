-- Randomizes evo lines from extracted line/branch data: shuffle name pools, apply stage/prevEvo, fix proxies.
local common_field_defs = require("modules.util.common_field_defs")
local pool_utils = require("modules.util.pool_utils")
local randomizer = require("randomizer")
local utils = require("randomizer.utils")
local logger = require("randomizer.logger")

local evo_line_randomize_utils = {
	ALL_TOGETHER_POOL_KEY = 0,
}

function evo_line_randomize_utils.init(context)
	pool_utils.init(context)
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
function evo_line_randomize_utils.buildNamePools(sourceCards, keyContext)
	return randomizer.groupFromField(sourceCards,
			function(card)
				return evo_line_randomize_utils.poolKey(
						pool_utils.getTypeGroupingKey(card, keyContext.withinType),
						card.stage, card.evoLineMaxStage, keyContext)
			end, "name:toString"):applyToEachList("removeDuplicates")
end

-- Creates an array of each cards' data (stage, maxStage, prevEvoIdx, typeGroupKey) in the line
function evo_line_randomize_utils.extractLineData(sourceLine, keyContext)
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

		entries[card.stage:getName()] = (entries[card.stage:getName()] or 0) + 1
		table.insert(entries, {
			typeGroupKey = pool_utils.getTypeGroupingKey(card, keyContext.withinType),
			stage = card.stage,
			maxStage = card.evoLineMaxStage,
			prevEvoIdx = prevEvoIdx,
		})
		-- Store the index by name for quick lookup of prev evo
		nameToIdx[name] = #entries
	end)

	return entries
end

-- Extract the evo line data from the cards
function evo_line_randomize_utils.extractAllEvoLineData(sourceCards, keyContext)
	-- For each evo line, map it to the evo line data
	return randomizer.groupBy(sourceCards, "evoLineId"):mapToList(function(evoLineId, sourceLine)
		return {
			-- We don't need original evo line id but keep it for debugging
			evoLineId = evoLineId,
			entries = evo_line_randomize_utils.extractLineData(sourceLine, keyContext),
		}
	end)
end

function evo_line_randomize_utils.buildEntriesFromEvoLine(evoLine)
	local evolutionStage = evo_line_randomize_utils.evolutionStage
	local stage1Count = evoLine.STAGE_1 or 0
	local stage2Count = evoLine.STAGE_2 or 0
	local lineMaxStage = evolutionStage.BASIC
	local entries = {
		[evolutionStage.BASIC:getName()] = 1,
		[evolutionStage.STAGE_1:getName()] = stage1Count,
		[evolutionStage.STAGE_2:getName()] = stage2Count
	}

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
	return entries
end


-------- Arg Utils and Small Util/getter like functions ----------------

-- Standardize input across module usage to the correct withinTYpe args
function evo_line_randomize_utils.namePoolTypeScope(args, choicePools)
	if choicePools == nil then
		return common_field_defs.TYPE_POOL_ALL_TOGETHER
	end
	return args.withinType or common_field_defs.TYPE_POOL_WITHIN_TYPE
end

-- Build the static key context info for our randomization that will help determine how we
-- use and construct keys and type/subtype values. This is primarily for convinience to not
-- have to pass a bunc of separate args
function evo_line_randomize_utils.buildNamePoolKeyContext(sourceCards, grouping, withinType,
		allowStageFallback)
	if allowStageFallback == nil then
		allowStageFallback = grouping == "BY_STAGE_AND_MAX_STAGE"
	end
	local keyContext = {
		grouping = grouping,
		withinType = withinType,
		splitsIntoPools = pool_utils.typeGroupingSplitsIntoPools(withinType),
		allowStageFallback = allowStageFallback,
		cardTypes = nil,
		colorlessTypes = nil,
	}
	-- If we are splitting into pools, we need some more type data
	if keyContext.splitsIntoPools then
		keyContext.cardTypes = randomizer.list(sourceCards):select(function(card)
			return pool_utils.getTypeGroupingKey(card, withinType)
		end):removeDuplicates()

		keyContext.colorlessTypes = randomizer.list(sourceCards):filter(function(card)
			return card.type == evo_line_randomize_utils.cardType.MONSTER_COLORLESS
		end):select(function(card)
			return pool_utils.getTypeGroupingKey(card, withinType)
		end):removeDuplicates()
	end
	return keyContext
end

-- Like pool_utils.sourceCards but can include trainer proxies
function evo_line_randomize_utils.sourceCards(context, source, excludeTrainerProxies)
	local deck = source == "CURRENT" and context.modified or context.original
	if excludeTrainerProxies then
		return deck:getRandomizableMonsterCards()
	end
	return deck:getRandomizableMonsterCardsWithProxies()
end

-- Can include or not include proxies based on args
function evo_line_randomize_utils.targetCards(context, excludeTrainerProxies)
	if excludeTrainerProxies then
		return context.modified:getRandomizableMonsterCards()
	end
	return context.modified:getRandomizableMonsterCardsWithProxies()
end

function evo_line_randomize_utils.isColorlessBasic(card)
	return card.type == evo_line_randomize_utils.cardType.MONSTER_COLORLESS
		and card.stage:getValue() == evo_line_randomize_utils.evolutionStage.BASIC:getValue()
end

--- Used for fixing proxies to make sure they are basics
--- Gets all the cards that are not proxies that have a prev evo
function evo_line_randomize_utils.nonBasicNonProxyByPrevEvo(toModifyCards)
	return toModifyCards:filter(function(card)
			return not card.isTrainerProxy
					and card.stage:getValue() ~= evo_line_randomize_utils.evolutionStage.BASIC
							:getValue()
					and not card.prevEvoName:isEmpty()
		end):groupBy("prevEvoName:toString")
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
function evo_line_randomize_utils.poolKey(cardTypeArg, stage, maxStage, keyContext)
	if keyContext.grouping == "ALL_TOGETHER" then
		if not keyContext.splitsIntoPools then
			-- No key - eveything is in one pool. Just return an arbitrary
			-- key so we can treat all cases as keyed for simplicity
			return evo_line_randomize_utils.ALL_TOGETHER_POOL_KEY
		end
		-- Key is just type
		return cardTypeArg
	end
	if keyContext.grouping == "BY_STAGE" then
		if keyContext.splitsIntoPools then
			-- key is a combo of type and stage
			return pool_utils.combineKeys(cardTypeArg, stage)
		end
		-- Key is just the stage
		return utils.asTableKey(stage)
	end
	if keyContext.splitsIntoPools then
		-- key is a combo of type, maxStage, and stage
		return pool_utils.combineKeys(cardTypeArg,
				pool_utils.stageMaxStageKeyFromValues(maxStage, stage))
	end
	-- key is just the maxStage and stage
	return pool_utils.stageMaxStageKeyFromValues(maxStage, stage)
end

function evo_line_randomize_utils.poolCount(namePools, key)
	local pool = namePools:get(key)
	return pool == nil and 0 or pool:size()
end

-- Preferred pool key first, then other maxStage buckets for the same stage when fallback is on.
function evo_line_randomize_utils.poolKeysForDraw(cardType, stage, maxStage, keyContext)
	-- Start with the full key for the passed args - always prefer the asked key
	local keys = { evo_line_randomize_utils.poolKey(cardType, stage, maxStage, keyContext) }
	-- This only makes sense if we are grouping by stage and max stage
	if keyContext.allowStageFallback and keyContext.grouping == "BY_STAGE_AND_MAX_STAGE" then
		-- Add any remaining valid bucket for this stage (maxStage >= stage) preferring higher max evo
		-- stages first by reverse iterating
		local evoStages = evo_line_randomize_utils.EVO_STAGES
		for stageIndex = #evoStages, 1, -1 do
			local otherMaxStage = evoStages[stageIndex]
			-- We already add the desired max stage first
			if otherMaxStage:getValue() >= stage:getValue() and otherMaxStage ~= maxStage then
				table.insert(keys, evo_line_randomize_utils.poolKey(cardType, stage, otherMaxStage,
						keyContext))
			end
		end
	end
	return keys
end

-- Names left for one type at this stage. With BY_STAGE_AND_MAX_STAGE, covers every
-- valid maxStage bucket for the stage (impossible maxStage < stage buckets are skipped).
function evo_line_randomize_utils.stagePoolCountForType(namePools, sourceType, stage, keyContext)
	-- Prefer this stage as maxStage. With fallback that covers every valid bucket for it
	local keys = evo_line_randomize_utils.poolKeysForDraw(sourceType, stage, stage, keyContext)
	local total = 0
	for _, key in ipairs(keys) do
		total = total + evo_line_randomize_utils.poolCount(namePools, key)
	end
	return total
end

-- Names left for one stage across types/max stage buckets when grouping splits that way.
-- When not withinType, pools are not keyed by type so count once with a nil type.
function evo_line_randomize_utils.stageTotalPoolCount(namePools, stage, keyContext)
	if not keyContext.splitsIntoPools then
		return evo_line_randomize_utils.stagePoolCountForType(namePools, nil, stage, keyContext)
	end

	local total = 0
	keyContext.cardTypes:each(function(cardType)
		total = total + evo_line_randomize_utils.stagePoolCountForType(namePools, cardType, stage,
				keyContext)
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
	local slots = { entries[baseIndex] }
	local currentIndex = baseIndex
	local evoIndexes = {}
	while true do
		evoIndexes = {}
		for index, entry in ipairs(entries) do
			if entry.prevEvoIdx == currentIndex then
				table.insert(evoIndexes, index)
			end
		end
		-- If there is not one, (i.e. none because we reached the end or more than 1
		-- because we hit a branch)
		if #evoIndexes ~= 1 then
			break
		end
		-- Otherwise add the entry to the slots and keep going
		currentIndex = evoIndexes[1]
		table.insert(slots, entries[currentIndex])
	end
	return slots, evoIndexes
end


---------------------------- Randomization Helper functions -------------------------------


-- Try to reserve a type for all slots. Uses a temp reserved map so slots that
-- share a pool key still need one name each. With allowStageFallback, BASIC/STAGE_1
-- slots may reserve against other maxStage buckets for the same stage.
function evo_line_randomize_utils.tryReserveTypeForSlots(slots, cardType, namePools, keyContext,
		reservedCounts)
	if cardType == nil then
		return false
	end
	local tempReserved = {}
	for _, entry in ipairs(slots) do
		local keys = evo_line_randomize_utils.poolKeysForDraw(cardType, entry.stage, entry.maxStage,
				keyContext)
		local key = evo_line_randomize_utils.firstKeyWithCapacity(namePools, keys, reservedCounts, tempReserved)
		if key == nil then
			logger.debug("evo_line_custom type reserve failed cardType=" .. tostring(cardType)
					.. " stage=" .. tostring(entry.stage) .. " maxStage=" .. tostring(entry.maxStage)
					.. " keys=" .. table.concat(keys, ","))
			return false
		end
		tempReserved[key] = (tempReserved[key] or 0) + 1
	end

	for key, count in pairs(tempReserved) do
		reservedCounts[key] = (reservedCounts[key] or 0) + count
	end
	for _, entry in ipairs(slots) do
		entry.typeGroupKey = cardType
	end
	return true
end

-- Do the pool(s) have the needed counts for the untyped entries? Used to determine
-- if its valid or if we need to throw it away and try something else.
-- With allowStageFallback, BASIC/STAGE_1 needs share capacity across all maxStage
-- buckets for that stage (STAGE_2 stays exact key only).
function evo_line_randomize_utils.canFillEntries(entries, namePools, keyContext)
	local neededByBucket = {}
	local availableByBucket = {}

	for _, entry in ipairs(entries) do
		local keys = evo_line_randomize_utils.poolKeysForDraw(nil, entry.stage,
				entry.maxStage, keyContext)
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
	end

	for bucket, needed in pairs(neededByBucket) do
		if availableByBucket[bucket] < needed then
			return false
		end
	end
	return true
end

-- draw a name from the pool for the given entry based on the args
function evo_line_randomize_utils.drawNameFromPool(namePools, keyContext, entry)
	local keys = evo_line_randomize_utils.poolKeysForDraw(entry.typeGroupKey, entry.stage, entry.maxStage,
			keyContext)
	-- Keys are expected in priority order
	for _, key in ipairs(keys) do
		local pool = namePools:get(key)
		if pool ~= nil and not pool:isEmpty() then
			return utils.consumeRandomElement(pool.items), key
		end
	end

	error("name pool exhausted while assigning a line slot")
end

-- We incrementally draw from the pools so we need to maintain the state externally so we can
-- handle it right and return, remove, and replentish the pool as needed
-- This is needed for evo lines and types as we don't know what will work in advanced so we just
-- fill it as we go as thats much easier than trying to plan and figure it out all in advance.
-- TODO Later: Add support in URC for pools that do this for us
function evo_line_randomize_utils.createPoolDrawState(pool, approach)
	return {
		pool = pool,
		consumable = approach == "MINIMIZE_REPEATS",
		used = randomizer.list({}),
	}
end

-- allowExhausted: return nil instead of error when the pool cannot draw
function evo_line_randomize_utils.drawFromPoolState(poolState, allowExhausted)
	if poolState.pool:isEmpty() then
		if poolState.consumable and not poolState.used:isEmpty() then
			poolState.pool = poolState.used
			poolState.used = randomizer.list({})
		elseif allowExhausted then
			return nil
		else
			error("Pool state items unexpectedly exhausted!")
		end
	end

	local choice = utils.consumeRandomElement(poolState.pool.items)
	if not poolState.consumable then
		table.insert(poolState.pool.items, choice)
	else
		table.insert(poolState.used.items, choice)
	end
	return choice
end

function evo_line_randomize_utils.remapWeightsTableToList(choiceKeys, weightTable)
	local entries = {}
	for _, choice in ipairs(choiceKeys) do
		local weight = weightTable[choice]
		if weight ~= nil and weight > 0 then
			table.insert(entries, { choice = choice, weight = weight })
		end
	end
	return randomizer.List.backedBy(entries)
end

function evo_line_randomize_utils.buildTypePolicyPools(branchingBasesColorless, branchRootConsistency,
		interBranchTypes, approach)
	local choicePools = {
		branchingBasesColorless = evo_line_randomize_utils.remapWeightsTableToList(
				common_field_defs.ARG_DEF_EVO_LINE_BRANCHING_BASES_COLORLESS.definition.fixedKeys,
				branchingBasesColorless),
		branchRootConsistency = evo_line_randomize_utils.remapWeightsTableToList(
				common_field_defs.ARG_DEF_EVO_LINE_BRANCH_ROOT_CONSISTENCY.definition.fixedKeys,
				branchRootConsistency),
		interBranchTypes = evo_line_randomize_utils.remapWeightsTableToList(
				common_field_defs.ARG_DEF_EVO_LINE_INTER_BRANCH_TYPES.definition.fixedKeys,
				interBranchTypes),
	}
	for key, weightList in pairs(choicePools) do
		if weightList:isEmpty() then
			error(key .. " must include at least one entry with a positive weight")
		end
		local expanded = weightList:flatMapNTimes("weight", function(entry)
			return entry.choice
		end)
		choicePools[key] = evo_line_randomize_utils.createPoolDrawState(expanded, approach)
	end
	return choicePools
end

-- Build preference groups for the potential energy types to prepare for selecting a type
function evo_line_randomize_utils.buildTypePreferenceGroups(cardTypes, branchContext, choicePools,
		triedTypes)
	-- Make an easy lookup table of all types to check
	local uncheckedTypes = {}
	cardTypes:each(function(cardType)
		uncheckedTypes[cardType] = true
	end)

	-- Remove any already tried types
	for _, cardType in ipairs(triedTypes) do
		uncheckedTypes[cardType] = nil
	end

	local preferred = {}
	local avoided = {}

	-- If we have other branches selected already, pull inter branch policy and apply it here
	if #branchContext.branchTypes > 0 then
		local interBranchTypes = evo_line_randomize_utils.drawFromPoolState(choicePools.interBranchTypes)
		if interBranchTypes == common_field_defs.INTER_BRANCH_MATCH then
			for _, cardType in ipairs(branchContext.branchTypes) do
				table.insert(preferred, cardType)
				uncheckedTypes[cardType] = nil
			end
			if #preferred > 0 then
				local preferredLabels = {}
				for _, cardType in ipairs(preferred) do
					table.insert(preferredLabels, tostring(cardType))
				end
				logger.debug("evo_line_custom inter_branch_match preferred types="
						.. table.concat(preferredLabels, ","))
			end
		elseif interBranchTypes == common_field_defs.INTER_BRANCH_DIFFER then
			for _, cardType in ipairs(branchContext.branchTypes) do
				table.insert(avoided, cardType)
				uncheckedTypes[cardType] = nil
			end
		end
	end

	-- Check the match root value in the branch context and avoid it if it's set
	if branchContext.branchRootConsistency == common_field_defs.BRANCH_ROOT_AVOID then
		table.insert(avoided, branchContext.rootType)
		uncheckedTypes[branchContext.rootType] = nil
	end

	-- Reformat any remaing types into the neutral list
	local neutral = {}
	for cardType, _ in pairs(uncheckedTypes) do
		table.insert(neutral, cardType)
	end

	return { preferred, neutral, avoided }
end

function evo_line_randomize_utils.tryPickAndAssignTypeFromGroup(group, slots, namePools, keyContext,
		reservedCounts, exhaustedLabel, triedTypes)
	while #group > 0 do
		local cardType = utils.consumeRandomElement(group)
		if evo_line_randomize_utils.tryReserveTypeForSlots(slots, cardType, namePools, keyContext,
				reservedCounts)
		then
			return cardType
		end
		if triedTypes ~= nil then
			table.insert(triedTypes, cardType)
		end
	end
	if exhaustedLabel ~= nil then
		logger.debug("evo_line_custom " .. exhaustedLabel .. " slots=["
				.. evo_line_randomize_utils.formatSlots(slots) .. "] pools=["
				.. evo_line_randomize_utils.formatNamePoolCounts(namePools) .. "]")
	end
	return nil
end

function evo_line_randomize_utils.pickAndAssignTypeToSlots(slots, namePools, keyContext,
		reservedCounts, choicePools, isBranchNode, branchContext)
	local triedTypes = {}
	local chosenType = nil

	-- First see if we are a branch node and should be colorless and try that
	if isBranchNode and evo_line_randomize_utils.drawFromPoolState(choicePools.branchingBasesColorless)
			== common_field_defs.BRANCHING_BASE_COLORLESS
			and keyContext.colorlessTypes ~= nil then
		chosenType = evo_line_randomize_utils.tryPickAndAssignTypeFromGroup(
				keyContext.colorlessTypes:toTable(), slots, namePools, keyContext,
				reservedCounts, nil, triedTypes)
	end

	-- If we did not choose a type and we are part of a branch, See if we should try and match the root
	if chosenType == nil and branchContext ~= nil
			and branchContext.branchRootConsistency == common_field_defs.BRANCH_ROOT_MATCH
			and not branchContext.matchRootFilled then
		chosenType = branchContext.rootType
		if not evo_line_randomize_utils.tryReserveTypeForSlots(slots, chosenType, namePools, keyContext,
					reservedCounts) then
			logger.debug("evo_line_custom branch_root_match could not match rootType="
					.. tostring(branchContext.rootType) .. " slots=["
					.. evo_line_randomize_utils.formatSlots(slots) .. "] pools=["
					.. evo_line_randomize_utils.formatNamePoolCounts(namePools) .. "]")
			table.insert(triedTypes, chosenType)
			chosenType = nil
		else
			branchContext.matchRootFilled = true
		end
	end

	-- If its not one of the high priority set to specific type cases, do more generic processing and
	-- selection to determine the type
	if chosenType == nil then
		if branchContext ~= nil then
			-- If its part of a branching line, build preference groups and use that for randomization
			for index, group in ipairs(evo_line_randomize_utils.buildTypePreferenceGroups(keyContext.cardTypes,
					branchContext, choicePools, triedTypes)) do
				local exhaustedLabel = nil
				if index == 1 and #branchContext.branchTypes > 0 and #group > 0 then
					exhaustedLabel = "inter_branch_match preferred exhausted"
				end
				chosenType = evo_line_randomize_utils.tryPickAndAssignTypeFromGroup(group, slots,
						namePools, keyContext, reservedCounts, exhaustedLabel)
				if chosenType ~= nil then
					break
				end
			end
		else
			-- otherwise just choose any type
			chosenType = evo_line_randomize_utils.tryPickAndAssignTypeFromGroup(keyContext.cardTypes:toTable(),
					slots, namePools, keyContext, reservedCounts)
		end
	end

	-- If we choose a type for a branch, add it to the context
	if chosenType ~= nil and branchContext ~= nil then
		table.insert(branchContext.branchTypes, chosenType)
		if branchContext.branchRootConsistency == common_field_defs.BRANCH_ROOT_AVOID then
			logger.debug("evo_line_custom branch_root_avoid assigned type=" .. tostring(chosenType)
					.. " avoidRootType=" .. tostring(branchContext.rootType) .. " slots=["
					.. evo_line_randomize_utils.formatSlots(slots) .. "]")
		end
	end
	return chosenType
end

function evo_line_randomize_utils.formatEvoLineStagesCounts(evoLine)
	return "[" .. (evoLine.BASIC or 1) .. ", " .. (evoLine.STAGE_1 or 0) .. ", "
			.. (evoLine.STAGE_2 or 0) .. "]"
end

function evo_line_randomize_utils.formatEntriesStagesCounts(evoLine)
	return "[" .. (evoLine[evo_line_randomize_utils.evolutionStage.BASIC:getName()] or 1) .. ", "
			.. (evoLine[evo_line_randomize_utils.evolutionStage.STAGE_1:getName()] or 0) .. ", "
			.. (evoLine[evo_line_randomize_utils.evolutionStage.STAGE_2:getName()] or 0) .. "]"
end

-- Debug: remaining name pool sizes keyed by pool key.
function evo_line_randomize_utils.formatNamePoolCounts(namePools)
	local parts = {}
	namePools:each(function(key, pool)
		table.insert(parts, tostring(key) .. "=" .. pool:size())
	end)
	return table.concat(parts, ", ")
end

function evo_line_randomize_utils.formatSlots(slots)
	local parts = {}
	for _, entry in ipairs(slots) do
		local typeLabel = entry.typeGroupKey ~= nil and tostring(entry.typeGroupKey) or "?"
		table.insert(parts, typeLabel .. "/" .. tostring(entry.stage)
				.. "(max=" .. tostring(entry.maxStage) .. ")")
	end
	return table.concat(parts, " -> ")
end

-- Assigns types along one evo line (including branches). branchContext is nil on the first call.
function evo_line_randomize_utils.assignTypesFromEntry(entries, baseIndex, namePools, keyContext,
		reservedCounts, choicePools, branchContext)
	-- Get this whole segment and any branches at the end of it
	local slots, branchIndexes = evo_line_randomize_utils.collectLinearEvos(entries, baseIndex)
	-- If we have branchIndexes, this line ends in a branch node and will use special type handling potentially
	-- (note this could be > 1 instead because if it has any branches, it will have at least 2 otherwise it
	-- would have been part of this segment)
	local isBranchNode = #branchIndexes > 0

	-- Pick a type for this segment. This handle any forced types or preferences, ensuring we have cards for it,
	-- and setting the types
	local chosenType = evo_line_randomize_utils.pickAndAssignTypeToSlots(slots, namePools, keyContext,
			reservedCounts, choicePools, isBranchNode, branchContext)
	-- If we failed to pick a type, return false and then it will try a different evo line shape which may work
	if chosenType == nil then
		logger.debug("evo_line_custom type segment failed slots=["
				.. evo_line_randomize_utils.formatSlots(slots) .. "] branches=" .. #branchIndexes)
		return false
	end

	-- if its not a branch, we are done
	if not isBranchNode then
		return true
	end

	-- if it is a branch, create the new branch state data from this node
	local branchRootConsistency = evo_line_randomize_utils.drawFromPoolState(
			choicePools.branchRootConsistency)
	local newBranchContext = {
		rootType = chosenType,
		branchTypes = {},
		branchRootConsistency = branchRootConsistency,
		-- Only used for BRANCH_ROOT_MATCH and for that case it should be set to false
		matchRootFilled = branchRootConsistency ~= common_field_defs.BRANCH_ROOT_MATCH,
	}

	-- Recurse to all evos using this new branch context returning false if any fail (which will
	-- then cause it to try a different evo line shape instead)
	for _, branchIndex in ipairs(branchIndexes) do
		if not evo_line_randomize_utils.assignTypesFromEntry(entries, branchIndex, namePools,
				keyContext, reservedCounts, choicePools, newBranchContext)
		then
			return false
		end
	end
	return true
end

-- Attempt to assign types to each entry in this line. If there are branches, they may
-- not all be the same type or not but its biased towards same type.
function evo_line_randomize_utils.assignTypesToLine(entries, namePools, keyContext, choicePools)
	local reservedCounts = {}
	return evo_line_randomize_utils.assignTypesFromEntry(entries, 1, namePools, keyContext,
			reservedCounts, choicePools, nil)
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

function evo_line_randomize_utils.addEvoNamesToLine(name, toModifyByPrevEvo, list)
	local evos = toModifyByPrevEvo:get(name)
	if evos == nil then
		return
	end
	-- Evos can have multiple versions of the cards so group them for convinience
	evos:groupBy("name:toString"):each(function(_, evoGroup)
		table.insert(list, evoGroup)
	end)
end

-- Walk evos by depth under rootName. Level 1 is forced to BASIC then any of its evos are updated appropriately
function evo_line_randomize_utils.demoteEvosOf(rootName, cardsByPrev, logPrefix,
		logSuffix)
	local evolutionStage = evo_line_randomize_utils.evolutionStage
	local stageForLevel = evolutionStage.BASIC
	local currentLevel = {}
	evo_line_randomize_utils.addEvoNamesToLine(rootName, cardsByPrev, currentLevel)

	-- If/while we are finding evos, keep going
	while #currentLevel > 0 do
		local nextLevel = {}
		for _, evoGroup in ipairs(currentLevel) do
			local evoName = evoGroup:get(1).name:toString()
			evo_line_randomize_utils.addEvoNamesToLine(evoName, cardsByPrev, nextLevel)
			-- Only apply if its actually lowering - this guards against the case where we have double branches and
			-- prevents one pass on the branch from overwritting the other without having to do any tracking or
			-- updating of references while modifying
			evoGroup:each(function(card)
				if card.stage:getValue() > stageForLevel:getValue() then
					card.stage = stageForLevel
				end
			end)
			-- If we are changing to a basic, we need to clear the prev evo
			if stageForLevel:getValue() == evolutionStage.BASIC:getValue() then
				evo_line_randomize_utils.applyPrevEvoToCards(evoGroup, "")
			end
			logger.debug(logPrefix .. " demoted " .. evoName .. logSuffix)
		end
		currentLevel = nextLevel
		local nextStage = stageForLevel:getNextStage()
		stageForLevel = nextStage or evolutionStage.STAGE_2
	end
end

-- Swap stage/prevEvo between two names and rewrite anyone pointing at either.
function evo_line_randomize_utils.swapEvoSlots(nameA, nameB, toModifyByName, cardsByPrev)
	local cardsA = toModifyByName:get(nameA)
	local cardsB = toModifyByName:get(nameB)
	local sampleA = cardsA:get(1)
	local sampleB = cardsB:get(1)
	local stageA = sampleA.stage
	local prevA = sampleA.prevEvoName:toString()
	local stageB = sampleB.stage
	local prevB = sampleB.prevEvoName:toString()

	-- Swap stage and prevEvo
	evo_line_randomize_utils.applyStageToCards(cardsA, stageB)
	evo_line_randomize_utils.applyPrevEvoToCards(cardsA, prevB)
	evo_line_randomize_utils.applyStageToCards(cardsB, stageA)
	evo_line_randomize_utils.applyPrevEvoToCards(cardsB, prevA)

	-- Swap any cards that evolve from them as well
	local evosA = cardsByPrev:get(nameA)
	local evosB = cardsByPrev:get(nameB)
	if evosA ~= nil then
		evosA:each(function(card)
			card.prevEvoName:setText(nameB)
		end)
	end
	if evosB ~= nil then
		evosB:each(function(card)
			card.prevEvoName:setText(nameA)
		end)
	end
end

-- Errors if a name cannot be drawn from the pool.
function evo_line_randomize_utils.assignEvoLineList(evoLineId, entries, namePools, keyContext,
		toModifyByName)
	local branched = evo_line_randomize_utils.lineEntriesHasBranch(entries)
	if branched and evoLineId ~= nil then
		logger.info("evo_line_cards filling lineId=" .. tostring(evoLineId) .. " "
				.. "evoLine = " .. evo_line_randomize_utils.formatEntriesStagesCounts(entries))
	end

	local idxToName = {}
	for idx, entry in ipairs(entries) do
		local drawnName = evo_line_randomize_utils.drawNameFromPool(namePools, keyContext, entry)
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
	end

	for idx, entry in ipairs(entries) do
		local cardName = idxToName[idx]
		local prevEvo = ""
		if entry.prevEvoIdx ~= nil then
			prevEvo = idxToName[entry.prevEvoIdx] or ""
		end
		evo_line_randomize_utils.applyPrevEvoToCards(toModifyByName:get(cardName), prevEvo)
	end
end

-- Pick types first, then assign like evo_line_cards
function evo_line_randomize_utils.tryAssignDrawnLine(evoLine, namePools, keyContext, toModifyByName,
		choicePools)
	-- Convert the evo line to the entries data
	local entries = evo_line_randomize_utils.buildEntriesFromEvoLine(evoLine)
	local shapeLabel = evo_line_randomize_utils.formatEvoLineStagesCounts(evoLine)
	-- If we have type policy pools, randomize with that
	if choicePools ~= nil then
		if not evo_line_randomize_utils.assignTypesToLine(entries, namePools, keyContext, choicePools)
		then
			logger.debug("evo_line_custom tryAssign failed type-assign shape=" .. shapeLabel
					.. " branched="
					.. tostring(evo_line_randomize_utils.lineEntriesHasBranch(entries)))
			return false
		end
	-- Otherwise, just ensure there is cards for the entries
	elseif not evo_line_randomize_utils.canFillEntries(entries, namePools, keyContext) then
		logger.debug("evo_line_custom tryAssign failed canFill shape=" .. shapeLabel)
		return false
	end

	-- Now that we have our shape and know it should fit, assign cards to the entries using the common
	-- code and approach
	evo_line_randomize_utils.assignEvoLineList(nil, entries, namePools, keyContext, toModifyByName)
	return true
end


-------- Custom weighted evo line pool drawing --------------------------------

-- Select a random shape, try it, remove all matching copies if it fails
-- We need this because we don't know if our shapes will fit the cards whereas for cards,
-- we know that they will fit since its based on what is already there
function evo_line_randomize_utils.assignOneCustomLineFromPool(poolState, namePools, keyContext,
		toModifyByName, choicePools)
	while true do
		-- Try to get an evo line
		local evoLine = evo_line_randomize_utils.drawFromPoolState(poolState, true)
		if evoLine == nil then
			-- We have exhausted everything, abort
			return false
		end
		-- try to assign cards to it (potentially considering type)
		if evo_line_randomize_utils.tryAssignDrawnLine(evoLine, namePools, keyContext, toModifyByName,
				choicePools)
		then
			return true
		end

		-- If we failed, remove it from all pools so we won't try it again
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

-- Done one stage at a time in reverse order to ensure we won't strand any higher level evos
-- Will continue until all names are assigned
function evo_line_randomize_utils.processCustomStagePass(stage, linePoolByMaxStage, namePools,
		keyContext, toModifyByName, approach, choicePools)
	local linePool = linePoolByMaxStage:get(stage:getValue()) or randomizer.list({})
	local poolState = evo_line_randomize_utils.createPoolDrawState(linePool, approach)
	while evo_line_randomize_utils.stageTotalPoolCount(namePools, stage, keyContext) > 0 do
		local assigned = evo_line_randomize_utils.assignOneCustomLineFromPool(poolState, namePools,
				keyContext, toModifyByName, choicePools)
		if not assigned then
			logger.warn("evo_line_custom could not assign remaining " .. tostring(stage)
					.. " names with available line shapes")
			logger.info("evo_line_custom abort " .. tostring(stage)
					.. " remainingNames="
					.. evo_line_randomize_utils.stageTotalPoolCount(namePools, stage, keyContext)
					.. " shapesLeft=" .. poolState.pool:size()
					.. " shapesUsed=" .. poolState.used:size()
					.. " pools=[" .. evo_line_randomize_utils.formatNamePoolCounts(namePools) .. "]")
			break
		end
	end
end

-- Keep drawing lines while any names are left to assign as one giant pool since we don't care about
-- their current stages
function evo_line_randomize_utils.processCustomAllTogetherPass(linePool, namePools, keyContext,
		toModifyByName, approach, choicePools)
	local poolState = evo_line_randomize_utils.createPoolDrawState(linePool, approach)

	while namePools:itemCount() > 0 do
		local assigned = evo_line_randomize_utils.assignOneCustomLineFromPool(poolState, namePools,
				keyContext, toModifyByName, choicePools)
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

-- choicePools nil means ignore energy types when drawing names. Non nil enables within type
-- pools and uses the weighted branch type rules from those pools.
function evo_line_randomize_utils.randomizeCustomEvoLines(context, args, choicePools)
	local sourceCards = randomizer.list(evo_line_randomize_utils.sourceCards(context, args.source,
			args.excludeTrainerProxies))
	local targets = randomizer.list(evo_line_randomize_utils.targetCards(context, args.excludeTrainerProxies))
	local toModifyByName = randomizer.groupBy(targets, "name")

	-- Form name pools from source cards and line pools from script args
	local withinType = evo_line_randomize_utils.namePoolTypeScope(args, choicePools)
	local keyContext = evo_line_randomize_utils.buildNamePoolKeyContext(sourceCards,
			args.grouping, withinType)
	local namePools = evo_line_randomize_utils.buildNamePools(sourceCards, keyContext)
	local linePool = randomizer.list(args.evoLinePool or {}):flatMapNTimes("weight",
			function(weighted)
				return weighted.evoLine
			end)

	logger.debug("evo_line_custom source=" .. tostring(args.source) .. " withinType="
			.. tostring(withinType) .. " grouping=" .. tostring(args.grouping) .. " approach="
			.. tostring(args.approach) .. " cards=" .. sourceCards:size())

	if args.grouping == "ALL_TOGETHER" then
		-- IN a single pass, keep drawing lines while any names are left to assign
		evo_line_randomize_utils.processCustomAllTogetherPass(linePool, namePools, keyContext,
				toModifyByName, args.approach, choicePools)
	else
		-- Group line pools by the highest stage in each evo line
		local linePoolByMaxStage = linePool:groupBy(function(evoLine)
			return evo_line_randomize_utils.lineMaxStageFromCounts(evoLine):getValue()
		end)

		-- Go one stage at a time in reverse order to ensure we don't leave any orphans
		local evoStages = evo_line_randomize_utils.EVO_STAGES
		for stageIndex = #evoStages, 1, -1 do
			local stage = evoStages[stageIndex]
			evo_line_randomize_utils.processCustomStagePass(stage, linePoolByMaxStage, namePools,
					keyContext, toModifyByName, args.approach, choicePools)
		end
	end

	evo_line_randomize_utils.finalize(context, args, targets, toModifyByName, keyContext)
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
function evo_line_randomize_utils.fixAllTogetherProxies(toModifyCards, toModifyByName, keyContext)
	if keyContext.grouping ~= "ALL_TOGETHER" then
		return
	end

	-- Step 0: construct maps of data for convinience
	local colorlessOnly = keyContext.splitsIntoPools
	local potentialSwapTargets = evo_line_randomize_utils.potentialNonProxySwapTargets(toModifyCards,
			colorlessOnly)
	local cardsByPrev = evo_line_randomize_utils.nonBasicNonProxyByPrevEvo(toModifyCards)

	toModifyCards:each(function(proxy)
		-- If its not a proxy or the proxy is already a basic, we are good
		if not proxy.isTrainerProxy then
			return
		end
		if proxy.stage:getValue() == evo_line_randomize_utils.evolutionStage.BASIC:getValue() then
			return
		end

		-- TODO: Remove deep copy. Also remove .items usage and instead make them use tables
		-- Step 1: random basic that is not a proxy and not a proxy's current prev evo
		local swapName = utils.consumeRandomElement(utils.deepCopy(potentialSwapTargets.items))

		-- Step 2: swap evo slots (stage/prev and anyone pointing at either name)
		local proxyName = proxy.name:toString()
		evo_line_randomize_utils.swapEvoSlots(proxyName, swapName, toModifyByName, cardsByPrev)

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

function evo_line_randomize_utils.finalize(context, args, targets, toModifyByName, keyContext)
	if not args.excludeTrainerProxies then
		evo_line_randomize_utils.fixAllTogetherProxies(targets, toModifyByName, keyContext)
	end
	-- We need to sync all cards including proxies regardless of if included because if they are excluded
	-- their evo data could have changed
	local syncCards = randomizer.list(context.modified:getRandomizableMonsterCardsWithProxies())
	evo_line_randomize_utils.syncEvoAiData(context, syncCards)
end


-------------------- Debugging Functions ----------------------------


-- For debug logging
function evo_line_randomize_utils.lineEntriesHasBranch(entries)
	local seenStages = {}
	for _, entry in ipairs(entries) do
		local stageValue = entry.stage:getValue()
		if seenStages[stageValue] then
			return true
		end
		seenStages[stageValue] = true
	end
	return false
end

return evo_line_randomize_utils
