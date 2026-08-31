-- Typed costs that do not match the card, plus a colorless card that should eat them all.
local card_sets = require("support.card_sets")

return {
	name = "match_card_type",
	module = "all_moves_match_type",
	cards = card_sets.MIXED_COST_CARDS,
	expect = {
		{
			id = "MONSTER_001", type = "MONSTER_FIRE", moves = {
				{ name = "Burn", costs = { FIRE = 2, COLORLESS = 1 } },
				{ name = "Forest Fire", costs = { FIRE = 2, COLORLESS = 1 } },
			},
		},
		{
			id = "MONSTER_002", type = "MONSTER_COLORLESS", moves = {
				{ name = "Tackle", costs = { COLORLESS = 3 } },
				{ name = "Slam", costs = { COLORLESS = 2 } },
			},
		},
	},
}
