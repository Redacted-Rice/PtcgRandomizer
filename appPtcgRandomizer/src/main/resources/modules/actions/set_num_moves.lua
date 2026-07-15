local randomizer = require("randomizer")

local module = {
	id = "set_num_moves",
	name = "Set X Moves Per Card",
	description = "Sets the number of active moves per card",
	seeded = false,
	groups = { "moves" },
	modifies = { "numMoves"},
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.2.0",
	},
	arguments = {
		{
			name = "numMoves",
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
	randomizer.list(context.modified:getMonsterCards()):each(function(card)
		card:setNumMoves(args.numMoves)
	end)
end

return module
