-- Typed costs that do not match the card, plus a colorless card that should eat them all.
local card_sets = require("support.card_sets")
local fields = require("support.fields")

return {
	name = "match_card_type",
	module = "all_moves_match_type",
	cards = card_sets.MIXED_COST_CARDS,
	expect = {
		{
			id = "MONSTER_001", type = "MONSTER_FIRE", moves = fields.moves({
				{ name = "Burn", costs = fields.costs({ FIRE = 2, COLORLESS = 1 }) },
				{ name = "Forest Fire", costs = fields.costs({ FIRE = 2, COLORLESS = 1 }) },
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
