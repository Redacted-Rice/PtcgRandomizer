-- Mixed typed costs plus colorless so the rewrite is obvious.
local card_sets = require("support.card_sets")
local fields = require("support.fields")

return {
	name = "rewrite_to_colorless",
	module = "all_moves_colorless",
	cards = card_sets.MIXED_COST_CARDS,
	expect = {
		{
			id = "MONSTER_001", type = "MONSTER_FIRE", moves = fields.moves({
				{ name = "Burn", costs = fields.costs({ COLORLESS = 3 }) },
				{ name = "Forest Fire", costs = fields.costs({ COLORLESS = 3 }) },
			}),
		},
		{
			id = "MONSTER_002", type = "MONSTER_COLORLESS", moves = fields.moves({
				{ name = "Tackle", costs = fields.costs({ COLORLESS = 3 }) },
				{ name = "Slam", costs = fields.costs({ COLORLESS = 2 }) },
			}),
		},
	},
}
