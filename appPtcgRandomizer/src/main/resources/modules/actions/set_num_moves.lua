local randomizer = require("randomizer")

local module
module = {
	id = "set_num_moves",
	name = "Set X Moves Per Card",
	description = "Sets the number of moves per card",
	seeded = false,
	groups = { "Monsters", "Support", "Moves", "Attacks", "Powers" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		{
			name = "numMoves",
			displayName = "Number of Moves",
			description = "Sets how many moves each card should have",
			definition = {
				type = "integer",
				constraint = { type = "range", min = 0, max = 2, },
			},
			default = 2,
		},
	},
	execute = function(context, args)
		return module.setNumMoves(context, args)
	end,
}

function module.setNumMoves(context, args)
	randomizer.list(context.modified:getRandomizableMonsterCards()):each(function(card)
		-- Set the num moves not overridding assignments. This intentially does not check
		-- locked slots first so it will be apparant in the logs if a card is skipped due
		-- to a locked slot
		card:setNumMoves(args.numMoves)
	end)
end

return module
