-- Use the same seed on every case
local seed = 42
local card_sets = require("support.card_sets")

return {
	{
		name = "all_together",
		module = "hp_cards_together_and_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			{ id = "MONSTER_001", hp = 70 },
			{ id = "MONSTER_002", hp = 50 },
			{ id = "MONSTER_003_1", hp = 40 },
			{ id = "MONSTER_004", hp = 40 },
			{ id = "MONSTER_005", hp = 40 },
			{ id = "MONSTER_006", hp = 50 },
			{ id = "MONSTER_010", hp = 60 },
			{ id = "MONSTER_011", hp = 40 },
			{ id = "MONSTER_012", hp = 60 },
			{ id = "MONSTER_013", hp = 80 },
			{ id = "MONSTER_014", hp = 30 },
			{ id = "MONSTER_015", hp = 110 },
			{ id = "MONSTER_016", hp = 80 },
			{ id = "MONSTER_017", hp = 30 },
			{ id = "MONSTER_018_1", hp = 50 },
			{ id = "MONSTER_019", hp = 90 },
		},
	},
	{
		name = "by_stage",
		module = "hp_cards_together_and_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
			grouping = "BY_STAGE",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			{ id = "MONSTER_001", hp = 40 }, -- BASIC
			{ id = "MONSTER_002", hp = 40 }, -- BASIC
			{ id = "MONSTER_003_1", hp = 40 }, -- BASIC
			{ id = "MONSTER_004", hp = 70 }, -- BASIC
			{ id = "MONSTER_005", hp = 40 }, -- BASIC
			{ id = "MONSTER_006", hp = 30 }, -- BASIC
			{ id = "MONSTER_010", hp = 60 }, -- BASIC
			{ id = "MONSTER_012", hp = 50 }, -- BASIC
			{ id = "MONSTER_014", hp = 90 }, -- BASIC
			{ id = "MONSTER_015", hp = 80 }, -- BASIC

			{ id = "MONSTER_011", hp = 50 }, -- STAGE_1
			{ id = "MONSTER_013", hp = 30 }, -- STAGE_1
			{ id = "MONSTER_016", hp = 50 }, -- STAGE_1
			{ id = "MONSTER_017", hp = 80 }, -- STAGE_1

			{ id = "MONSTER_018_1", hp = 110 }, -- STAGE_2
			{ id = "MONSTER_019", hp = 60 }, -- STAGE_2
		},
	},
	{
		name = "from_current",
		module = "hp_cards_together_and_stage",
		seed = seed,
		args = {
			source = "CURRENT",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			{ id = "MONSTER_001", hp = 110 },
			{ id = "MONSTER_002", hp = 60 },
			{ id = "MONSTER_003_1", hp = 80 },
			{ id = "MONSTER_004", hp = 80 },
			{ id = "MONSTER_005", hp = 10 },
			{ id = "MONSTER_006", hp = 30 },
			{ id = "MONSTER_010", hp = 10 },
			{ id = "MONSTER_011", hp = 20 },
			{ id = "MONSTER_012", hp = 100 },
			{ id = "MONSTER_013", hp = 40 },
			{ id = "MONSTER_014", hp = 100 },
			{ id = "MONSTER_015", hp = 30 },
			{ id = "MONSTER_016", hp = 120 },
			{ id = "MONSTER_017", hp = 50 },
			{ id = "MONSTER_018_1", hp = 40 },
			{ id = "MONSTER_019", hp = 20 },
		},
	},
	{
		name = "remove_duplicates",
		module = "hp_cards_together_and_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "REMOVE_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			{ id = "MONSTER_001", hp = 60 },
			{ id = "MONSTER_002", hp = 80 },
			{ id = "MONSTER_003_1", hp = 90 },
			{ id = "MONSTER_004", hp = 90 },
			{ id = "MONSTER_005", hp = 40 },
			{ id = "MONSTER_006", hp = 50 },
			{ id = "MONSTER_010", hp = 40 },
			{ id = "MONSTER_011", hp = 70 },
			{ id = "MONSTER_012", hp = 30 },
			{ id = "MONSTER_013", hp = 110 },
			{ id = "MONSTER_014", hp = 50 },
			{ id = "MONSTER_015", hp = 110 },
			{ id = "MONSTER_016", hp = 60 },
			{ id = "MONSTER_017", hp = 80 },
			{ id = "MONSTER_018_1", hp = 70 },
			{ id = "MONSTER_019", hp = 30 },
		},
	},
	{
		name = "fully_random",
		module = "hp_cards_together_and_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "FULLY_RANDOM",
			grouping = "ALL_TOGETHER",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			{ id = "MONSTER_001", hp = 70 },
			{ id = "MONSTER_002", hp = 50 },
			{ id = "MONSTER_003_1", hp = 40 },
			{ id = "MONSTER_004", hp = 110 },
			{ id = "MONSTER_005", hp = 80 },
			{ id = "MONSTER_006", hp = 60 },
			{ id = "MONSTER_010", hp = 80 },
			{ id = "MONSTER_011", hp = 70 },
			{ id = "MONSTER_012", hp = 60 },
			{ id = "MONSTER_013", hp = 80 },
			{ id = "MONSTER_014", hp = 40 },
			{ id = "MONSTER_015", hp = 110 },
			{ id = "MONSTER_016", hp = 60 },
			{ id = "MONSTER_017", hp = 60 },
			{ id = "MONSTER_018_1", hp = 40 },
			{ id = "MONSTER_019", hp = 60 },
		},
	},
}
