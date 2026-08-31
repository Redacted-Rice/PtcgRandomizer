-- Use the same seed on every case
local seed = 42
local card_sets = require("support.card_sets")

return {
	{
		name = "keep_duplicates",
		module = "randomize_num_moves_by_stage_max_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			{ id = "MONSTER_001", numMoves = 2 }, -- BASIC/BASIC
			{ id = "MONSTER_002", numMoves = 2 }, -- BASIC/BASIC
			{ id = "MONSTER_003_1", numMoves = 1 }, -- BASIC/BASIC
			{ id = "MONSTER_004", numMoves = 2 }, -- BASIC/BASIC
			{ id = "MONSTER_005", numMoves = 1 }, -- BASIC/BASIC
			{ id = "MONSTER_006", numMoves = 2 }, -- BASIC/BASIC

			{ id = "MONSTER_010", numMoves = 2 }, -- BASIC/STAGE_1
			{ id = "MONSTER_012", numMoves = 2 }, -- BASIC/STAGE_1

			{ id = "MONSTER_011", numMoves = 1 }, -- STAGE_1/STAGE_1
			{ id = "MONSTER_013", numMoves = 2 }, -- STAGE_1/STAGE_1

			{ id = "MONSTER_014", numMoves = 2 }, -- BASIC/STAGE_2
			{ id = "MONSTER_015", numMoves = 1 }, -- BASIC/STAGE_2

			{ id = "MONSTER_016", numMoves = 2 }, -- STAGE_1/STAGE_2
			{ id = "MONSTER_017", numMoves = 1 }, -- STAGE_1/STAGE_2

			{ id = "MONSTER_018_1", numMoves = 2 }, -- STAGE_2/STAGE_2
			{ id = "MONSTER_019", numMoves = 0 }, -- STAGE_2/STAGE_2
		},
	},
	{
		name = "from_current",
		module = "randomize_num_moves_by_stage_max_stage",
		seed = seed,
		args = {
			source = "CURRENT",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			{ id = "MONSTER_001", numMoves = 0 }, -- BASIC/BASIC
			{ id = "MONSTER_002", numMoves = 1 }, -- BASIC/BASIC
			{ id = "MONSTER_003_1", numMoves = 2 }, -- BASIC/BASIC
			{ id = "MONSTER_004", numMoves = 2 }, -- BASIC/BASIC
			{ id = "MONSTER_005", numMoves = 2 }, -- BASIC/BASIC
			{ id = "MONSTER_006", numMoves = 1 }, -- BASIC/BASIC

			{ id = "MONSTER_010", numMoves = 0 }, -- BASIC/STAGE_1
			{ id = "MONSTER_012", numMoves = 1 }, -- BASIC/STAGE_1

			{ id = "MONSTER_011", numMoves = 2 }, -- STAGE_1/STAGE_1
			{ id = "MONSTER_013", numMoves = 1 }, -- STAGE_1/STAGE_1

			{ id = "MONSTER_014", numMoves = 1 }, -- BASIC/STAGE_2
			{ id = "MONSTER_015", numMoves = 2 }, -- BASIC/STAGE_2

			{ id = "MONSTER_016", numMoves = 0 }, -- STAGE_1/STAGE_2
			{ id = "MONSTER_017", numMoves = 2 }, -- STAGE_1/STAGE_2

			{ id = "MONSTER_018_1", numMoves = 1 }, -- STAGE_2/STAGE_2
			{ id = "MONSTER_019", numMoves = 2 }, -- STAGE_2/STAGE_2
		},
	},
	{
		name = "remove_duplicates",
		module = "randomize_num_moves_by_stage_max_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "REMOVE_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			{ id = "MONSTER_001", numMoves = 2 }, -- BASIC/BASIC
			{ id = "MONSTER_002", numMoves = 1 }, -- BASIC/BASIC
			{ id = "MONSTER_003_1", numMoves = 1 }, -- BASIC/BASIC
			{ id = "MONSTER_004", numMoves = 2 }, -- BASIC/BASIC
			{ id = "MONSTER_005", numMoves = 1 }, -- BASIC/BASIC
			{ id = "MONSTER_006", numMoves = 2 }, -- BASIC/BASIC

			{ id = "MONSTER_010", numMoves = 2 }, -- BASIC/STAGE_1
			{ id = "MONSTER_012", numMoves = 2 }, -- BASIC/STAGE_1

			{ id = "MONSTER_011", numMoves = 1 }, -- STAGE_1/STAGE_1
			{ id = "MONSTER_013", numMoves = 2 }, -- STAGE_1/STAGE_1

			{ id = "MONSTER_014", numMoves = 2 }, -- BASIC/STAGE_2
			{ id = "MONSTER_015", numMoves = 1 }, -- BASIC/STAGE_2

			{ id = "MONSTER_016", numMoves = 2 }, -- STAGE_1/STAGE_2
			{ id = "MONSTER_017", numMoves = 1 }, -- STAGE_1/STAGE_2

			{ id = "MONSTER_018_1", numMoves = 2 }, -- STAGE_2/STAGE_2
			{ id = "MONSTER_019", numMoves = 0 }, -- STAGE_2/STAGE_2
		},
	},
	{
		name = "fully_random",
		module = "randomize_num_moves_by_stage_max_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "FULLY_RANDOM",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			{ id = "MONSTER_001", numMoves = 2 }, -- BASIC/BASIC
			{ id = "MONSTER_002", numMoves = 2 }, -- BASIC/BASIC
			{ id = "MONSTER_003_1", numMoves = 1 }, -- BASIC/BASIC
			{ id = "MONSTER_004", numMoves = 2 }, -- BASIC/BASIC
			{ id = "MONSTER_005", numMoves = 2 }, -- BASIC/BASIC
			{ id = "MONSTER_006", numMoves = 1 }, -- BASIC/BASIC

			{ id = "MONSTER_010", numMoves = 2 }, -- BASIC/STAGE_1
			{ id = "MONSTER_012", numMoves = 2 }, -- BASIC/STAGE_1

			{ id = "MONSTER_011", numMoves = 1 }, -- STAGE_1/STAGE_1
			{ id = "MONSTER_013", numMoves = 1 }, -- STAGE_1/STAGE_1

			{ id = "MONSTER_014", numMoves = 2 }, -- BASIC/STAGE_2
			{ id = "MONSTER_015", numMoves = 1 }, -- BASIC/STAGE_2

			{ id = "MONSTER_016", numMoves = 2 }, -- STAGE_1/STAGE_2
			{ id = "MONSTER_017", numMoves = 1 }, -- STAGE_1/STAGE_2

			{ id = "MONSTER_018_1", numMoves = 2 }, -- STAGE_2/STAGE_2
			{ id = "MONSTER_019", numMoves = 2 }, -- STAGE_2/STAGE_2
		},
	},
}
