local card_sets = require("support.card_sets")

local hpPools = {
	BASIC = { 10, 20, 30 },
	STAGE_1 = { 40, 50, 60 },
	STAGE_2 = { 70, 80, 90 },
}

return {
	{
		name = "minimize_repeats",
		module = "randomize_hp_custom_by_stage",
		seed = 42,
		args = {
			approach = "MINIMIZE_REPEATS",
			hpPools = hpPools,
		},
		cards = card_sets.STD_TEST_CARDS_ROM,
		expect = {
			{ id = "MONSTER_001", hp = 20 }, -- BASIC
			{ id = "MONSTER_002", hp = 30 }, -- BASIC
			{ id = "MONSTER_003_1", hp = 10 }, -- BASIC
			{ id = "MONSTER_004", hp = 20 }, -- BASIC
			{ id = "MONSTER_005", hp = 10 }, -- BASIC
			{ id = "MONSTER_006", hp = 30 }, -- BASIC
			{ id = "MONSTER_010", hp = 30 }, -- BASIC
			{ id = "MONSTER_012", hp = 20 }, -- BASIC
			{ id = "MONSTER_014", hp = 10 }, -- BASIC
			{ id = "MONSTER_015", hp = 30 }, -- BASIC

			{ id = "MONSTER_011", hp = 60 }, -- STAGE_1
			{ id = "MONSTER_013", hp = 50 }, -- STAGE_1
			{ id = "MONSTER_016", hp = 40 }, -- STAGE_1
			{ id = "MONSTER_017", hp = 40 }, -- STAGE_1

			{ id = "MONSTER_018_1", hp = 90 }, -- STAGE_2
			{ id = "MONSTER_019", hp = 70 }, -- STAGE_2
		},
	},
	{
		name = "fully_random",
		module = "randomize_hp_custom_by_stage",
		-- seed 42 was nearly even on BASIC. 53 lands 30 six times and 20 only once there.
		seed = 53,
		args = {
			approach = "FULLY_RANDOM",
			hpPools = hpPools,
		},
		cards = card_sets.STD_TEST_CARDS_ROM,
		expect = {
			{ id = "MONSTER_001", hp = 10 }, -- BASIC
			{ id = "MONSTER_002", hp = 10 }, -- BASIC
			{ id = "MONSTER_003_1", hp = 30 }, -- BASIC
			{ id = "MONSTER_004", hp = 30 }, -- BASIC
			{ id = "MONSTER_005", hp = 20 }, -- BASIC
			{ id = "MONSTER_006", hp = 30 }, -- BASIC
			{ id = "MONSTER_010", hp = 10 }, -- BASIC
			{ id = "MONSTER_012", hp = 30 }, -- BASIC
			{ id = "MONSTER_014", hp = 30 }, -- BASIC
			{ id = "MONSTER_015", hp = 30 }, -- BASIC

			{ id = "MONSTER_011", hp = 40 }, -- STAGE_1
			{ id = "MONSTER_013", hp = 40 }, -- STAGE_1
			{ id = "MONSTER_016", hp = 60 }, -- STAGE_1
			{ id = "MONSTER_017", hp = 60 }, -- STAGE_1

			{ id = "MONSTER_018_1", hp = 80 }, -- STAGE_2
			{ id = "MONSTER_019", hp = 80 }, -- STAGE_2
		},
	},
}
