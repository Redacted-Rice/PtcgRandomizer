-- Use the same seed on every case
local seed = 42
local card_sets = require("support.card_sets")

return {
	{
		name = "keep_duplicates",
		module = "hp_cards_stage_max_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			{ id = "MONSTER_001", hp = 50 }, -- BASIC/BASIC
			{ id = "MONSTER_002", hp = 60 }, -- BASIC/BASIC
			{ id = "MONSTER_003_1", hp = 40 }, -- BASIC/BASIC
			{ id = "MONSTER_004", hp = 70 }, -- BASIC/BASIC
			{ id = "MONSTER_005", hp = 40 }, -- BASIC/BASIC
			{ id = "MONSTER_006", hp = 80 }, -- BASIC/BASIC

			{ id = "MONSTER_010", hp = 30 }, -- BASIC/STAGE_1
			{ id = "MONSTER_012", hp = 40 }, -- BASIC/STAGE_1

			{ id = "MONSTER_011", hp = 80 }, -- STAGE_1/STAGE_1
			{ id = "MONSTER_013", hp = 50 }, -- STAGE_1/STAGE_1

			{ id = "MONSTER_014", hp = 40 }, -- BASIC/STAGE_2
			{ id = "MONSTER_015", hp = 90 }, -- BASIC/STAGE_2

			{ id = "MONSTER_016", hp = 50 }, -- STAGE_1/STAGE_2
			{ id = "MONSTER_017", hp = 30 }, -- STAGE_1/STAGE_2

			{ id = "MONSTER_018_1", hp = 110 }, -- STAGE_2/STAGE_2
			{ id = "MONSTER_019", hp = 60 }, -- STAGE_2/STAGE_2
		},
	},
	{
		name = "from_current",
		module = "hp_cards_stage_max_stage",
		seed = seed,
		args = {
			source = "CURRENT",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			{ id = "MONSTER_001", hp = 30 }, -- BASIC/BASIC
			{ id = "MONSTER_002", hp = 100 }, -- BASIC/BASIC
			{ id = "MONSTER_003_1", hp = 10 }, -- BASIC/BASIC
			{ id = "MONSTER_004", hp = 110 }, -- BASIC/BASIC
			{ id = "MONSTER_005", hp = 20 }, -- BASIC/BASIC
			{ id = "MONSTER_006", hp = 120 }, -- BASIC/BASIC

			{ id = "MONSTER_010", hp = 50 }, -- BASIC/STAGE_1
			{ id = "MONSTER_012", hp = 80 }, -- BASIC/STAGE_1

			{ id = "MONSTER_011", hp = 40 }, -- STAGE_1/STAGE_1
			{ id = "MONSTER_013", hp = 60 }, -- STAGE_1/STAGE_1

			{ id = "MONSTER_014", hp = 80 }, -- BASIC/STAGE_2
			{ id = "MONSTER_015", hp = 20 }, -- BASIC/STAGE_2

			{ id = "MONSTER_016", hp = 40 }, -- STAGE_1/STAGE_2
			{ id = "MONSTER_017", hp = 100 }, -- STAGE_1/STAGE_2

			{ id = "MONSTER_018_1", hp = 30 }, -- STAGE_2/STAGE_2
			{ id = "MONSTER_019", hp = 10 }, -- STAGE_2/STAGE_2
		},
	},
	{
		name = "remove_duplicates",
		module = "hp_cards_stage_max_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "REMOVE_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			{ id = "MONSTER_001", hp = 60 }, -- BASIC/BASIC
			{ id = "MONSTER_002", hp = 80 }, -- BASIC/BASIC
			{ id = "MONSTER_003_1", hp = 50 }, -- BASIC/BASIC
			{ id = "MONSTER_004", hp = 40 }, -- BASIC/BASIC
			{ id = "MONSTER_005", hp = 70 }, -- BASIC/BASIC
			{ id = "MONSTER_006", hp = 70 }, -- BASIC/BASIC

			{ id = "MONSTER_010", hp = 30 }, -- BASIC/STAGE_1
			{ id = "MONSTER_012", hp = 40 }, -- BASIC/STAGE_1

			{ id = "MONSTER_011", hp = 80 }, -- STAGE_1/STAGE_1
			{ id = "MONSTER_013", hp = 50 }, -- STAGE_1/STAGE_1

			{ id = "MONSTER_014", hp = 40 }, -- BASIC/STAGE_2
			{ id = "MONSTER_015", hp = 90 }, -- BASIC/STAGE_2

			{ id = "MONSTER_016", hp = 50 }, -- STAGE_1/STAGE_2
			{ id = "MONSTER_017", hp = 30 }, -- STAGE_1/STAGE_2

			{ id = "MONSTER_018_1", hp = 110 }, -- STAGE_2/STAGE_2
			{ id = "MONSTER_019", hp = 60 }, -- STAGE_2/STAGE_2
		},
	},
	{
		name = "fully_random",
		module = "hp_cards_stage_max_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "FULLY_RANDOM",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			{ id = "MONSTER_001", hp = 50 }, -- BASIC/BASIC
			{ id = "MONSTER_002", hp = 40 }, -- BASIC/BASIC
			{ id = "MONSTER_003_1", hp = 40 }, -- BASIC/BASIC
			{ id = "MONSTER_004", hp = 40 }, -- BASIC/BASIC
			{ id = "MONSTER_005", hp = 60 }, -- BASIC/BASIC
			{ id = "MONSTER_006", hp = 70 }, -- BASIC/BASIC

			{ id = "MONSTER_010", hp = 30 }, -- BASIC/STAGE_1
			{ id = "MONSTER_012", hp = 30 }, -- BASIC/STAGE_1

			{ id = "MONSTER_011", hp = 80 }, -- STAGE_1/STAGE_1
			{ id = "MONSTER_013", hp = 50 }, -- STAGE_1/STAGE_1

			{ id = "MONSTER_014", hp = 40 }, -- BASIC/STAGE_2
			{ id = "MONSTER_015", hp = 40 }, -- BASIC/STAGE_2

			{ id = "MONSTER_016", hp = 50 }, -- STAGE_1/STAGE_2
			{ id = "MONSTER_017", hp = 30 }, -- STAGE_1/STAGE_2

			{ id = "MONSTER_018_1", hp = 110 }, -- STAGE_2/STAGE_2
			{ id = "MONSTER_019", hp = 110 }, -- STAGE_2/STAGE_2
		},
	},
}
