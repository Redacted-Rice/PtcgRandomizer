local card_sets = require("support.card_sets")

-- repeats weight the pool. minimize_repeats pops each entry before refilling
local retreatPool = { 0, 1, 1, 2, 2 }

return {
	{
		name = "minimize_repeats",
		module = "retreat_cost_custom_together",
		seed = 42,
		args = {
			approach = "MINIMIZE_REPEATS",
			retreatPool = retreatPool,
		},
		cards = card_sets.STD_TEST_CARDS_ROM,
		expect = {
			{ id = "MONSTER_001", retreatCost = 1 },
			{ id = "MONSTER_002", retreatCost = 2 },
			{ id = "MONSTER_003_1", retreatCost = 2 },
			{ id = "MONSTER_004", retreatCost = 0 },
			{ id = "MONSTER_005", retreatCost = 2 },
			{ id = "MONSTER_006", retreatCost = 1 },
			{ id = "MONSTER_010", retreatCost = 0 },
			{ id = "MONSTER_011", retreatCost = 1 },
			{ id = "MONSTER_012", retreatCost = 2 },
			{ id = "MONSTER_013", retreatCost = 1 },
			{ id = "MONSTER_014", retreatCost = 2 },
			{ id = "MONSTER_015", retreatCost = 1 },
			{ id = "MONSTER_016", retreatCost = 2 },
			{ id = "MONSTER_017", retreatCost = 1 },
			{ id = "MONSTER_018_1", retreatCost = 0 },
			{ id = "MONSTER_019", retreatCost = 1 },
		},
	},
	{
		name = "fully_random",
		module = "retreat_cost_custom_together",
		seed = 41,
		args = {
			approach = "FULLY_RANDOM",
			retreatPool = retreatPool,
		},
		cards = card_sets.STD_TEST_CARDS_ROM,
		expect = {
			{ id = "MONSTER_001", retreatCost = 1 },
			{ id = "MONSTER_002", retreatCost = 1 },
			{ id = "MONSTER_003_1", retreatCost = 1 },
			{ id = "MONSTER_004", retreatCost = 0 },
			{ id = "MONSTER_005", retreatCost = 2 },
			{ id = "MONSTER_006", retreatCost = 0 },
			{ id = "MONSTER_010", retreatCost = 2 },
			{ id = "MONSTER_011", retreatCost = 1 },
			{ id = "MONSTER_012", retreatCost = 1 },
			{ id = "MONSTER_013", retreatCost = 0 },
			{ id = "MONSTER_014", retreatCost = 1 },
			{ id = "MONSTER_015", retreatCost = 0 },
			{ id = "MONSTER_016", retreatCost = 2 },
			{ id = "MONSTER_017", retreatCost = 1 },
			{ id = "MONSTER_018_1", retreatCost = 2 },
			{ id = "MONSTER_019", retreatCost = 2 },
		},
	},
}
