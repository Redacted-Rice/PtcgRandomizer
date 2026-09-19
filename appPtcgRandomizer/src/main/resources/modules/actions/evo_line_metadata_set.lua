-- Assigns evoLineId and evoLineMaxStage on each monster card wrapper.
-- These are Lua side fields (not ROM data) used by other randomization modules.
local randomizer = require("randomizer")

local module
module = {
	id = "evo_line_metadata_set",
	name = "Set Evo Line Metadata",
	description = "Sets metadata for each evolution line in the rom for other modules reference. Makes no changes to the ROM",
	groups = { "Monsters", "Support", "Evolutions" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	provides = {
		{ name = "evoLineId", type = "integer" },
		{ name = "evoBranchIds", type = "List<integer>" },
		{ name = "evoLineMaxStage", type = "EvolutionStage" },
	},
	seeded = false,
	execute = function(context, args)
		return module.setEvoLineMetadata(context, args)
	end,
}

function module.setMaxStageIfHigher(cardsList, cardEvoStage)
	local first = cardsList:get(1)
	local currentMax = first.evoLineMaxStage
	-- Set if its higher than the current max or unset
	if currentMax == nil or cardEvoStage:getValue() > currentMax:getValue() then
		cardsList:each(function(mc)
			mc.evoLineMaxStage = cardEvoStage
		end)
	end
end

function module.addBranchIdIfNotPresent(mc, branchId)
	if mc.evoBranchIds == nil then
		mc.evoBranchIds = { branchId }
	else
		for _, id in ipairs(mc.evoBranchIds) do
			if id == branchId then
				return
			end
		end
		table.insert(mc.evoBranchIds, branchId)
	end
end

function module.applyAndPropagateBranchId(lineCardsByName, cards, branchId, branchMaxStage)
	cards:each(function(mc)
		module.addBranchIdIfNotPresent(mc, branchId)
	end)
	module.setMaxStageIfHigher(cards, branchMaxStage)

	local card = cards:get(1)
	local prevName = card.prevEvoName:toString()
	if prevName == "" then
		return
	end
	local prevCards = lineCardsByName:get(prevName)
	if prevCards == nil then
		error("Previous cards not found for " .. card.name:toString())
		return
	end
	module.applyAndPropagateBranchId(lineCardsByName, prevCards, branchId, branchMaxStage)
end

function module.setEvoLineMetadata(context)
	-- Add the fields to the change detector first so it will log what is assigned
	randomizer.changedetector.addFields("Monster Cards", {
		{ field = "evoLineId", header = "Evo Line", align = "right" },
		{ field = "evoBranchIds", header = "Branch Ids", align = "right" },
		{ field = "evoLineMaxStage", header = "Max Stage", align = "right" },
	})

	-- Set evo line metadata for both original and modified (includes trainer proxies)
	module.applyEvoLineMetadata(context.original:getRandomizableMonsterCardsWithProxies())
	module.applyEvoLineMetadata(context.modified:getRandomizableMonsterCardsWithProxies())
end

function module.applyEvoLineMetadata(monsterCards)
	local cards = randomizer.list(monsterCards)
	local cardsByName = cards:groupBy(function(card)
		return card.name:toString()
	end)
	local nextEvoId = 1
	local nextBranchId = 1

	-- First set the evo line for each card name with no previous evo. This will
	-- set the evo id for each line. Note that things like flying or surfing
	-- will count as a different evo line which is consistent with how the
	-- game handles it already
	cardsByName:each(function(_, cardsList)
		local card = cardsList:get(1)
		local prevName = card.prevEvoName:toString()
		if prevName == "" or cardsByName:get(prevName) == nil then
			local thisEvoId = nextEvoId
			nextEvoId = nextEvoId + 1
			cardsList:each(function(mc)
				mc.evoLineId = thisEvoId
			end)
		end
	end)

	-- Now go through all the cards with a previous evo and find their base
	-- card and copy the evoLineId
	cardsByName:each(function(_, cardsList)
		local baseCard = cardsList:get(1)
		if not baseCard.prevEvoName:isEmpty() then
			-- Recursively go through each of its prev evos and set their max evo stage
			-- if its higher than it already is
			local chainCard = baseCard
			while not chainCard.prevEvoName:isEmpty() do
				local prevName = chainCard.prevEvoName:toString()
				local prevCards = cardsByName:get(prevName)
				if prevCards == nil then
					break
				end
				chainCard = prevCards:get(1)
			end
			-- Finally set the evoLineId for the base cards evo line id. Any others in this line
			-- that don't have it yet will get it assigned when they are processed too
			local evoLineId = chainCard.evoLineId
			cardsList:each(function(mc)
				mc.evoLineId = evoLineId
			end)
		end
	end)

	-- Now we go through the evo lines and set the branch ids and max stages for each branch
	-- 1. Group and iterate through each evo line
	cards:groupBy("evoLineId"):each(function(_, line)
		-- 2. For each evo line, group by stage (reverse sorted) then name
		local byStage = line:groupBy("stage"):sort(function(a, b)
			return a > b
		end)
		local byName = line:groupBy(function(card)
			return card.name:toString()
		end)

		-- 3. for each stage group (in reverse order), check if there is a evoBranchId.
		byStage:each(function(stage, stageCards)
			local stageCardsByName = stageCards:groupBy(function(card)
				return card.name:toString()
			end)
			stageCardsByName:each(function(name, cardsWithName)
				local firstCard = cardsWithName:get(1)

				-- 4.a. If there is at least one, do nothing. If there is none, assing the next one
				if firstCard.evoBranchIds ~= nil and #firstCard.evoBranchIds > 0 then
					return
				end

				-- 4.b If there is not one, assign the next one and then recursively find its prevEvo in the name
				-- and add that evoBranchId to its list and repeat until we get to the end of the branch
				local branchId = nextBranchId
				nextBranchId = nextBranchId + 1
				module.applyAndPropagateBranchId(byName, cardsWithName, branchId, firstCard.stage)
			end)
		end)
	end)

	logger.info("Module evo_line_metadata_set completed for "
		.. cards:size() .. " cards")
end

return module
