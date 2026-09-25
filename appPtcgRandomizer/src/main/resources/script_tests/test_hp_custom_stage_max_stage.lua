-- Use the same seed on every case
local seed = 42
local card_sets = require("support.card_sets")

local hpPools = {
	BASIC = {
		BASIC = {
			{ weight = 1, value = 10 },
			{ weight = 1, value = 20 },
			{ weight = 1, value = 30 },
		},
	},
	STAGE_1 = {
		BASIC = {
			{ weight = 1, value = 40 },
			{ weight = 1, value = 50 },
			{ weight = 1, value = 60 },
		},
		STAGE_1 = {
			{ weight = 1, value = 50 },
			{ weight = 1, value = 60 },
			{ weight = 1, value = 70 },
		},
	},
	STAGE_2 = {
		BASIC = {
			{ weight = 1, value = 60 },
			{ weight = 1, value = 70 },
			{ weight = 1, value = 80 },
		},
		STAGE_1 = {
			{ weight = 1, value = 70 },
			{ weight = 1, value = 80 },
			{ weight = 1, value = 90 },
		},
		STAGE_2 = {
			{ weight = 1, value = 80 },
			{ weight = 1, value = 90 },
			{ weight = 1, value = 100 },
		},
	},
}

return {
	{
		name = "minimize_repeats",
		module = "hp_custom_stage_max_stage",
		seed = seed,
		args = {
			approach = "MINIMIZE_REPEATS",
			hpPools = hpPools,
		},
		cards = card_sets.STD_TEST_CARDS_ROM,
		expect = {
			{ id = "MONSTER_001", hp = 30 }, -- BASIC/BASIC
			{ id = "MONSTER_002", hp = 10 }, -- BASIC/BASIC
			{ id = "MONSTER_003_1", hp = 20 }, -- BASIC/BASIC
			{ id = "MONSTER_004", hp = 30 }, -- BASIC/BASIC
			{ id = "MONSTER_005", hp = 20 }, -- BASIC/BASIC
			{ id = "MONSTER_006", hp = 10 }, -- BASIC/BASIC

			{ id = "MONSTER_010", hp = 50 }, -- BASIC/STAGE_1
			{ id = "MONSTER_012", hp = 60 }, -- BASIC/STAGE_1

			{ id = "MONSTER_011", hp = 50 }, -- STAGE_1/STAGE_1
			{ id = "MONSTER_013", hp = 60 }, -- STAGE_1/STAGE_1

			{ id = "MONSTER_014", hp = 60 }, -- BASIC/STAGE_2
			{ id = "MONSTER_015", hp = 80 }, -- BASIC/STAGE_2

			{ id = "MONSTER_016", hp = 70 }, -- STAGE_1/STAGE_2
			{ id = "MONSTER_017", hp = 90 }, -- STAGE_1/STAGE_2

			{ id = "MONSTER_018_1", hp = 80 }, -- STAGE_2/STAGE_2
			{ id = "MONSTER_019", hp = 100 }, -- STAGE_2/STAGE_2
		},
	},
	{
		name = "fully_random",
		module = "hp_custom_stage_max_stage",
		seed = seed,
		args = {
			approach = "FULLY_RANDOM",
			hpPools = hpPools,
		},
		cards = card_sets.STD_TEST_CARDS_ROM,
		expect = {
			{ id = "MONSTER_001", hp = 30 }, -- BASIC/BASIC
			{ id = "MONSTER_002", hp = 20 }, -- BASIC/BASIC
			{ id = "MONSTER_003_1", hp = 10 }, -- BASIC/BASIC
			{ id = "MONSTER_004", hp = 30 }, -- BASIC/BASIC
			{ id = "MONSTER_005", hp = 30 }, -- BASIC/BASIC
			{ id = "MONSTER_006", hp = 10 }, -- BASIC/BASIC

			{ id = "MONSTER_010", hp = 50 }, -- BASIC/STAGE_1
			{ id = "MONSTER_012", hp = 60 }, -- BASIC/STAGE_1

			{ id = "MONSTER_011", hp = 50 }, -- STAGE_1/STAGE_1
			{ id = "MONSTER_013", hp = 60 }, -- STAGE_1/STAGE_1

			{ id = "MONSTER_014", hp = 60 }, -- BASIC/STAGE_2
			{ id = "MONSTER_015", hp = 80 }, -- BASIC/STAGE_2

			{ id = "MONSTER_016", hp = 70 }, -- STAGE_1/STAGE_2
			{ id = "MONSTER_017", hp = 70 }, -- STAGE_1/STAGE_2

			{ id = "MONSTER_018_1", hp = 80 }, -- STAGE_2/STAGE_2
			{ id = "MONSTER_019", hp = 100 }, -- STAGE_2/STAGE_2
		},
	},
}
