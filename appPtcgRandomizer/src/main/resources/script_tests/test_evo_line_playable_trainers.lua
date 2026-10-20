local card_sets = require("support.card_sets")

local function cardsWithTrainerProxies(cards, rootIds)
	local roots = {}
	for _, id in ipairs(rootIds) do
		roots[id] = true
	end
	local copy = {}
	for _, card in ipairs(cards) do
		local row = {}
		for key, value in pairs(card) do
			row[key] = value
		end
		if roots[row.id] then
			row.isTrainerProxy = true
		end
		table.insert(copy, row)
	end
	return copy
end

return {
	{
		name = "demotes_evos_under_proxies_on_fix_evo_branch_complex",
		module = "evo_line_playable_trainers",
		args = {},
		cards = cardsWithTrainerProxies(card_sets.FIX_EVO_BRANCH_COMPLEX, {
			"MONSTER_101_1",
			"MONSTER_121",
			"MONSTER_140",
		}),
		expect = {
			-- Evos of this proxy become basics and there is no evos so they all are basic
			{ id = "MONSTER_101_1", name = "1-3-basic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_101_2", name = "1-3-basic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_102", name = "1-3-s1a", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_105_1", name = "1-3-s1b", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_105_2", name = "1-3-s1b", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_104", name = "1-3-s1c", stage = "BASIC", prevEvoName = "" },
			-- Evos become basic and the stage 1s become stage 2s and keep their prev evo links
			{ id = "MONSTER_121", name = "1-3-2-basic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_122", name = "1-3-2-basic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_123", name = "1-3-2-s1a", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_124", name = "1-3-2-s1a", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_125_1", name = "1-3-2-s2a", stage = "STAGE_1", prevEvoName = "1-3-2-s1a" },
			{ id = "MONSTER_125_2", name = "1-3-2-s2a", stage = "STAGE_1", prevEvoName = "1-3-2-s1a" },
			{ id = "MONSTER_126_1", name = "1-3-2-s1b", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_127", name = "1-3-2-s2b", stage = "STAGE_1", prevEvoName = "1-3-2-s1b" },
			{ id = "MONSTER_128", name = "1-3-2-s1c", stage = "BASIC", prevEvoName = "" },
			-- Root proxy is removed and results in two separate 1-2-0 branches
			{ id = "MONSTER_140", name = "1-2-4-basic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_141", name = "1-2-4-s1a", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_142", name = "1-2-4-s2aa", stage = "STAGE_1", prevEvoName = "1-2-4-s1a" },
			{ id = "MONSTER_143", name = "1-2-4-s2ab", stage = "STAGE_1", prevEvoName = "1-2-4-s1a" },
			{ id = "MONSTER_144_1", name = "1-2-4-s2ab", stage = "STAGE_1", prevEvoName = "1-2-4-s1a" },
			{ id = "MONSTER_145_1", name = "1-2-4-s1b", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_145_2", name = "1-2-4-s1b", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_146_1", name = "1-2-4-s2ba", stage = "STAGE_1", prevEvoName = "1-2-4-s1b" },
			{ id = "MONSTER_147", name = "1-2-4-s2bb", stage = "STAGE_1", prevEvoName = "1-2-4-s1b" },
		},
	},
	{
		name = "leaves_non_branching_lines_in_evo_line_cards_current",
		module = "evo_line_playable_trainers",
		args = {},
		cards = cardsWithTrainerProxies(card_sets.EVO_LINE_CARDS_CURRENT, {
			"MONSTER_080",
		}),
		expect = {
			{ id = "MONSTER_001", name = "GrassABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_002", name = "GrassA2nd", stage = "STAGE_1", prevEvoName = "GrassABasic" },
			{ id = "MONSTER_003_1", name = "GrassA3rd", stage = "STAGE_2", prevEvoName = "GrassA2nd" },
			{ id = "MONSTER_004", name = "GrassBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_005", name = "GrassB2nd", stage = "STAGE_1", prevEvoName = "GrassBBasic" },
			{ id = "MONSTER_007", name = "GrassCBasic", stage = "BASIC", prevEvoName = "" },
			-- This proxy is removed and results in all basics
			{ id = "MONSTER_080", name = "FightABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_081_1", name = "FightABranchA", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_081_2", name = "FightABranchB", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_012", name = "FightBBasic", stage = "BASIC", prevEvoName = "" },
		},
	},
}
