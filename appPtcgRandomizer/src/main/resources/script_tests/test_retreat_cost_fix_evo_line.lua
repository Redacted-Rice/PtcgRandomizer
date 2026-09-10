-- Fix runs on CURRENT where the inversions live.
local card_sets = require("support.card_sets")

return {
	{
		name = "match_previous",
		module = "retreat_cost_fix_evo_line",
		args = {
			mode = "Match Previous",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			-- Solo basics unchanged
			{ id = "MONSTER_001", retreatCost = 0 },
			{ id = "MONSTER_002", retreatCost = 1 },
			{ id = "MONSTER_003_1", retreatCost = 1 },
			{ id = "MONSTER_004", retreatCost = 2 },
			{ id = "MONSTER_005", retreatCost = 1 },
			{ id = "MONSTER_006", retreatCost = 3 },

			-- Inverted: evo raised to match basic
			{ id = "MONSTER_010", retreatCost = 3 },
			{ id = "MONSTER_011", retreatCost = 3 },
			-- Ordered: unchanged
			{ id = "MONSTER_012", retreatCost = 1 },
			{ id = "MONSTER_013", retreatCost = 2 },
			-- Split: later stages raised to the highest earlier value seen
			{ id = "MONSTER_014", retreatCost = 0 },
			{ id = "MONSTER_015", retreatCost = 3 },
			{ id = "MONSTER_016", retreatCost = 3 },
			{ id = "MONSTER_017", retreatCost = 3 },
			{ id = "MONSTER_018_1", retreatCost = 3 },
			{ id = "MONSTER_019", retreatCost = 3 },
		},
	},
	{
		name = "redistribute",
		module = "retreat_cost_fix_evo_line",
		args = {
			mode = "Redistribute",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			-- Solo basics unchanged
			{ id = "MONSTER_001", retreatCost = 0 },
			{ id = "MONSTER_002", retreatCost = 1 },
			{ id = "MONSTER_003_1", retreatCost = 1 },
			{ id = "MONSTER_004", retreatCost = 2 },
			{ id = "MONSTER_005", retreatCost = 1 },
			{ id = "MONSTER_006", retreatCost = 3 },

			-- Inverted: swapped
			{ id = "MONSTER_010", retreatCost = 1 },
			{ id = "MONSTER_011", retreatCost = 3 },
			-- Ordered: unchanged
			{ id = "MONSTER_012", retreatCost = 1 },
			{ id = "MONSTER_013", retreatCost = 2 },
			-- Split: values redistributed up the line
			{ id = "MONSTER_014", retreatCost = 0 },
			{ id = "MONSTER_015", retreatCost = 1 },
			{ id = "MONSTER_016", retreatCost = 1 },
			{ id = "MONSTER_017", retreatCost = 3 },
			{ id = "MONSTER_018_1", retreatCost = 3 },
			{ id = "MONSTER_019", retreatCost = 3 },
		},
	},
}
