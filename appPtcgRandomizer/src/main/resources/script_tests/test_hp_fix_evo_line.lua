-- Fix runs on CURRENT where the inversions live.
-- Branch cases use FIX_EVO_BRANCH_COMPLEX (1-3-0, 1-3-2, 1-2-4).
local card_sets = require("support.card_sets")

return {
	{
		name = "match_previous",
		module = "hp_fix_evo_line",
		args = {
			mode = "Match Previous",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			-- Solo basics unchanged
			{ id = "MONSTER_001", hp = 10 },
			{ id = "MONSTER_002", hp = 20 },
			{ id = "MONSTER_003_1", hp = 30 },
			{ id = "MONSTER_004", hp = 100 },
			{ id = "MONSTER_005", hp = 110 },
			{ id = "MONSTER_006", hp = 120 },

			-- Inverted: evo raised to match basic
			{ id = "MONSTER_010", hp = 80 },
			{ id = "MONSTER_011", hp = 80 },
			-- Ordered: unchanged
			{ id = "MONSTER_012", hp = 50 },
			{ id = "MONSTER_013", hp = 60 },
			-- Split: later stages raised to the highest earlier value seen
			{ id = "MONSTER_014", hp = 20 },
			{ id = "MONSTER_015", hp = 80 },
			{ id = "MONSTER_016", hp = 80 },
			{ id = "MONSTER_017", hp = 100 },
			{ id = "MONSTER_018_1", hp = 100 },
			{ id = "MONSTER_019", hp = 100 },
		},
	},
	{
		name = "redistribute",
		module = "hp_fix_evo_line",
		args = {
			mode = "Redistribute",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			-- Solo basics unchanged
			{ id = "MONSTER_001", hp = 10 },
			{ id = "MONSTER_002", hp = 20 },
			{ id = "MONSTER_003_1", hp = 30 },
			{ id = "MONSTER_004", hp = 100 },
			{ id = "MONSTER_005", hp = 110 },
			{ id = "MONSTER_006", hp = 120 },

			-- Inverted: swapped
			{ id = "MONSTER_010", hp = 40 },
			{ id = "MONSTER_011", hp = 80 },
			-- Ordered: unchanged
			{ id = "MONSTER_012", hp = 50 },
			{ id = "MONSTER_013", hp = 60 },
			-- Split: values redistributed up the line
			{ id = "MONSTER_014", hp = 20 },
			{ id = "MONSTER_015", hp = 10 },
			{ id = "MONSTER_016", hp = 30 },
			{ id = "MONSTER_017", hp = 40 },
			{ id = "MONSTER_018_1", hp = 100 },
			{ id = "MONSTER_019", hp = 80 },
		},
	},
	{
		name = "branch_individual_match_previous",
		module = "hp_fix_evo_line",
		args = {
			mode = "Match Previous",
			branchHandling = "Individual Branches",
		},
		original = card_sets.FIX_EVO_BRANCH_COMPLEX,
		modified = card_sets.FIX_EVO_BRANCH_COMPLEX,
		expect = {
			-- 1-3-0: each line raised to basic max (90)
			{ id = "MONSTER_101_1", hp = 80 }, 	-- b
			{ id = "MONSTER_101_2", hp = 90 }, 	-- b
			{ id = "MONSTER_102", hp = 90 },	-- 1a
			{ id = "MONSTER_105_1", hp = 100 },	-- 1b
			{ id = "MONSTER_105_2", hp = 90 },	-- 1b
			{ id = "MONSTER_104", hp = 90 },	-- 1c

			-- 1-3-2: 2b (70) stays below 1a (100)
			{ id = "MONSTER_121", hp = 70 },	-- b
			{ id = "MONSTER_122", hp = 50 },	-- b
			{ id = "MONSTER_123", hp = 100 },	-- 1a
			{ id = "MONSTER_124", hp = 70 },	-- 1a
			{ id = "MONSTER_125_1", hp = 100 },	-- 2a
			{ id = "MONSTER_125_2", hp = 100 },	-- 2a
			{ id = "MONSTER_126_1", hp = 70 },	-- 1b
			{ id = "MONSTER_127", hp = 70 },	-- 2b
			{ id = "MONSTER_128", hp = 70 },	-- 1c

			-- 1-2-4: 2ba (60) and 2bb (70) stay below 1a (100)
			{ id = "MONSTER_140", hp = 60 },	-- b
			{ id = "MONSTER_141", hp = 100 },	-- 1a
			{ id = "MONSTER_142", hp = 100 },	-- 2aa
			{ id = "MONSTER_143", hp = 100 },	-- 2ab
			{ id = "MONSTER_144_1", hp = 100 },	-- 2ab
			{ id = "MONSTER_145_1", hp = 60 },	-- 1b
			{ id = "MONSTER_145_2", hp = 60 },	-- 1b
			{ id = "MONSTER_146_1", hp = 60 },	-- 2ba
			{ id = "MONSTER_147", hp = 70 },	-- 2bb
		},
	},
	{
		name = "branch_all_together_match_previous",
		module = "hp_fix_evo_line",
		args = {
			mode = "Match Previous",
			branchHandling = "All Together",
		},
		original = card_sets.FIX_EVO_BRANCH_COMPLEX,
		modified = card_sets.FIX_EVO_BRANCH_COMPLEX,
		expect = {
			-- 1-3-0: same as individual case
			{ id = "MONSTER_101_1", hp = 80 },	-- b
			{ id = "MONSTER_101_2", hp = 90 },	-- b
			{ id = "MONSTER_102", hp = 90 },	-- 1a
			{ id = "MONSTER_105_1", hp = 100 },	-- 1b
			{ id = "MONSTER_105_2", hp = 90 },	-- 1b
			{ id = "MONSTER_104", hp = 90 },	-- 1c

			-- 1-3-2: All 1s are <= all 2s
			{ id = "MONSTER_121", hp = 70 },	-- b
			{ id = "MONSTER_122", hp = 50 },	-- b
			{ id = "MONSTER_123", hp = 100 },	-- 1a
			{ id = "MONSTER_124", hp = 70 },	-- 1a
			{ id = "MONSTER_125_1", hp = 100 },	-- 2a
			{ id = "MONSTER_125_2", hp = 100 },	-- 2a
			{ id = "MONSTER_126_1", hp = 70 },	-- 1b
			{ id = "MONSTER_127", hp = 100 },	-- 2b
			{ id = "MONSTER_128", hp = 70 },	-- 1c

			-- 1-2-4: All 1s are <= all 2s
			{ id = "MONSTER_140", hp = 60 },	-- b
			{ id = "MONSTER_141", hp = 100 },	-- 1a
			{ id = "MONSTER_142", hp = 100 },	-- 2aa
			{ id = "MONSTER_143", hp = 100 },	-- 2ab
			{ id = "MONSTER_144_1", hp = 100 },	-- 2ab
			{ id = "MONSTER_145_1", hp = 60 },	-- 1b
			{ id = "MONSTER_145_2", hp = 60 },	-- 1b
			{ id = "MONSTER_146_1", hp = 100 },	-- 2ba
			{ id = "MONSTER_147", hp = 100 },	-- 2bb
		},
	},
}
