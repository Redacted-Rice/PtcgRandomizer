local card_sets = require("support.card_sets")

local hpPool = { 10, 20, 30 }

return {
	{
		name = "minimize_repeats",
		module = "randomize_hp_custom_all_together",
		seed = 42,
		args = {
			approach = "MINIMIZE_REPEATS",
			hpPool = hpPool,
		},
		cards = card_sets.STD_TEST_CARDS_ROM,
		-- Pool consumed then refilled across the shared set. Values are evenly distributed
        -- (30 has one extra which is fine)
		expect = {
			{ id = "MONSTER_001", hp = 20 },
			{ id = "MONSTER_002", hp = 10 },
			{ id = "MONSTER_003_1", hp = 30 },
			{ id = "MONSTER_004", hp = 30 },
			{ id = "MONSTER_005", hp = 20 },
			{ id = "MONSTER_006", hp = 10 },
			{ id = "MONSTER_010", hp = 30 },
			{ id = "MONSTER_011", hp = 10 },
			{ id = "MONSTER_012", hp = 20 },
			{ id = "MONSTER_013", hp = 10 },
			{ id = "MONSTER_014", hp = 20 },
			{ id = "MONSTER_015", hp = 30 },
			{ id = "MONSTER_016", hp = 20 },
			{ id = "MONSTER_017", hp = 30 },
			{ id = "MONSTER_018_1", hp = 10 },
			{ id = "MONSTER_019", hp = 30 },
		},
	},
	{
		name = "fully_random",
		module = "randomize_hp_custom_all_together",
		-- seed 42 was nearly even (6/5/5). 41 lands 20 nine times and 30 only once showing
        -- its random and functioning as expected
		seed = 41,
		args = {
			approach = "FULLY_RANDOM",
			hpPool = hpPool,
		},
		cards = card_sets.STD_TEST_CARDS_ROM,
		expect = {
			{ id = "MONSTER_001", hp = 20 },
			{ id = "MONSTER_002", hp = 20 },
			{ id = "MONSTER_003_1", hp = 10 },
			{ id = "MONSTER_004", hp = 20 },
			{ id = "MONSTER_005", hp = 20 },
			{ id = "MONSTER_006", hp = 20 },
			{ id = "MONSTER_010", hp = 20 },
			{ id = "MONSTER_011", hp = 10 },
			{ id = "MONSTER_012", hp = 20 },
			{ id = "MONSTER_013", hp = 10 },
			{ id = "MONSTER_014", hp = 20 },
			{ id = "MONSTER_015", hp = 30 },
			{ id = "MONSTER_016", hp = 20 },
			{ id = "MONSTER_017", hp = 10 },
			{ id = "MONSTER_018_1", hp = 10 },
			{ id = "MONSTER_019", hp = 10 },
		},
	},
}
