-- Use the same seed on every case
local seed = 42
local card_sets = require("support.card_sets")

local hpPools = {
	BASIC = {
		BASIC = { 10, 20, 30 },
	},
	STAGE_1 = {
		BASIC = { 40, 50, 60 },
		STAGE_1 = { 50, 60, 70 },
	},
	STAGE_2 = {
		BASIC = { 60, 70, 80 },
		STAGE_1 = { 70, 80, 90 },
		STAGE_2 = { 80, 90, 100 },
	},
}

return {
	{
		name = "minimize_repeats",
		module = "randomize_hp_custom_by_stage_max_stage",
		seed = seed,
		args = {
			approach = "MINIMIZE_REPEATS",
			hpPools = hpPools,
		},
		cards = card_sets.STD_TEST_CARDS_ROM,
		-- Solo basics: pool consumed once then refilled. Other buckets size 2 in a 3-value pool.
        -- We see each basic/basic twice and do not see any repeates for others as expected
		expect = {
			{ id = "MONSTER_001", hp = 10 }, -- BASIC/BASIC
			{ id = "MONSTER_002", hp = 20 }, -- BASIC/BASIC
			{ id = "MONSTER_003_1", hp = 30 }, -- BASIC/BASIC
			{ id = "MONSTER_004", hp = 20 }, -- BASIC/BASIC
			{ id = "MONSTER_005", hp = 10 }, -- BASIC/BASIC
			{ id = "MONSTER_006", hp = 30 }, -- BASIC/BASIC

			{ id = "MONSTER_010", hp = 60 }, -- BASIC/STAGE_1
			{ id = "MONSTER_012", hp = 50 }, -- BASIC/STAGE_1

			{ id = "MONSTER_011", hp = 70 }, -- STAGE_1/STAGE_1
			{ id = "MONSTER_013", hp = 50 }, -- STAGE_1/STAGE_1

			{ id = "MONSTER_014", hp = 60 }, -- BASIC/STAGE_2
			{ id = "MONSTER_015", hp = 80 }, -- BASIC/STAGE_2

			{ id = "MONSTER_016", hp = 80 }, -- STAGE_1/STAGE_2
			{ id = "MONSTER_017", hp = 90 }, -- STAGE_1/STAGE_2

			{ id = "MONSTER_018_1", hp = 80 }, -- STAGE_2/STAGE_2
			{ id = "MONSTER_019", hp = 90 }, -- STAGE_2/STAGE_2
		},
	},
	{
		name = "fully_random",
		module = "randomize_hp_custom_by_stage_max_stage",
		seed = seed,
		args = {
			approach = "FULLY_RANDOM",
			hpPools = hpPools,
		},
		cards = card_sets.STD_TEST_CARDS_ROM,
		-- Solo basics: 10 landed four times and 30 didn't at all. We see some
        -- repeats in others which is expected
		expect = {
			{ id = "MONSTER_001", hp = 10 }, -- BASIC/BASIC
			{ id = "MONSTER_002", hp = 10 }, -- BASIC/BASIC
			{ id = "MONSTER_003_1", hp = 10 }, -- BASIC/BASIC
			{ id = "MONSTER_004", hp = 20 }, -- BASIC/BASIC
			{ id = "MONSTER_005", hp = 10 }, -- BASIC/BASIC
			{ id = "MONSTER_006", hp = 10 }, -- BASIC/BASIC

			{ id = "MONSTER_010", hp = 60 }, -- BASIC/STAGE_1
			{ id = "MONSTER_012", hp = 50 }, -- BASIC/STAGE_1

			{ id = "MONSTER_011", hp = 70 }, -- STAGE_1/STAGE_1
			{ id = "MONSTER_013", hp = 70 }, -- STAGE_1/STAGE_1

			{ id = "MONSTER_014", hp = 60 }, -- BASIC/STAGE_2
			{ id = "MONSTER_015", hp = 80 }, -- BASIC/STAGE_2

			{ id = "MONSTER_016", hp = 80 }, -- STAGE_1/STAGE_2
			{ id = "MONSTER_017", hp = 80 }, -- STAGE_1/STAGE_2

			{ id = "MONSTER_018_1", hp = 80 }, -- STAGE_2/STAGE_2
			{ id = "MONSTER_019", hp = 80 }, -- STAGE_2/STAGE_2
		},
	},
}
