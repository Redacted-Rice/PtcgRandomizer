-- Use the same seed on every case
local seed = 42
local card_sets = require("support.card_sets")

return {
	{
		name = "keep_duplicates",
		module = "shuffle_hp_by_stage_max_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
        -- These number match the ROM numbers as expected
		expect = {
			{ id = "MONSTER_001", hp = 50 }, -- BASIC/BASIC
			{ id = "MONSTER_002", hp = 80 }, -- BASIC/BASIC
			{ id = "MONSTER_003_1", hp = 60 }, -- BASIC/BASIC
			{ id = "MONSTER_004", hp = 40 }, -- BASIC/BASIC
			{ id = "MONSTER_005", hp = 70 }, -- BASIC/BASIC
			{ id = "MONSTER_006", hp = 40 }, -- BASIC/BASIC

			{ id = "MONSTER_010", hp = 30 }, -- BASIC/STAGE_1
			{ id = "MONSTER_012", hp = 40 }, -- BASIC/STAGE_1

			{ id = "MONSTER_011", hp = 80 }, -- STAGE_1/STAGE_1
			{ id = "MONSTER_013", hp = 50 }, -- STAGE_1/STAGE_1

			{ id = "MONSTER_014", hp = 40 }, -- BASIC/STAGE_2
			{ id = "MONSTER_015", hp = 90 }, -- BASIC/STAGE_2

			{ id = "MONSTER_016", hp = 30 }, -- STAGE_1/STAGE_2
			{ id = "MONSTER_017", hp = 50 }, -- STAGE_1/STAGE_2

			{ id = "MONSTER_018_1", hp = 110 }, -- STAGE_2/STAGE_2
			{ id = "MONSTER_019", hp = 60 }, -- STAGE_2/STAGE_2
		},
	},
	{
		name = "from_current",
		module = "shuffle_hp_by_stage_max_stage",
		seed = seed,
		args = {
			source = "CURRENT",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
        -- These number match the CURRENT numbers as expected which are already a bit crazy
		expect = {
			{ id = "MONSTER_001", hp = 30 }, -- BASIC/BASIC
			{ id = "MONSTER_002", hp = 120 }, -- BASIC/BASIC
			{ id = "MONSTER_003_1", hp = 100 }, -- BASIC/BASIC
			{ id = "MONSTER_004", hp = 20 }, -- BASIC/BASIC
			{ id = "MONSTER_005", hp = 110 }, -- BASIC/BASIC
			{ id = "MONSTER_006", hp = 10 }, -- BASIC/BASIC

			{ id = "MONSTER_010", hp = 50 }, -- BASIC/STAGE_1
			{ id = "MONSTER_012", hp = 80 }, -- BASIC/STAGE_1

			{ id = "MONSTER_011", hp = 40 }, -- STAGE_1/STAGE_1
			{ id = "MONSTER_013", hp = 60 }, -- STAGE_1/STAGE_1

			{ id = "MONSTER_014", hp = 80 }, -- BASIC/STAGE_2
			{ id = "MONSTER_015", hp = 20 }, -- BASIC/STAGE_2

			{ id = "MONSTER_016", hp = 100 }, -- STAGE_1/STAGE_2
			{ id = "MONSTER_017", hp = 40 }, -- STAGE_1/STAGE_2

			{ id = "MONSTER_018_1", hp = 30 }, -- STAGE_2/STAGE_2
			{ id = "MONSTER_019", hp = 10 }, -- STAGE_2/STAGE_2
		},
	},
	{
		name = "remove_duplicates",
		module = "shuffle_hp_by_stage_max_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "REMOVE_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
            -- 50 was selected twice instead of 40 when keeping duplicates. Others are unchanged
			{ id = "MONSTER_001", hp = 50 }, -- BASIC/BASIC
			{ id = "MONSTER_002", hp = 70 }, -- BASIC/BASIC
			{ id = "MONSTER_003_1", hp = 60 }, -- BASIC/BASIC
			{ id = "MONSTER_004", hp = 80 }, -- BASIC/BASIC
			{ id = "MONSTER_005", hp = 40 }, -- BASIC/BASIC
			{ id = "MONSTER_006", hp = 50 }, -- BASIC/BASIC

			{ id = "MONSTER_010", hp = 30 }, -- BASIC/STAGE_1
			{ id = "MONSTER_012", hp = 40 }, -- BASIC/STAGE_1

			{ id = "MONSTER_011", hp = 80 }, -- STAGE_1/STAGE_1
			{ id = "MONSTER_013", hp = 50 }, -- STAGE_1/STAGE_1

			{ id = "MONSTER_014", hp = 40 }, -- BASIC/STAGE_2
			{ id = "MONSTER_015", hp = 90 }, -- BASIC/STAGE_2

			{ id = "MONSTER_016", hp = 30 }, -- STAGE_1/STAGE_2
			{ id = "MONSTER_017", hp = 50 }, -- STAGE_1/STAGE_2

			{ id = "MONSTER_018_1", hp = 110 }, -- STAGE_2/STAGE_2
			{ id = "MONSTER_019", hp = 60 }, -- STAGE_2/STAGE_2
		},
	},
	{
		name = "fully_random",
		module = "shuffle_hp_by_stage_max_stage",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "FULLY_RANDOM",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
        -- Some values are missing and some are repeated as expected for full random
		expect = {
			{ id = "MONSTER_001", hp = 50 }, -- BASIC/BASIC
			{ id = "MONSTER_002", hp = 50 }, -- BASIC/BASIC
			{ id = "MONSTER_003_1", hp = 40 }, -- BASIC/BASIC
			{ id = "MONSTER_004", hp = 70 }, -- BASIC/BASIC
			{ id = "MONSTER_005", hp = 80 }, -- BASIC/BASIC
			{ id = "MONSTER_006", hp = 70 }, -- BASIC/BASIC

			{ id = "MONSTER_010", hp = 30 }, -- BASIC/STAGE_1
			{ id = "MONSTER_012", hp = 30 }, -- BASIC/STAGE_1

			{ id = "MONSTER_011", hp = 80 }, -- STAGE_1/STAGE_1
			{ id = "MONSTER_013", hp = 80 }, -- STAGE_1/STAGE_1

			{ id = "MONSTER_014", hp = 40 }, -- BASIC/STAGE_2
			{ id = "MONSTER_015", hp = 40 }, -- BASIC/STAGE_2

			{ id = "MONSTER_016", hp = 30 }, -- STAGE_1/STAGE_2
			{ id = "MONSTER_017", hp = 50 }, -- STAGE_1/STAGE_2

			{ id = "MONSTER_018_1", hp = 110 }, -- STAGE_2/STAGE_2
			{ id = "MONSTER_019", hp = 110 }, -- STAGE_2/STAGE_2
		},
	},
}
