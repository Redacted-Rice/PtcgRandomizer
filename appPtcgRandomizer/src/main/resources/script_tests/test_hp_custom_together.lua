local card_sets = require("support.card_sets")

-- repeats weight the pool. minimize_repeats pops each entry before refilling
local hpPool = { 10, 20, 20, 30, 30 }

return {
	{
		name = "minimize_repeats",
		module = "hp_custom_together",
		seed = 42,
		args = {
			approach = "MINIMIZE_REPEATS",
			hpPool = hpPool,
		},
		cards = card_sets.STD_TEST_CARDS_ROM,
		expect = {
			{ id = "MONSTER_001", hp = 30 },
			{ id = "MONSTER_002", hp = 20 },
			{ id = "MONSTER_003_1", hp = 30 },
			{ id = "MONSTER_004", hp = 10 },
			{ id = "MONSTER_005", hp = 20 },
			{ id = "MONSTER_006", hp = 30 },
			{ id = "MONSTER_010", hp = 10 },
			{ id = "MONSTER_011", hp = 20 },
			{ id = "MONSTER_012", hp = 20 },
			{ id = "MONSTER_013", hp = 30 },
			{ id = "MONSTER_014", hp = 20 },
			{ id = "MONSTER_015", hp = 30 },
			{ id = "MONSTER_016", hp = 20 },
			{ id = "MONSTER_017", hp = 10 },
			{ id = "MONSTER_018_1", hp = 30 },
			{ id = "MONSTER_019", hp = 30 },
		},
	},
	{
		name = "fully_random",
		module = "hp_custom_together",
		-- seed 42 was nearly even on the weighted pool. 41 skews toward 20
		seed = 41,
		args = {
			approach = "FULLY_RANDOM",
			hpPool = hpPool,
		},
		cards = card_sets.STD_TEST_CARDS_ROM,
		expect = {
			{ id = "MONSTER_001", hp = 20 },
			{ id = "MONSTER_002", hp = 30 },
			{ id = "MONSTER_003_1", hp = 30 },
			{ id = "MONSTER_004", hp = 20 },
			{ id = "MONSTER_005", hp = 30 },
			{ id = "MONSTER_006", hp = 10 },
			{ id = "MONSTER_010", hp = 30 },
			{ id = "MONSTER_011", hp = 20 },
			{ id = "MONSTER_012", hp = 30 },
			{ id = "MONSTER_013", hp = 10 },
			{ id = "MONSTER_014", hp = 20 },
			{ id = "MONSTER_015", hp = 30 },
			{ id = "MONSTER_016", hp = 10 },
			{ id = "MONSTER_017", hp = 30 },
			{ id = "MONSTER_018_1", hp = 10 },
			{ id = "MONSTER_019", hp = 20 },
		},
	},
}
