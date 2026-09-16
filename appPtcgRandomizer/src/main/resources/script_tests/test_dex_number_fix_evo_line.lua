-- Fix runs on CURRENT where the inversions and gaps live.
local card_sets = require("support.card_sets")

return {
	{
		name = "reassign_sequential",
		module = "dex_number_fix_evo_line",
		args = {},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			-- Lines are resolved in order of their root basic's lowest current dex
			{ id = "MONSTER_001", dexNumber = 1 },
			{ id = "MONSTER_002", dexNumber = 2 },
			{ id = "MONSTER_003_1", dexNumber = 3 },

			-- OrderedBasic seed 50 comes before SplitBasic seed 60
			{ id = "MONSTER_012", dexNumber = 4 },
			{ id = "MONSTER_013", dexNumber = 5 },

			{ id = "MONSTER_014", dexNumber = 6 },
			{ id = "MONSTER_015", dexNumber = 6 },
			{ id = "MONSTER_016", dexNumber = 7 },
			{ id = "MONSTER_017", dexNumber = 7 },
			{ id = "MONSTER_018_1", dexNumber = 8 },
			{ id = "MONSTER_019", dexNumber = 8 },

			{ id = "MONSTER_004", dexNumber = 9 },
			{ id = "MONSTER_005", dexNumber = 10 },

			-- InvertedBasic seed 105
			{ id = "MONSTER_010", dexNumber = 11 },
			{ id = "MONSTER_011", dexNumber = 12 },

			{ id = "MONSTER_006", dexNumber = 13 },
		},
	},
}
