-- Fix runs on CURRENT where the inversions and gaps live.
-- Branching case uses shared EVO_BRANCH_3_TO_2 (1 basic -> 3 stage 1 -> 2 stage 2).
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
	{
		-- Verifies depth first per branch dex assignment
		-- basic, then branch 1 (S1+S2), branch 2 (S1+S2), branch 3 (S1 tip).
		name = "reassign_depth_first_by_branch_3_to_2",
		module = "dex_number_fix_evo_line",
		args = {},
		cards = card_sets.EVO_BRANCH_3_TO_2,
		expect = {
			{ id = "MONSTER_121", dexNumber = 1 }, -- Branch3Basic
			{ id = "MONSTER_129", dexNumber = 2 }, -- Branch3S1A (branch 1)
			{ id = "MONSTER_138", dexNumber = 3 }, -- Branch3S2A (branch 1)
			{ id = "MONSTER_130", dexNumber = 4 }, -- Branch3S1B (branch 2)
			{ id = "MONSTER_139", dexNumber = 5 }, -- Branch3S2B (branch 2)
			{ id = "MONSTER_131", dexNumber = 6 }, -- Branch3S1C (branch 3 tip)
		},
	},
	{
		-- 1 basic -> 3 stage 1 tips. Branches sorted by id: 1 (S1B), 2 (S1C), 3 (S1A).
		name = "reassign_depth_first_by_branch_1_to_3",
		module = "dex_number_fix_evo_line",
		args = {},
		cards = card_sets.EVO_BRANCH_1_TO_3,
		expect = {
			{ id = "MONSTER_100", dexNumber = 1 }, -- Branch1Basic
			{ id = "MONSTER_102", dexNumber = 2 }, -- Branch1S1B (branch 1)
			{ id = "MONSTER_103", dexNumber = 3 }, -- Branch1S1C (branch 2)
			{ id = "MONSTER_101_1", dexNumber = 4 }, -- Branch1S1A (branch 3)
		},
	},
}
