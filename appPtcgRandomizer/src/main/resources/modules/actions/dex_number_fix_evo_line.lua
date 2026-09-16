-- Reassigns dex numbers globally so evo lines get sequential ids so
-- they will appear in sequential order in the game.
-- Run after dex number randomization. Needs evoLineId on each card.
local randomizer = require("randomizer")
local conversion_utils = require("modules.util.conversion_utils")

local module
module = {
	id = "dex_number_fix_evo_line",
	name = "Make Dex Numbers Consistent for Evo Lines",
	description = "Updates dex numbers so that evo lines will be shown sequentially in the game",
	groups = { "Monsters", "Pokedex", "Evolutions", "Support", "Consistency" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	needs = {
		{ name = "evoLineId", type = "integer" },
	},
	seeded = false,
	execute = function(context, args)
		return module.fixDexNumbers(context, args)
	end,
}

function module.lowestBasicDexNumber(line)
	local basics = randomizer.groupBy(line, "stage"):get("BASIC")
	if basics == nil or basics:isEmpty() then
		return nil
	end
	return basics:min(function(mc)
		return conversion_utils.byteToUnsigned(mc.dexNumber)
	end)
end

function module.assignLine(line, startId)
	local dexId = startId
	-- Go through each stage in this line and assign the dex number
	randomizer.groupBy(line, "stage"):sort():each(function(_, cardsAtStage)
		cardsAtStage:each(function(mc)
			mc.dexNumber = conversion_utils.byteToSigned(dexId)
		end)
		dexId = dexId + 1
	end)
	return dexId
end

function module.fixDexNumbers(context)
	local cards = context.modified:getRandomizableMonsterCards()
	local nextId = 1

	-- Group by evo line id, get the lowest basic dex number for each line, sort by that,
	-- then assign the new dex numbers
	randomizer.groupBy(cards, "evoLineId"):map(function(_, line)
		return {
			line = line,
			lowestBasicDexNum = module.lowestBasicDexNumber(line) or 999,
		}
	end):sort(function(a, b)
		return a.lowestBasicDexNum < b.lowestBasicDexNum
	end):each(function(entry)
		nextId = module.assignLine(entry.line, nextId)
	end)
end

return module
