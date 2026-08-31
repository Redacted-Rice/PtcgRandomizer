-- Use the same seed on every case
local seed = 42
local card_sets = require("support.card_sets")

return {
	{
		name = "all_together",
		module = "shuffle_hp",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		-- Exactly match the usage in ROM
		expect = {
			{ id = "MONSTER_001", hp = 80 },
			{ id = "MONSTER_002", hp = 40 },
			{ id = "MONSTER_003_1", hp = 60 },
			{ id = "MONSTER_004", hp = 50 },
			{ id = "MONSTER_005", hp = 110 },
			{ id = "MONSTER_006", hp = 70 },
			{ id = "MONSTER_010", hp = 40 },
			{ id = "MONSTER_011", hp = 30 },
			{ id = "MONSTER_012", hp = 40 },
			{ id = "MONSTER_013", hp = 40 },
			{ id = "MONSTER_014", hp = 60 },
			{ id = "MONSTER_015", hp = 80 },
			{ id = "MONSTER_016", hp = 50 },
			{ id = "MONSTER_017", hp = 50 },
			{ id = "MONSTER_018_1", hp = 30 },
			{ id = "MONSTER_019", hp = 90 },
		},
	},
	{
		name = "by_stage",
		module = "shuffle_hp",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
			grouping = "BY_STAGE",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		-- Exactly match the usage in ROM by stage
		expect = {
			{ id = "MONSTER_001", hp = 40 }, -- BASIC
			{ id = "MONSTER_002", hp = 30 }, -- BASIC
			{ id = "MONSTER_003_1", hp = 70 }, -- BASIC
			{ id = "MONSTER_004", hp = 50 }, -- BASIC
			{ id = "MONSTER_005", hp = 80 }, -- BASIC
			{ id = "MONSTER_006", hp = 40 }, -- BASIC
			{ id = "MONSTER_010", hp = 90 }, -- BASIC
			{ id = "MONSTER_012", hp = 60 }, -- BASIC
			{ id = "MONSTER_014", hp = 40 }, -- BASIC
			{ id = "MONSTER_015", hp = 40 }, -- BASIC

			{ id = "MONSTER_011", hp = 50 }, -- STAGE_1
			{ id = "MONSTER_013", hp = 80 }, -- STAGE_1
			{ id = "MONSTER_016", hp = 50 }, -- STAGE_1
			{ id = "MONSTER_017", hp = 30 }, -- STAGE_1

			{ id = "MONSTER_018_1", hp = 110 }, -- STAGE_2
			{ id = "MONSTER_019", hp = 60 }, -- STAGE_2
		},
	},
	{
		name = "from_current",
		module = "shuffle_hp",
		seed = seed,
		args = {
			source = "CURRENT",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		-- Exactly match the usage in CURRENT by stage
		expect = {
			{ id = "MONSTER_001", hp = 40 },
			{ id = "MONSTER_002", hp = 10 },
			{ id = "MONSTER_003_1", hp = 10 },
			{ id = "MONSTER_004", hp = 40 },
			{ id = "MONSTER_005", hp = 30 },
			{ id = "MONSTER_006", hp = 110 },
			{ id = "MONSTER_010", hp = 20 },
			{ id = "MONSTER_011", hp = 50 },
			{ id = "MONSTER_012", hp = 80 },
			{ id = "MONSTER_013", hp = 80 },
			{ id = "MONSTER_014", hp = 100 },
			{ id = "MONSTER_015", hp = 120 },
			{ id = "MONSTER_016", hp = 60 },
			{ id = "MONSTER_017", hp = 30 },
			{ id = "MONSTER_018_1", hp = 100 },
			{ id = "MONSTER_019", hp = 20 },
		},
	},
	{
		name = "remove_duplicates",
		module = "shuffle_hp",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "REMOVE_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
        -- Each value used twice instead of the same as source
		expect = {
			{ id = "MONSTER_001", hp = 80 },
			{ id = "MONSTER_002", hp = 110 },
			{ id = "MONSTER_003_1", hp = 40 },
			{ id = "MONSTER_004", hp = 60 },
			{ id = "MONSTER_005", hp = 110 },
			{ id = "MONSTER_006", hp = 30 },
			{ id = "MONSTER_010", hp = 70 },
			{ id = "MONSTER_011", hp = 90 },
			{ id = "MONSTER_012", hp = 30 },
			{ id = "MONSTER_013", hp = 60 },
			{ id = "MONSTER_014", hp = 50 },
			{ id = "MONSTER_015", hp = 90 },
			{ id = "MONSTER_016", hp = 50 },
			{ id = "MONSTER_017", hp = 40 },
			{ id = "MONSTER_018_1", hp = 70 },
			{ id = "MONSTER_019", hp = 80 },
		},
	},
	{
		name = "fully_random",
		module = "shuffle_hp",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "FULLY_RANDOM",
			grouping = "ALL_TOGETHER",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
        -- See some missing and some over used as expected for FULL RANDOM
		expect = {
			{ id = "MONSTER_001", hp = 80 },
			{ id = "MONSTER_002", hp = 70 },
			{ id = "MONSTER_003_1", hp = 50 },
			{ id = "MONSTER_004", hp = 30 },
			{ id = "MONSTER_005", hp = 60 },
			{ id = "MONSTER_006", hp = 80 },
			{ id = "MONSTER_010", hp = 80 },
			{ id = "MONSTER_011", hp = 40 },
			{ id = "MONSTER_012", hp = 40 },
			{ id = "MONSTER_013", hp = 40 },
			{ id = "MONSTER_014", hp = 50 },
			{ id = "MONSTER_015", hp = 60 },
			{ id = "MONSTER_016", hp = 30 },
			{ id = "MONSTER_017", hp = 50 },
			{ id = "MONSTER_018_1", hp = 40 },
			{ id = "MONSTER_019", hp = 50 },
		},
	},
}
