-- Fix runs on CURRENT where the inversions live.
-- Branch cases use FIX_EVO_BRANCH_COMPLEX (1-3-0, 1-3-2, 1-2-4).
local card_sets = require("support.card_sets")

return {
	{
		name = "match_previous",
		module = "retreat_cost_fix_evo_line",
		args = {
			mode = "Match Previous",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			-- Solo basics unchanged
			{ id = "MONSTER_001", retreatCost = 0 },
			{ id = "MONSTER_002", retreatCost = 1 },
			{ id = "MONSTER_003_1", retreatCost = 1 },
			{ id = "MONSTER_004", retreatCost = 2 },
			{ id = "MONSTER_005", retreatCost = 1 },
			{ id = "MONSTER_006", retreatCost = 3 },

			-- Inverted: evo raised to match basic
			{ id = "MONSTER_010", retreatCost = 3 },
			{ id = "MONSTER_011", retreatCost = 3 },
			-- Ordered: unchanged
			{ id = "MONSTER_012", retreatCost = 1 },
			{ id = "MONSTER_013", retreatCost = 2 },
			-- Split: later stages raised to the highest earlier value seen
			{ id = "MONSTER_014", retreatCost = 0 },
			{ id = "MONSTER_015", retreatCost = 3 },
			{ id = "MONSTER_016", retreatCost = 3 },
			{ id = "MONSTER_017", retreatCost = 3 },
			{ id = "MONSTER_018_1", retreatCost = 3 },
			{ id = "MONSTER_019", retreatCost = 3 },
		},
	},
	{
		name = "redistribute",
		module = "retreat_cost_fix_evo_line",
		args = {
			mode = "Redistribute",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
			-- Solo basics unchanged
			{ id = "MONSTER_001", retreatCost = 0 },
			{ id = "MONSTER_002", retreatCost = 1 },
			{ id = "MONSTER_003_1", retreatCost = 1 },
			{ id = "MONSTER_004", retreatCost = 2 },
			{ id = "MONSTER_005", retreatCost = 1 },
			{ id = "MONSTER_006", retreatCost = 3 },

			-- Inverted: swapped
			{ id = "MONSTER_010", retreatCost = 1 },
			{ id = "MONSTER_011", retreatCost = 3 },
			-- Ordered: unchanged
			{ id = "MONSTER_012", retreatCost = 1 },
			{ id = "MONSTER_013", retreatCost = 2 },
			-- Split: values redistributed up the line
			{ id = "MONSTER_014", retreatCost = 0 },
			{ id = "MONSTER_015", retreatCost = 1 },
			{ id = "MONSTER_016", retreatCost = 1 },
			{ id = "MONSTER_017", retreatCost = 3 },
			{ id = "MONSTER_018_1", retreatCost = 3 },
			{ id = "MONSTER_019", retreatCost = 3 },
		},
	},
	{
		name = "branch_separate_match_previous",
		module = "retreat_cost_fix_evo_line",
		args = {
			mode = "Match Previous",
			branchHandling = "Separate Branches",
		},
		original = card_sets.FIX_EVO_BRANCH_COMPLEX,
		modified = card_sets.FIX_EVO_BRANCH_COMPLEX,
		expect = {
			-- 1-3-0: Branches alone. No change or continuity
			{ id = "MONSTER_101_1", retreatCost = 3 },	-- b
			{ id = "MONSTER_101_2", retreatCost = 2 },	-- b
			{ id = "MONSTER_102", retreatCost = 1 },	-- 1a
			{ id = "MONSTER_105_1", retreatCost = 3 },	-- 1b
			{ id = "MONSTER_105_2", retreatCost = 1 },	-- 1b
			{ id = "MONSTER_104", retreatCost = 0 },	-- 1c

			-- 1-3-2: 1 -> 2 should match previous. Others are unchanged
			-- 2a bumped up to 3
			-- 2b bumped up to 1
			{ id = "MONSTER_121", retreatCost = 2 },	-- b
			{ id = "MONSTER_122", retreatCost = 1 },	-- b
			{ id = "MONSTER_123", retreatCost = 3 },	-- 1a
			{ id = "MONSTER_124", retreatCost = 2 },	-- 1a
			{ id = "MONSTER_125_1", retreatCost = 3 },	-- 2a
			{ id = "MONSTER_125_2", retreatCost = 3 },	-- 2a
			{ id = "MONSTER_126_1", retreatCost = 1 },	-- 1b
			{ id = "MONSTER_127", retreatCost = 1 },	-- 2b
			{ id = "MONSTER_128", retreatCost = 2 },	-- 1c

			-- 1-2-4: Each branch is alone again. No changes or continuity
			{ id = "MONSTER_140", retreatCost = 1 },	-- b
			{ id = "MONSTER_141", retreatCost = 3 },	-- 1a
			{ id = "MONSTER_142", retreatCost = 0 },	-- 2aa
			{ id = "MONSTER_143", retreatCost = 2 },	-- 2ab
			{ id = "MONSTER_144_1", retreatCost = 1 },	-- 2ab
			{ id = "MONSTER_145_1", retreatCost = 1 },	-- 1b
			{ id = "MONSTER_145_2", retreatCost = 0 },	-- 1b
			{ id = "MONSTER_146_1", retreatCost = 3 },	-- 2ba
			{ id = "MONSTER_147", retreatCost = 0 },	-- 2bb
		},
	},
	{
		name = "branch_individual_match_previous",
		module = "retreat_cost_fix_evo_line",
		args = {
			mode = "Match Previous",
			branchHandling = "Individual Branches",
		},
		original = card_sets.FIX_EVO_BRANCH_COMPLEX,
		modified = card_sets.FIX_EVO_BRANCH_COMPLEX,
		expect = {
			-- 1-3-0: All raised to basic max (3)
			{ id = "MONSTER_101_1", retreatCost = 3 },	-- b
			{ id = "MONSTER_101_2", retreatCost = 2 },	-- b
			{ id = "MONSTER_102", retreatCost = 3 },	-- 1a
			{ id = "MONSTER_105_1", retreatCost = 3 },	-- 1b
			{ id = "MONSTER_105_2", retreatCost = 3 },	-- 1b
			{ id = "MONSTER_104", retreatCost = 3 },	-- 1c

			-- 1-3-2: all 1s raised (2)
			-- 2a bumped to 3s to match 1a max (3)
			-- 2b stayed at 2
			{ id = "MONSTER_121", retreatCost = 2 },	-- b
			{ id = "MONSTER_122", retreatCost = 1 },	-- b
			{ id = "MONSTER_123", retreatCost = 3 },	-- 1a
			{ id = "MONSTER_124", retreatCost = 2 },	-- 1a
			{ id = "MONSTER_125_1", retreatCost = 3 },	-- 2a
			{ id = "MONSTER_125_2", retreatCost = 3 },	-- 2a
			{ id = "MONSTER_126_1", retreatCost = 2 },	-- 1b
			{ id = "MONSTER_127", retreatCost = 2 },	-- 2b
			{ id = "MONSTER_128", retreatCost = 2 },	-- 1c

			-- 1-2-4: all 1s at least 1
			-- 2as bumped to 3s to match 1a max (3)
			-- 2bs bumped to 1s to match 2a max (1)
			{ id = "MONSTER_140", retreatCost = 1 },	-- b
			{ id = "MONSTER_141", retreatCost = 3 },	-- 1a
			{ id = "MONSTER_142", retreatCost = 3 },	-- 2aa
			{ id = "MONSTER_143", retreatCost = 3 },	-- 2ab
			{ id = "MONSTER_144_1", retreatCost = 3 },	-- 2ab
			{ id = "MONSTER_145_1", retreatCost = 1 },	-- 1b
			{ id = "MONSTER_145_2", retreatCost = 1 },	-- 1b
			{ id = "MONSTER_146_1", retreatCost = 3 },	-- 2ba
			{ id = "MONSTER_147", retreatCost = 1 },	-- 2bb
		},
	},
	{
		name = "branch_all_together_match_previous",
		module = "retreat_cost_fix_evo_line",
		args = {
			mode = "Match Previous",
			branchHandling = "All Together",
		},
		original = card_sets.FIX_EVO_BRANCH_COMPLEX,
		modified = card_sets.FIX_EVO_BRANCH_COMPLEX,
		expect = {
			-- 1-3-0: All raised to match basic max (3)
			{ id = "MONSTER_101_1", retreatCost = 3 },	-- b
			{ id = "MONSTER_101_2", retreatCost = 2 },	-- b
			{ id = "MONSTER_102", retreatCost = 3 },	-- 1a
			{ id = "MONSTER_105_1", retreatCost = 3 },	-- 1b
			{ id = "MONSTER_105_2", retreatCost = 3 },	-- 1b
			{ id = "MONSTER_104", retreatCost = 3 },	-- 1c

			-- 1-3-2: all stage 1s >= b max (2), all 2s >= 1a max (3)
			{ id = "MONSTER_121", retreatCost = 2 },	-- b
			{ id = "MONSTER_122", retreatCost = 1 },	-- b
			{ id = "MONSTER_123", retreatCost = 3 },	-- 1a
			{ id = "MONSTER_124", retreatCost = 2 },	-- 1a
			{ id = "MONSTER_125_1", retreatCost = 3 },	-- 2a
			{ id = "MONSTER_125_2", retreatCost = 3 },	-- 2a
			{ id = "MONSTER_126_1", retreatCost = 2 },	-- 1b
			{ id = "MONSTER_127", retreatCost = 3 },	-- 2b
			{ id = "MONSTER_128", retreatCost = 2 },	-- 1c

			-- 1-2-4: all stage 1s >= b max (1), all 2s >= 1a max (3)
			{ id = "MONSTER_140", retreatCost = 1 },	-- b
			{ id = "MONSTER_141", retreatCost = 3 },	-- 1a
			{ id = "MONSTER_142", retreatCost = 3 },	-- 2aa
			{ id = "MONSTER_143", retreatCost = 3 },	-- 2ab
			{ id = "MONSTER_144_1", retreatCost = 3 },	-- 2ab
			{ id = "MONSTER_145_1", retreatCost = 1 },	-- 1b
			{ id = "MONSTER_145_2", retreatCost = 1 },	-- 1b
			{ id = "MONSTER_146_1", retreatCost = 3 },	-- 2ba
			{ id = "MONSTER_147", retreatCost = 3 },	-- 2bb
		},
	},
	{
		name = "branch_individual_redistribute",
		module = "retreat_cost_fix_evo_line",
		args = {
			mode = "Redistribute",
			branchHandling = "Individual Branches",
		},
		original = card_sets.FIX_EVO_BRANCH_COMPLEX,
		modified = card_sets.FIX_EVO_BRANCH_COMPLEX,
		expect = {
			-- 1-3-0: Lowest swapped to basic. 1s are >= b max (0)
			{ id = "MONSTER_101_1", retreatCost = 0 },	-- b
			{ id = "MONSTER_101_2", retreatCost = 1 },	-- b
			{ id = "MONSTER_102", retreatCost = 1 },	-- 1a
			{ id = "MONSTER_105_1", retreatCost = 3 },	-- 1b
			{ id = "MONSTER_105_2", retreatCost = 2 },	-- 1b
			{ id = "MONSTER_104", retreatCost = 3 },	-- 1c

			-- 1-3-2: Each branch individually increases each stage
			{ id = "MONSTER_121", retreatCost = 0 },	-- b
			{ id = "MONSTER_122", retreatCost = 0 },	-- b
			{ id = "MONSTER_123", retreatCost = 2 },	-- 1a
			{ id = "MONSTER_124", retreatCost = 1 },	-- 1a
			{ id = "MONSTER_125_1", retreatCost = 3 },	-- 2a
			{ id = "MONSTER_125_2", retreatCost = 2 },	-- 2a
			{ id = "MONSTER_126_1", retreatCost = 1 },	-- 1b
			{ id = "MONSTER_127", retreatCost = 1 },	-- 2b
			{ id = "MONSTER_128", retreatCost = 2 },	-- 1c

			-- 1-2-4: Each branch individually increases each stage
			{ id = "MONSTER_140", retreatCost = 0 },	-- b
			{ id = "MONSTER_141", retreatCost = 0 },	-- 1a
			{ id = "MONSTER_142", retreatCost = 1 },	-- 2aa
			{ id = "MONSTER_143", retreatCost = 2 },	-- 2ab
			{ id = "MONSTER_144_1", retreatCost = 3 },	-- 2ab
			{ id = "MONSTER_145_1", retreatCost = 0 },	-- 1b
			{ id = "MONSTER_145_2", retreatCost = 1 },	-- 1b
			{ id = "MONSTER_146_1", retreatCost = 3 },	-- 2ba
			{ id = "MONSTER_147", retreatCost = 1 },	-- 2bb
		},
	},
	{
		name = "branch_all_together_redistribute",
		module = "retreat_cost_fix_evo_line",
		args = {
			mode = "Redistribute",
			branchHandling = "All Together",
		},
		original = card_sets.FIX_EVO_BRANCH_COMPLEX,
		modified = card_sets.FIX_EVO_BRANCH_COMPLEX,
		expect = {
			-- 1-3-0: All basics are all <= stage 1s
			{ id = "MONSTER_101_1", retreatCost = 0 },	-- b
			{ id = "MONSTER_101_2", retreatCost = 1 },	-- b
			{ id = "MONSTER_102", retreatCost = 2 },	-- 1a
			{ id = "MONSTER_105_1", retreatCost = 3 },	-- 1b
			{ id = "MONSTER_105_2", retreatCost = 1 },	-- 1b
			{ id = "MONSTER_104", retreatCost = 3 },	-- 1c

			-- 1-3-2: All basics are all <= stage 1s and all stage 1s are all <= stage 2s
			{ id = "MONSTER_121", retreatCost = 0 },	-- b
			{ id = "MONSTER_122", retreatCost = 0 },	-- b
			{ id = "MONSTER_123", retreatCost = 1 },	-- 1a
			{ id = "MONSTER_124", retreatCost = 1 },	-- 1a
			{ id = "MONSTER_125_1", retreatCost = 2 },	-- 2a
			{ id = "MONSTER_125_2", retreatCost = 2 },	-- 2a
			{ id = "MONSTER_126_1", retreatCost = 1 },	-- 1b
			{ id = "MONSTER_127", retreatCost = 3 },	-- 2b
			{ id = "MONSTER_128", retreatCost = 2 },	-- 1c

			-- 1-2-4: cross-branch swaps; 1a and 1b reprints differ from individual
			{ id = "MONSTER_140", retreatCost = 0 },	-- b
			{ id = "MONSTER_141", retreatCost = 0 },	-- 1a
			{ id = "MONSTER_142", retreatCost = 3 },	-- 2aa
			{ id = "MONSTER_143", retreatCost = 2 },	-- 2ab
			{ id = "MONSTER_144_1", retreatCost = 1 },	-- 2ab
			{ id = "MONSTER_145_1", retreatCost = 0 },	-- 1b
			{ id = "MONSTER_145_2", retreatCost = 1 },	-- 1b
			{ id = "MONSTER_146_1", retreatCost = 3 },	-- 2ba
			{ id = "MONSTER_147", retreatCost = 1 },	-- 2bb
		},
	},
	{
		name = "branch_separate_redistribute",
		module = "retreat_cost_fix_evo_line",
		args = {
			mode = "Redistribute",
			branchHandling = "Separate Branches",
		},
		original = card_sets.FIX_EVO_BRANCH_COMPLEX,
		modified = card_sets.FIX_EVO_BRANCH_COMPLEX,
		expect = {
			-- 1-3-0: Branches alone. No change or continuity
			{ id = "MONSTER_101_1", retreatCost = 3 },	-- b
			{ id = "MONSTER_101_2", retreatCost = 2 },	-- b
			{ id = "MONSTER_102", retreatCost = 1 },	-- 1a
			{ id = "MONSTER_105_1", retreatCost = 3 },	-- 1b
			{ id = "MONSTER_105_2", retreatCost = 1 },	-- 1b
			{ id = "MONSTER_104", retreatCost = 0 },	-- 1c

			-- 1-3-2: No continuity between basic and stage 1s
			-- 1 & 2s are in order though
			{ id = "MONSTER_121", retreatCost = 2 },	-- b
			{ id = "MONSTER_122", retreatCost = 1 },	-- b
			{ id = "MONSTER_123", retreatCost = 0 },	-- 1a
			{ id = "MONSTER_124", retreatCost = 1 },	-- 1a
			{ id = "MONSTER_125_1", retreatCost = 3 },	-- 2a
			{ id = "MONSTER_125_2", retreatCost = 2 },	-- 2a
			{ id = "MONSTER_126_1", retreatCost = 0 },	-- 1b
			{ id = "MONSTER_127", retreatCost = 1 },	-- 2b
			{ id = "MONSTER_128", retreatCost = 2 },	-- 1c

			-- 1-2-4: Branches alone. No change or continuity
			{ id = "MONSTER_140", retreatCost = 1 },	-- b
			{ id = "MONSTER_141", retreatCost = 3 },	-- 1a
			{ id = "MONSTER_142", retreatCost = 0 },	-- 2aa
			{ id = "MONSTER_143", retreatCost = 2 },	-- 2ab
			{ id = "MONSTER_144_1", retreatCost = 1 },	-- 2ab
			{ id = "MONSTER_145_1", retreatCost = 1 },	-- 1b
			{ id = "MONSTER_145_2", retreatCost = 0 },	-- 1b
			{ id = "MONSTER_146_1", retreatCost = 3 },	-- 2ba
			{ id = "MONSTER_147", retreatCost = 0 },	-- 2bb
		},
	},
}
