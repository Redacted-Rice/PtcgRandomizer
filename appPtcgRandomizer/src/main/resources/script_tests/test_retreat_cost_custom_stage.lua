local card_sets = require("support.card_sets")

local retreatPools = {
	BASIC = { 0, 1, 2 },
	STAGE_1 = { 1, 2, 3 },
	STAGE_2 = { 1, 2, 3 },
}

return {
	{
		name = "minimize_repeats",
		module = "retreat_cost_custom_stage",
		seed = 42,
		args = {
			approach = "MINIMIZE_REPEATS",
			retreatPools = retreatPools,
		},
		cards = card_sets.STD_TEST_CARDS_ROM,
		expect = {
			{ id = "MONSTER_001", retreatCost = 0 },
			{ id = "MONSTER_002", retreatCost = 1 },
			{ id = "MONSTER_003_1", retreatCost = 2 },
			{ id = "MONSTER_004", retreatCost = 0 },
			{ id = "MONSTER_005", retreatCost = 1 },
			{ id = "MONSTER_006", retreatCost = 1 },
			{ id = "MONSTER_010", retreatCost = 1 },
			{ id = "MONSTER_012", retreatCost = 0 },
			{ id = "MONSTER_014", retreatCost = 2 },
			{ id = "MONSTER_015", retreatCost = 2 },

			{ id = "MONSTER_011", retreatCost = 1 },
			{ id = "MONSTER_013", retreatCost = 2 },
			{ id = "MONSTER_016", retreatCost = 3 },
			{ id = "MONSTER_017", retreatCost = 3 },

			{ id = "MONSTER_018_1", retreatCost = 2 },
			{ id = "MONSTER_019", retreatCost = 3 },
		},
	},
	{
		name = "fully_random",
		module = "retreat_cost_custom_stage",
		seed = 53,
		args = {
			approach = "FULLY_RANDOM",
			retreatPools = retreatPools,
		},
		cards = card_sets.STD_TEST_CARDS_ROM,
		expect = {
			{ id = "MONSTER_001", retreatCost = 0 },
			{ id = "MONSTER_002", retreatCost = 1 },
			{ id = "MONSTER_003_1", retreatCost = 0 },
			{ id = "MONSTER_004", retreatCost = 1 },
			{ id = "MONSTER_005", retreatCost = 1 },
			{ id = "MONSTER_006", retreatCost = 0 },
			{ id = "MONSTER_010", retreatCost = 2 },
			{ id = "MONSTER_012", retreatCost = 1 },
			{ id = "MONSTER_014", retreatCost = 0 },
			{ id = "MONSTER_015", retreatCost = 2 },

			{ id = "MONSTER_011", retreatCost = 1 },
			{ id = "MONSTER_013", retreatCost = 3 },
			{ id = "MONSTER_016", retreatCost = 1 },
			{ id = "MONSTER_017", retreatCost = 1 },

			{ id = "MONSTER_018_1", retreatCost = 2 },
			{ id = "MONSTER_019", retreatCost = 3 },
		},
	},
}
