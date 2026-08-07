local randomizer = require("randomizer")

local module
module = {
	id = "even_rando_evo_line_types",
	name = "Even Random Evo Line Types",
	description = "Randomize monster type per evolution line with balanced type distribution",
	groups = { "cards" },
	modifies = { "type" },
	author = "Redacted Rice",
	version = "0.5",
	requires = {
		PtcgRandomizer = "0.2.0",
	},
	needs = {
		{ name = "evoLineId", type = "integer" },
	},
	execute = function(context, args)
		return module.randomizeEvoLineTypes(context, args)
	end,
}

function module.randomizeEvoLineTypes(context)
	local monsterCards = context.modified:getRandomizableMonsterCards()

	-- Get all types from monster cards
	-- TODO later: Probably use original not modified - or make it an config option?
	-- TODO later: Add options for different modes: Even distro, rom based distro, curr based distro, custom distro, etc.
	local typePool = randomizer.groupBy(monsterCards, "type"):map(function(_, cards)
		return cards:get(1).type
	end)

	-- Get one card from each evo line to set the type of
	local byEvoLine = randomizer.groupBy(monsterCards, "evoLineId")
	local representatives = byEvoLine:map(function(_, line)
		return line:get(1)
	end)

	-- Randomize the representative card types
	typePool:useToRandomize(representatives, "type", {
		consumable = true,
		regenerate = true,
	})

	-- Copy each line's type from its first card. Since byEvoLine is unchanged, and
	-- representatives were those same first-card objects we can safely use the first
	-- card again as it will still be the representative card
	byEvoLine:each(function(_, line)
		local cardType = line:get(1).type
		line:each(function(card)
			card.type = cardType
		end)
	end)
end

return module
