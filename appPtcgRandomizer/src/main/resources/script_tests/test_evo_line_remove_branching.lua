local card_sets = require("support.card_sets")

return {
	{
		name = "removes_branches_from_fix_evo_branch_complex",
		module = "evo_line_remove_branching",
		args = {},
		cards = card_sets.FIX_EVO_BRANCH_COMPLEX,
		expectCards = {
			-- All evos end up as basics since there is no further evos
			{ id = "MONSTER_101_1", name = "1-3-basic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_101_2", name = "1-3-basic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_102", name = "1-3-s1a", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_105_1", name = "1-3-s1b", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_105_2", name = "1-3-s1b", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_104", name = "1-3-s1c", stage = "BASIC", prevEvoName = "" },
			-- All stage 1s become basics and the stage 2s become stage 1s and keep their prev evo links
			{ id = "MONSTER_121", name = "1-3-2-basic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_122", name = "1-3-2-basic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_123", name = "1-3-2-s1a", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_124", name = "1-3-2-s1a", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_125_1", name = "1-3-2-s2a", stage = "STAGE_1", prevEvoName = "1-3-2-s1a" },
			{ id = "MONSTER_125_2", name = "1-3-2-s2a", stage = "STAGE_1", prevEvoName = "1-3-2-s1a" },
			{ id = "MONSTER_126_1", name = "1-3-2-s1b", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_127", name = "1-3-2-s2b", stage = "STAGE_1", prevEvoName = "1-3-2-s1b" },
			{ id = "MONSTER_128", name = "1-3-2-s1c", stage = "BASIC", prevEvoName = "" },
			-- Everthing is a branch here so all end up basics
			{ id = "MONSTER_140", name = "1-2-4-basic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_141", name = "1-2-4-s1a", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_142", name = "1-2-4-s2aa", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_143", name = "1-2-4-s2ab", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_144_1", name = "1-2-4-s2ab", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_145_1", name = "1-2-4-s1b", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_145_2", name = "1-2-4-s1b", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_146_1", name = "1-2-4-s2ba", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_147", name = "1-2-4-s2bb", stage = "BASIC", prevEvoName = "" },
		},
	},
	{
		name = "leaves_non_branching_lines_in_evo_line_cards_current",
		module = "evo_line_remove_branching",
		args = {},
		cards = card_sets.EVO_LINE_CARDS_CURRENT,
		expectCards = {
			{ id = "MONSTER_001", name = "GrassABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_002", name = "GrassA2nd", stage = "STAGE_1", prevEvoName = "GrassABasic" },
			{ id = "MONSTER_003_1", name = "GrassA3rd", stage = "STAGE_2", prevEvoName = "GrassA2nd" },
			{ id = "MONSTER_004", name = "GrassBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_005", name = "GrassB2nd", stage = "STAGE_1", prevEvoName = "GrassBBasic" },
			{ id = "MONSTER_007", name = "GrassCBasic", stage = "BASIC", prevEvoName = "" },
			-- This branch is removed and results in basics
			{ id = "MONSTER_080", name = "FightABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_081_1", name = "FightABranchA", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_081_2", name = "FightABranchB", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_012", name = "FightBBasic", stage = "BASIC", prevEvoName = "" },
		},
	},
}
