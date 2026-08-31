-- Fix runs on CURRENT where the inversions live.
local card_sets = require("support.card_sets")

return {
	{
		name = "raise_minimums",
		module = "fix_evo_line_hp",
		args = {
			mode = "Raise Minimums",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
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
		module = "fix_evo_line_hp",
		args = {
			mode = "Redistribute",
		},
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = {
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
}
