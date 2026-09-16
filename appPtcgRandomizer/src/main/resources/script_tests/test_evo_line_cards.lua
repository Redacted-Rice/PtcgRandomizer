local card_sets = require("support.card_sets")

local seed = 42
local romCards = card_sets.EVO_LINE_CARDS_ROM
local currentCards = card_sets.EVO_LINE_CARDS_CURRENT

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
	caseFor("by_stage_and_max_stage", { withinType = false, grouping = "BY_STAGE_AND_MAX_STAGE", source = "ROM" },
		romCards, romCards, {
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
	}),

	caseFor("by_stage", { withinType = false, grouping = "BY_STAGE", source = "ROM" },
		romCards, romCards, {
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

	caseFor("all_together", { withinType = false, grouping = "ALL_TOGETHER", source = "ROM" },
		romCards, romCards, {
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

	caseFor("within_type_by_stage_and_max_stage",
		{ withinType = true, grouping = "BY_STAGE_AND_MAX_STAGE", source = "ROM" },
		romCards, romCards, {
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
		{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
		{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
		{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
		{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },

		{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterBBasic" },
		{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterBBasic" },

		{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
		{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
		{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },
		{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },

		{ id = "MONSTER_094", name = "WaterDBasic", stage = "BASIC", prevEvoName = "" },
	}),

	caseFor("within_type", { withinType = true, grouping = "ALL_TOGETHER", source = "ROM" },
		romCards, romCards, {
		-- Fire is only with fire and have same number of evo lines with same maxes
		{ id = "MONSTER_038_1", name = "FireA2nd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_038_2", name = "FireA2nd", stage = "BASIC", prevEvoName = "" },

		{ id = "MONSTER_025_1", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_025_2", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_025_3", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },

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

		{ id = "MONSTER_018_1", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_018_2", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_1", prevEvoName = "FireEBasic" },
		{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_1", prevEvoName = "FireEBasic" },

		-- Same for water
		{ id = "MONSTER_039_1", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_039_2", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_039_3", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },

		{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "STAGE_1", prevEvoName = "WaterA2nd" },
		{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "STAGE_1", prevEvoName = "WaterA2nd" },

		{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "STAGE_1", prevEvoName = "WaterA3rd" },
		{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "STAGE_1", prevEvoName = "WaterA3rd" },
		{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "STAGE_1", prevEvoName = "WaterA3rd" },
		{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_2", prevEvoName = "WaterCBasic" },
		{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_2", prevEvoName = "WaterCBasic" },

		{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "STAGE_1", prevEvoName = "WaterC2nd" },
		{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "STAGE_1", prevEvoName = "WaterC2nd" },
		{ id = "MONSTER_094", name = "WaterDBasic", stage = "STAGE_2", prevEvoName = "WaterC3rd" },
	}),

	caseFor("from_current", { withinType = false, grouping = "ALL_TOGETHER", source = "CURRENT" },
		romCards, currentCards, {
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
	}),
}
