-- Use the same seed on every case
local seed = 42
local card_sets = require("support.card_sets")

return {
	{
		name = "all_together",
		module = "num_moves_together_and_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		-- Matches ROM numbers exactly
		expect = {
			{ id = "MONSTER_001", numMoves = 0 },
			{ id = "MONSTER_002", numMoves = 2 },
			{ id = "MONSTER_003_1", numMoves = 2 },
			{ id = "MONSTER_004", numMoves = 1 },
			{ id = "MONSTER_005", numMoves = 2 },
			{ id = "MONSTER_006", numMoves = 2 },
			{ id = "MONSTER_010", numMoves = 2 },
			{ id = "MONSTER_011", numMoves = 1 },
			{ id = "MONSTER_012", numMoves = 2 },
			{ id = "MONSTER_013", numMoves = 1 },
			{ id = "MONSTER_014", numMoves = 2 },
			{ id = "MONSTER_015", numMoves = 2 },
			{ id = "MONSTER_016", numMoves = 2 },
			{ id = "MONSTER_017", numMoves = 1 },
			{ id = "MONSTER_018_1", numMoves = 1 },
			{ id = "MONSTER_019", numMoves = 2 },
		},

	},
	{
		name = "by_stage",
		module = "num_moves_together_and_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
			grouping = "BY_STAGE",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		-- Matches ROM numbers exactly
		expect = {
			{ id = "MONSTER_001", numMoves = 2 }, -- BASIC
			{ id = "MONSTER_002", numMoves = 2 }, -- BASIC
			{ id = "MONSTER_003_1", numMoves = 1 }, -- BASIC
			{ id = "MONSTER_004", numMoves = 1 }, -- BASIC
			{ id = "MONSTER_005", numMoves = 2 }, -- BASIC
			{ id = "MONSTER_006", numMoves = 2 }, -- BASIC
			{ id = "MONSTER_010", numMoves = 2 }, -- BASIC
			{ id = "MONSTER_012", numMoves = 1 }, -- BASIC
			{ id = "MONSTER_014", numMoves = 2 }, -- BASIC
			{ id = "MONSTER_015", numMoves = 2 }, -- BASIC

			{ id = "MONSTER_011", numMoves = 1 }, -- STAGE_1
			{ id = "MONSTER_013", numMoves = 1 }, -- STAGE_1
			{ id = "MONSTER_016", numMoves = 2 }, -- STAGE_1
			{ id = "MONSTER_017", numMoves = 2 }, -- STAGE_1

			{ id = "MONSTER_018_1", numMoves = 2 }, -- STAGE_2
			{ id = "MONSTER_019", numMoves = 0 }, -- STAGE_2
		},

	},
	{
		name = "from_current",
		module = "num_moves_together_and_stage",
		seed = seed,
		args = {
			source = "CURRENT",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		-- Matches CURRENT numbers exactly
		expect = {
			{ id = "MONSTER_001", numMoves = 2 },
			{ id = "MONSTER_002", numMoves = 0 },
			{ id = "MONSTER_003_1", numMoves = 0 },
			{ id = "MONSTER_004", numMoves = 2 },
			{ id = "MONSTER_005", numMoves = 1 },
			{ id = "MONSTER_006", numMoves = 1 },
			{ id = "MONSTER_010", numMoves = 1 },
			{ id = "MONSTER_011", numMoves = 2 },
			{ id = "MONSTER_012", numMoves = 1 },
			{ id = "MONSTER_013", numMoves = 2 },
			{ id = "MONSTER_014", numMoves = 1 },
			{ id = "MONSTER_015", numMoves = 1 },
			{ id = "MONSTER_016", numMoves = 2 },
			{ id = "MONSTER_017", numMoves = 2 },
			{ id = "MONSTER_018_1", numMoves = 2 },
			{ id = "MONSTER_019", numMoves = 0 },
		},

	},
	{
		name = "remove_duplicates",
		module = "num_moves_together_and_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "REMOVE_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		-- Even split of all 3 values as expected
		expect = {
			{ id = "MONSTER_001", numMoves = 1 },
			{ id = "MONSTER_002", numMoves = 2 },
			{ id = "MONSTER_003_1", numMoves = 0 },
			{ id = "MONSTER_004", numMoves = 2 },
			{ id = "MONSTER_005", numMoves = 1 },
			{ id = "MONSTER_006", numMoves = 0 },
			{ id = "MONSTER_010", numMoves = 2 },
			{ id = "MONSTER_011", numMoves = 0 },
			{ id = "MONSTER_012", numMoves = 1 },
			{ id = "MONSTER_013", numMoves = 2 },
			{ id = "MONSTER_014", numMoves = 0 },
			{ id = "MONSTER_015", numMoves = 1 },
			{ id = "MONSTER_016", numMoves = 2 },
			{ id = "MONSTER_017", numMoves = 1 },
			{ id = "MONSTER_018_1", numMoves = 0 },
			{ id = "MONSTER_019", numMoves = 1 },
		},

	},
	{
		name = "fully_random",
		module = "num_moves_together_and_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "FULLY_RANDOM",
			grouping = "ALL_TOGETHER",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		-- Doesn't match but keeps similar disto. Here one 0 is selected
		-- which is "expected" for fully random
		expect = {
			{ id = "MONSTER_001", numMoves = 0 },
			{ id = "MONSTER_002", numMoves = 1 },
			{ id = "MONSTER_003_1", numMoves = 2 },
			{ id = "MONSTER_004", numMoves = 1 },
			{ id = "MONSTER_005", numMoves = 2 },
			{ id = "MONSTER_006", numMoves = 1 },
			{ id = "MONSTER_010", numMoves = 1 },
			{ id = "MONSTER_011", numMoves = 2 },
			{ id = "MONSTER_012", numMoves = 2 },
			{ id = "MONSTER_013", numMoves = 2 },
			{ id = "MONSTER_014", numMoves = 1 },
			{ id = "MONSTER_015", numMoves = 2 },
			{ id = "MONSTER_016", numMoves = 1 },
			{ id = "MONSTER_017", numMoves = 2 },
			{ id = "MONSTER_018_1", numMoves = 1 },
			{ id = "MONSTER_019", numMoves = 1 },
		},

	},
}
