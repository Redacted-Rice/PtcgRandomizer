local randomizer = require("randomizer")

local module
module = {
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

-- TODO later: None the other modules actually use this ATM. Consider how to handle this
-- for those or to make separate ones that do use this
function module.setNumMoves(context, args)
	randomizer.list(context.modified:getRandomizableMonsterCards()):each(function(card)
        -- Set the num moves not overridding assignments. This intentially does not check
        -- locked slots first so it will be apparant in the logs if a card is skipped due
        -- to a locked slot
		card:setNumMoves(args.numMoves)
	end)
end

return module
