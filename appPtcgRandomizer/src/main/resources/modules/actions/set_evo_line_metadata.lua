-- Assigns evoLineId and evoLineMaxStage on each monster card wrapper.
-- These are Lua side fields (not ROM data) used by other randomization modules.
local randomizer = require("randomizer")

local module
module = {
	id = "set_evo_line_metadata",
	name = "Set Evo Line Metadata",
	description = "Sets metadata for each evolution line in the rom for other modules reference. Makes no changes to the ROM",
	groups = { "Monsters", "Support" },
	modifies = { "Evolutions" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.2.0",
	},
	provides = {
		{ name = "evoLineId", type = "integer" },
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

function module.setEvoLineMetadata(context)
    -- Add the fields to the change detector first so it will log what is assigned
	randomizer.changedetector.addFields("Monster Cards", {
		{ field = "evoLineId", header = "Evo Line", align = "right" },
		{ field = "evoLineMaxStage", header = "Max Stage", align = "right" },
	})

    -- Set evo line metadata for both original and modified
	module.applyEvoLineMetadata(context.original:getRandomizableMonsterCards())
	module.applyEvoLineMetadata(context.modified:getRandomizableMonsterCards())
end

function module.applyEvoLineMetadata(monsterCards)
	local cardsByName = randomizer.groupBy(monsterCards, function(card)
		return card.name:toString()
	end)
	local nextEvoId = 1

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
				mc.evoLineMaxStage = mc.stage
			end)
		end
	end)

	-- Now go through all the cards with a previous evo and find their base
	-- card and copy the evoLineId
	cardsByName:each(function(_, cardsList)
		local baseCard = cardsList:get(1)
		if not baseCard.prevEvoName:isEmpty() then
			-- First set this cards max evo stage if its higher than it already is
			local cardEvoStage = baseCard.stage
			module.setMaxStageIfHigher(cardsList, cardEvoStage)

			-- Now recursively go through each of its prev evos and set their max evo stage
			-- if its higher than it already is
			local chainCard = baseCard
			while not chainCard.prevEvoName:isEmpty() do
				local prevName = chainCard.prevEvoName:toString()
				local prevCards = cardsByName:get(prevName)
				if prevCards == nil then
					break
				end
				chainCard = prevCards:get(1)
				module.setMaxStageIfHigher(prevCards, cardEvoStage)
			end
			-- Finally set the evoLineId for the base cards evo line id. Any others in this line
			-- that don't have it yet will get it assigned when they are processed too
			local evoLineId = chainCard.evoLineId
			cardsList:each(function(mc)
				mc.evoLineId = evoLineId
			end)
		end
	end)
	logger.info("Module set_evo_line_metadata completed for "
		.. randomizer.list(monsterCards):size() .. " cards")
end

return module
