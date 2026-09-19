local card_sets = require("support.card_sets")

local seed = 42
local romCards = card_sets.EVO_LINE_CARDS_ROM
local currentCards = card_sets.EVO_LINE_CARDS_CURRENT
local byStageAndMax = { withinType = false, grouping = "BY_STAGE_AND_MAX_STAGE", source = "ROM" }

local function caseFor(name, args, original, modified, expect, caseSeed)
	return {
		name = name,
		module = "evo_line_cards",
		seed = caseSeed or seed,
		args = args,
		original = original,
		modified = modified or original,
		expect = expect,
	}
end

return {
	caseFor(
		"by_stage_and_max_stage",
		{ withinType = false, grouping = "BY_STAGE_AND_MAX_STAGE", source = "ROM" },
		romCards,
		romCards,
		{
			{ id = "MONSTER_003_1", name = "FireABasic", stage = "BASIC", prevEvoName = "" }, -- basic / stage 2
			{ id = "MONSTER_003_2", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_038_1", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "FireABasic" }, -- stage 1 / stage 2
			{ id = "MONSTER_038_2", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },

			{ id = "MONSTER_025_1", name = "FireBBasic", stage = "BASIC", prevEvoName = "" }, -- basic / stage 2
			{ id = "MONSTER_025_2", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_3", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" }, -- stage 1 / stage 2
			{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },

			{ id = "MONSTER_092_1", name = "FireCBasic", stage = "BASIC", prevEvoName = "" }, -- basic / stage 1
			{ id = "MONSTER_092_2", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "FireCBasic" }, -- stage 1 / stage 1
			{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "FireCBasic" },

			{ id = "MONSTER_039_1", name = "WaterABasic", stage = "BASIC", prevEvoName = "" }, -- basic / stage 2
			{ id = "MONSTER_039_2", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_3", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_026_1", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" }, -- stage 1 / stage 2
			{ id = "MONSTER_026_2", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },
			{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },

			{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" }, -- basic / stage 1
			{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "WaterBBasic" }, -- stage 1 / stage 1
			{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "WaterBBasic" },

			{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" }, -- basic / stage 2
			{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" }, -- stage 1 / stage 2
			{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },

			-- Basic onlys not changed
			{ id = "MONSTER_080", name = "FireDBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_018_1", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_018_2", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_094", name = "WaterDBasic", stage = "BASIC", prevEvoName = "" },
		}
	),

	caseFor("by_stage", { withinType = false, grouping = "BY_STAGE", source = "ROM" }, romCards, romCards, {
		{ id = "MONSTER_003_1", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_003_2", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
		{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
		{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },
		{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },

		-- Now basic only
		{ id = "MONSTER_025_1", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_025_2", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_025_3", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },

		-- Now basic only
		{ id = "MONSTER_092_1", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_092_2", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },

		-- No longer basic only
		{ id = "MONSTER_080", name = "FireDBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_026_1", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireDBasic" },
		{ id = "MONSTER_026_2", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireDBasic" },
		{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },
		{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },

		{ id = "MONSTER_018_1", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_018_2", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },

		{ id = "MONSTER_039_1", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_039_2", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_039_3", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
		{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },

		{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "WaterBBasic" },
		{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "WaterBBasic" },
		{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireC2nd" },
		{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireC2nd" },

		{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
		{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },

		-- No longer basic only
		{ id = "MONSTER_094", name = "WaterDBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_038_1", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "WaterDBasic" },
		{ id = "MONSTER_038_2", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "WaterDBasic" },
		{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },
		{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },
	}),

	caseFor("all_together", { withinType = false, grouping = "ALL_TOGETHER", source = "ROM" }, romCards, romCards, {
		-- Still 3 basic only chains, 2 stage 1 max, and 4 stage 3 max chains
		{ id = "MONSTER_038_1", name = "FireA2nd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_038_2", name = "FireA2nd", stage = "BASIC", prevEvoName = "" },

		{ id = "MONSTER_025_1", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_025_2", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_025_3", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
		{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },

		{ id = "MONSTER_026_1", name = "FireB2nd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_026_2", name = "FireB2nd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_1", prevEvoName = "FireB2nd" },
		{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_1", prevEvoName = "FireB2nd" },

		{ id = "MONSTER_092_1", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_092_2", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_094", name = "WaterDBasic", stage = "STAGE_1", prevEvoName = "FireCBasic" },
		{ id = "MONSTER_018_1", name = "FireEBasic", stage = "STAGE_2", prevEvoName = "WaterDBasic" },
		{ id = "MONSTER_018_2", name = "FireEBasic", stage = "STAGE_2", prevEvoName = "WaterDBasic" },

		{ id = "MONSTER_080", name = "FireDBasic", stage = "BASIC", prevEvoName = "" },

		{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },

		{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_003_1", name = "FireABasic", stage = "STAGE_1", prevEvoName = "WaterC2nd" },
		{ id = "MONSTER_003_2", name = "FireABasic", stage = "STAGE_1", prevEvoName = "WaterC2nd" },
		{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "STAGE_2", prevEvoName = "FireABasic" },
		{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "STAGE_2", prevEvoName = "FireABasic" },
		{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "STAGE_2", prevEvoName = "FireABasic" },

		{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_039_1", name = "WaterABasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
		{ id = "MONSTER_039_2", name = "WaterABasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
		{ id = "MONSTER_039_3", name = "WaterABasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
		{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterABasic" },
		{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterABasic" },

		{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterA2nd" },
		{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterA2nd" },
		{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "WaterB2nd" },
		{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "WaterB2nd" },
	}),

	caseFor(
		"within_type_by_stage_and_max_stage",
		{ withinType = true, grouping = "BY_STAGE_AND_MAX_STAGE", source = "ROM" },
		romCards,
		romCards,
		{
			-- Fire A & B swapped evos. Rest happened to stay the same which is fine
			{ id = "MONSTER_003_1", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_003_2", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_026_1", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_026_2", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },

			{ id = "MONSTER_025_1", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_2", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_3", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_038_1", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_038_2", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },
			{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },

			{ id = "MONSTER_092_1", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_092_2", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "FireCBasic" },
			{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "FireCBasic" },

			{ id = "MONSTER_080", name = "FireDBasic", stage = "BASIC", prevEvoName = "" },

			{ id = "MONSTER_018_1", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_018_2", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },

			{ id = "MONSTER_039_1", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_2", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_3", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },

			{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterBBasic" },
			{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterBBasic" },

			{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },

			{ id = "MONSTER_094", name = "WaterDBasic", stage = "BASIC", prevEvoName = "" },
		}
	),

	caseFor("within_type", { withinType = true, grouping = "ALL_TOGETHER", source = "ROM" }, romCards, romCards, {
		-- Fire is only with fire and have same number of evo lines with same maxes
		{ id = "MONSTER_038_1", name = "FireA2nd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_038_2", name = "FireA2nd", stage = "BASIC", prevEvoName = "" },

		{ id = "MONSTER_025_1", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_025_2", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_025_3", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_018_1", name = "FireEBasic", stage = "STAGE_1", prevEvoName = "FireBBasic" },
		{ id = "MONSTER_018_2", name = "FireEBasic", stage = "STAGE_1", prevEvoName = "FireBBasic" },

		{ id = "MONSTER_026_1", name = "FireB2nd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_026_2", name = "FireB2nd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_003_1", name = "FireABasic", stage = "STAGE_1", prevEvoName = "FireB2nd" },
		{ id = "MONSTER_003_2", name = "FireABasic", stage = "STAGE_1", prevEvoName = "FireB2nd" },
		{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireABasic" },
		{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireABasic" },

		{ id = "MONSTER_093_1", name = "FireC2nd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_093_2", name = "FireC2nd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_080", name = "FireDBasic", stage = "STAGE_1", prevEvoName = "FireC2nd" },
		{ id = "MONSTER_092_1", name = "FireCBasic", stage = "STAGE_2", prevEvoName = "FireDBasic" },
		{ id = "MONSTER_092_2", name = "FireCBasic", stage = "STAGE_2", prevEvoName = "FireDBasic" },

		{ id = "MONSTER_059_1", name = "FireA3rd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_059_2", name = "FireA3rd", stage = "BASIC", prevEvoName = "" },

		-- Same for water
		{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
		{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
		{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
		{ id = "MONSTER_039_1", name = "WaterABasic", stage = "STAGE_2", prevEvoName = "WaterCBasic" },
		{ id = "MONSTER_039_2", name = "WaterABasic", stage = "STAGE_2", prevEvoName = "WaterCBasic" },
		{ id = "MONSTER_039_3", name = "WaterABasic", stage = "STAGE_2", prevEvoName = "WaterCBasic" },

		{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterA2nd" },
		{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterA2nd" },
		{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "STAGE_2", prevEvoName = "WaterB2nd" },
		{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "STAGE_2", prevEvoName = "WaterB2nd" },

		{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterA3rd" },
		{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterA3rd" },

		{ id = "MONSTER_094", name = "WaterDBasic", stage = "BASIC", prevEvoName = "" },
	}),

	caseFor(
		"from_current",
		{ withinType = false, grouping = "ALL_TOGETHER", source = "CURRENT" },
		romCards,
		currentCards,
		{
			-- All mixed up but same line types and kept branching evo
			{ id = "MONSTER_001", name = "GrassABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_005", name = "GrassB2nd", stage = "STAGE_1", prevEvoName = "GrassABasic" },

			{ id = "MONSTER_004", name = "GrassBBasic", stage = "BASIC", prevEvoName = "" },

			-- Kept a branching evo correctly
			{ id = "MONSTER_080", name = "FightABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_002", name = "GrassA2nd", stage = "STAGE_1", prevEvoName = "FightABasic" },
			{ id = "MONSTER_003_1", name = "GrassA3rd", stage = "STAGE_1", prevEvoName = "FightABasic" },

			{ id = "MONSTER_012", name = "FightBBasic", stage = "BASIC", prevEvoName = "" },

			{ id = "MONSTER_081_2", name = "FightABranchB", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_007", name = "GrassCBasic", stage = "STAGE_1", prevEvoName = "FightABranchB" },
			{ id = "MONSTER_081_1", name = "FightABranchA", stage = "STAGE_2", prevEvoName = "GrassCBasic" },
		}
	),

	-- 1 basic, 3 stage 1. All three stage 1 slots point back to the one basic.
	caseFor("prev_dist_1_to_3", byStageAndMax, card_sets.EVO_BRANCH_1_TO_3, card_sets.EVO_BRANCH_1_TO_3, {
		{ id = "MONSTER_100", name = "Branch1Basic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_102", name = "Branch1S1B", stage = "STAGE_1", prevEvoName = "Branch1Basic" },
		{ id = "MONSTER_103", name = "Branch1S1C", stage = "STAGE_1", prevEvoName = "Branch1Basic" },
		{ id = "MONSTER_101_1", name = "Branch1S1A", stage = "STAGE_1", prevEvoName = "Branch1Basic" },
	}),

	-- 2 basics, 3 stage 1. All stage 1 slots share prevEvoIdx 1 (first basic slot).
	caseFor("prev_dist_2_to_3", byStageAndMax, card_sets.EVO_BRANCH_2_TO_3, card_sets.EVO_BRANCH_2_TO_3, {
		{ id = "MONSTER_117", name = "Branch2BasicB", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_116", name = "Branch2BasicA", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_119", name = "Branch2S1B", stage = "STAGE_1", prevEvoName = "Branch2BasicB" },
		{ id = "MONSTER_120", name = "Branch2S1C", stage = "STAGE_1", prevEvoName = "Branch2BasicB" },
		{ id = "MONSTER_118", name = "Branch2S1A", stage = "STAGE_1", prevEvoName = "Branch2BasicB" },
	}),

	-- 1 basic, 3 stage 1, 2 stage 2. S1C is a tip; S2A/S2B keep distinct prev slots from the set.
	caseFor("prev_dist_3_to_2", byStageAndMax, card_sets.EVO_BRANCH_3_TO_2, card_sets.EVO_BRANCH_3_TO_2, {
		{ id = "MONSTER_121", name = "Branch3Basic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_130", name = "Branch3S1B", stage = "STAGE_1", prevEvoName = "Branch3Basic" },
		{ id = "MONSTER_131", name = "Branch3S1C", stage = "STAGE_1", prevEvoName = "Branch3Basic" },
		{ id = "MONSTER_129", name = "Branch3S1A", stage = "STAGE_1", prevEvoName = "Branch3Basic" },
		{ id = "MONSTER_139", name = "Branch3S2B", stage = "STAGE_2", prevEvoName = "Branch3S1A" },
		{ id = "MONSTER_138", name = "Branch3S2A", stage = "STAGE_2", prevEvoName = "Branch3S1B" },
	}),

	-- Proxy evolving from a basic must not swap with that basic or else it will refer to itself
	-- as the prev evo. Pool excludes proxy prev evos so we know the basic we select is safe
	caseFor(
		"all_together_proxy_not_self_prev",
		{ withinType = false, grouping = "ALL_TOGETHER", source = "ROM" },
		{
			{
				id = "MONSTER_001",
				name = "ProxyBasic",
				type = "MONSTER_FIRE",
				stage = "BASIC",
				prevEvoName = "",
				evoLineId = 1,
				evoLineMaxStage = "STAGE_1",
			},
			{
				id = "MONSTER_002",
				name = "ProxyDoll",
				type = "MONSTER_COLORLESS",
				stage = "STAGE_1",
				prevEvoName = "ProxyBasic",
				evoLineId = 1,
				evoLineMaxStage = "STAGE_1",
				isTrainerProxy = true,
			},
			{
				id = "MONSTER_003_1",
				name = "SafeBasic",
				type = "MONSTER_WATER",
				stage = "BASIC",
				prevEvoName = "",
				evoLineId = 2,
				evoLineMaxStage = "BASIC",
			},
		},
		nil,
		{
			-- seed 42: ProxyDoll stays/becomes basic. SafeBasic takes the stage-1 slot
			{ id = "MONSTER_002", name = "ProxyDoll", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_003_1", name = "SafeBasic", stage = "STAGE_1", prevEvoName = "ProxyBasic" },
			{ id = "MONSTER_001", name = "ProxyBasic", stage = "BASIC", prevEvoName = "" },
		}
	),
}
