-- Use the same seed on every case
local seed = 42
local card_sets = require("support.card_sets")

return {
	{
		name = "keep_duplicates",
		module = "retreat_cost_cards_stage_max_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			{ id = "MONSTER_001", retreatCost = 1 },
			{ id = "MONSTER_002", retreatCost = 3 },
			{ id = "MONSTER_003_1", retreatCost = 2 },
			{ id = "MONSTER_004", retreatCost = 3 },
			{ id = "MONSTER_005", retreatCost = 2 },
			{ id = "MONSTER_006", retreatCost = 1 },

			{ id = "MONSTER_010", retreatCost = 1 },
			{ id = "MONSTER_012", retreatCost = 1 },

			{ id = "MONSTER_011", retreatCost = 2 },
			{ id = "MONSTER_013", retreatCost = 3 },

			{ id = "MONSTER_014", retreatCost = 1 },
			{ id = "MONSTER_015", retreatCost = 3 },

			{ id = "MONSTER_016", retreatCost = 2 },
			{ id = "MONSTER_017", retreatCost = 1 },

			{ id = "MONSTER_018_1", retreatCost = 3 },
			{ id = "MONSTER_019", retreatCost = 2 },
		},
	},
	{
		name = "from_current",
		module = "retreat_cost_cards_stage_max_stage",
		seed = seed,
		args = {
			source = "CURRENT",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			{ id = "MONSTER_001", retreatCost = 1 },
			{ id = "MONSTER_002", retreatCost = 1 },
			{ id = "MONSTER_003_1", retreatCost = 1 },
			{ id = "MONSTER_004", retreatCost = 3 },
			{ id = "MONSTER_005", retreatCost = 2 },
			{ id = "MONSTER_006", retreatCost = 0 },

			{ id = "MONSTER_010", retreatCost = 1 },
			{ id = "MONSTER_012", retreatCost = 3 },

			{ id = "MONSTER_011", retreatCost = 2 },
			{ id = "MONSTER_013", retreatCost = 1 },

			{ id = "MONSTER_014", retreatCost = 3 },
			{ id = "MONSTER_015", retreatCost = 0 },

			{ id = "MONSTER_016", retreatCost = 1 },
			{ id = "MONSTER_017", retreatCost = 3 },

			{ id = "MONSTER_018_1", retreatCost = 1 },
			{ id = "MONSTER_019", retreatCost = 3 },
		},
	},
	{
		name = "remove_duplicates",
		module = "retreat_cost_cards_stage_max_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "REMOVE_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			{ id = "MONSTER_001", retreatCost = 2 },
			{ id = "MONSTER_002", retreatCost = 3 },
			{ id = "MONSTER_003_1", retreatCost = 1 },
			{ id = "MONSTER_004", retreatCost = 3 },
			{ id = "MONSTER_005", retreatCost = 2 },
			{ id = "MONSTER_006", retreatCost = 1 },

			{ id = "MONSTER_010", retreatCost = 1 },
			{ id = "MONSTER_012", retreatCost = 1 },

			{ id = "MONSTER_011", retreatCost = 2 },
			{ id = "MONSTER_013", retreatCost = 3 },

			{ id = "MONSTER_014", retreatCost = 1 },
			{ id = "MONSTER_015", retreatCost = 3 },

			{ id = "MONSTER_016", retreatCost = 2 },
			{ id = "MONSTER_017", retreatCost = 1 },

			{ id = "MONSTER_018_1", retreatCost = 3 },
			{ id = "MONSTER_019", retreatCost = 2 },
		},
	},
	{
		name = "fully_random",
		module = "retreat_cost_cards_stage_max_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "FULLY_RANDOM",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			{ id = "MONSTER_001", retreatCost = 1 },
			{ id = "MONSTER_002", retreatCost = 3 },
			{ id = "MONSTER_003_1", retreatCost = 2 },
			{ id = "MONSTER_004", retreatCost = 2 },
			{ id = "MONSTER_005", retreatCost = 3 },
			{ id = "MONSTER_006", retreatCost = 1 },

			{ id = "MONSTER_010", retreatCost = 1 },
			{ id = "MONSTER_012", retreatCost = 1 },

			{ id = "MONSTER_011", retreatCost = 2 },
			{ id = "MONSTER_013", retreatCost = 2 },

			{ id = "MONSTER_014", retreatCost = 1 },
			{ id = "MONSTER_015", retreatCost = 3 },

			{ id = "MONSTER_016", retreatCost = 2 },
			{ id = "MONSTER_017", retreatCost = 2 },

			{ id = "MONSTER_018_1", retreatCost = 3 },
			{ id = "MONSTER_019", retreatCost = 3 },
		},
	},
}
