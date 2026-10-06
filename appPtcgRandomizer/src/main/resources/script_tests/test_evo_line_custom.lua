local card_sets = require("support.card_sets")

local seed = 42
local romCards = card_sets.EVO_LINE_CARDS_ROM
local currentCards = card_sets.EVO_LINE_CARDS_CURRENT

-- Representative mix of line shapes but small enough to exhaust for our tests
local defaultEvoLinePool = {
	{ weight = 1, evoLine = { BASIC = 1, STAGE_1 = 0, STAGE_2 = 0 } },
	{ weight = 1, evoLine = { BASIC = 1, STAGE_1 = 1, STAGE_2 = 0 } },
	{ weight = 1, evoLine = { BASIC = 1, STAGE_1 = 1, STAGE_2 = 1 } },
	{ weight = 1, evoLine = { BASIC = 1, STAGE_1 = 3, STAGE_2 = 0 } },
	{ weight = 1, evoLine = { BASIC = 1, STAGE_1 = 3, STAGE_2 = 2 } },
}

local function caseFor(name, args, original, modified, expect, caseSeed)
	return {
		name = name,
		module = "evo_line_custom",
		seed = caseSeed or seed,
		args = args,
		original = original,
		modified = modified or original,
		expect = expect,
	}
end

return {
	caseFor(
		"all_together_minimize_repeats",
		{
			source = "ROM",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
			withinType = false,
			evoLinePool = defaultEvoLinePool,
		},
		romCards,
		romCards,
		{
			-- shape: [1-3-2]
			-- Has several cards with changed stages as expected and mixed types
			{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_038_1", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_038_2", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_2", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_2", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_1", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_1", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_2", prevEvoName = "FireB3rd" },
			{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_2", prevEvoName = "FireB3rd" },
			{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "STAGE_1", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "STAGE_1", prevEvoName = "WaterC2nd" },

			-- shape: [1-1-1]
			{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_1", name = "FireBBasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_025_2", name = "FireBBasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_025_3", name = "FireBBasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireBBasic" },

			-- shape: [1-1-0]
			{ id = "MONSTER_018_1", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_018_2", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "STAGE_1", prevEvoName = "FireEBasic" },
			{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "STAGE_1", prevEvoName = "FireEBasic" },
			{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "STAGE_1", prevEvoName = "FireEBasic" },

			-- shape: [1-3-0]
			{ id = "MONSTER_094", name = "WaterDBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_003_1", name = "FireABasic", stage = "STAGE_1", prevEvoName = "WaterDBasic" },
			{ id = "MONSTER_003_2", name = "FireABasic", stage = "STAGE_1", prevEvoName = "WaterDBasic" },
			{ id = "MONSTER_080", name = "FireDBasic", stage = "STAGE_1", prevEvoName = "WaterDBasic" },
			{ id = "MONSTER_039_1", name = "WaterABasic", stage = "STAGE_1", prevEvoName = "WaterDBasic" },
			{ id = "MONSTER_039_2", name = "WaterABasic", stage = "STAGE_1", prevEvoName = "WaterDBasic" },
			{ id = "MONSTER_039_3", name = "WaterABasic", stage = "STAGE_1", prevEvoName = "WaterDBasic" },

			-- shape: [1-0-0]
			{ id = "MONSTER_026_1", name = "FireB2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_026_2", name = "FireB2nd", stage = "BASIC", prevEvoName = "" },

			-- These two repeat after we used each other at least once

			-- shape: [1-0-0]
			{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-1-0]
			{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_092_1", name = "FireCBasic", stage = "STAGE_1", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_092_2", name = "FireCBasic", stage = "STAGE_1", prevEvoName = "WaterA2nd" },
		}
	),

	caseFor(
		"all_together_fully_random",
		{
			source = "ROM",
			approach = "FULLY_RANDOM",
			grouping = "ALL_TOGETHER",
			withinType = false,
			evoLinePool = defaultEvoLinePool,
		},
		romCards,
		romCards,
		{
			-- shape: [1-3-2]
			-- Has several cards with changed stages as expected and mixed types
			{ id = "MONSTER_038_1", name = "FireA2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_038_2", name = "FireA2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_092_1", name = "FireCBasic", stage = "STAGE_1", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_092_2", name = "FireCBasic", stage = "STAGE_1", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_039_1", name = "WaterABasic", stage = "STAGE_2", prevEvoName = "FireCBasic" },
			{ id = "MONSTER_039_2", name = "WaterABasic", stage = "STAGE_2", prevEvoName = "FireCBasic" },
			{ id = "MONSTER_039_3", name = "WaterABasic", stage = "STAGE_2", prevEvoName = "FireCBasic" },
			{ id = "MONSTER_094", name = "WaterDBasic", stage = "STAGE_1", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_2", prevEvoName = "WaterDBasic" },
			{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_2", prevEvoName = "WaterDBasic" },
			{ id = "MONSTER_026_1", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_026_2", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireA2nd" },

			-- Used this one twice even without using [1-1-1]
			-- shape: [1-3-2]
			{ id = "MONSTER_081_1", name = "FireB3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_081_2", name = "FireB3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_018_1", name = "FireEBasic", stage = "STAGE_1", prevEvoName = "FireB3rd" },
			{ id = "MONSTER_018_2", name = "FireEBasic", stage = "STAGE_1", prevEvoName = "FireB3rd" },
			{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "FireEBasic" },
			{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "FireEBasic" },
			{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "FireB3rd" },
			{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "FireB3rd" },
			{ id = "MONSTER_025_1", name = "FireBBasic", stage = "STAGE_2", prevEvoName = "WaterB2nd" },
			{ id = "MONSTER_025_2", name = "FireBBasic", stage = "STAGE_2", prevEvoName = "WaterB2nd" },
			{ id = "MONSTER_025_3", name = "FireBBasic", stage = "STAGE_2", prevEvoName = "WaterB2nd" },
			{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "FireB3rd" },
			{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "FireB3rd" },

			-- shape: [1-3-0]
			{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_1", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_1", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_003_1", name = "FireABasic", stage = "STAGE_1", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_003_2", name = "FireABasic", stage = "STAGE_1", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "STAGE_1", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "STAGE_1", prevEvoName = "WaterC2nd" },

			-- shape: [1-1-0]
			{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_080", name = "FireDBasic", stage = "STAGE_1", prevEvoName = "WaterCBasic" },

			-- shape: [1-0-0]
			{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
		},
		41
	),

	caseFor(
		"by_stage",
		{
			source = "ROM",
			approach = "MINIMIZE_REPEATS",
			grouping = "BY_STAGE",
			withinType = false,
			evoLinePool = defaultEvoLinePool,
		},
		romCards,
		romCards,
		{
			-- We don't use [1-3-0] but thats due to the order we got things and not having enough -
			-- it then fills in as much as it can with the remaining cards so it still has repeats
			-- but fewer (hopefully) than it would otherwise. We see alot of basics because again
			-- its filling in the cards as best it can with what is left

			-- shape: [1-3-2]
			-- Types are mixed up but stages are kept
			{ id = "MONSTER_003_1", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_003_2", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_026_1", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_026_2", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },

			-- shape: [1-1-1]
			-- Branch changes from water to fire which is expected
			{ id = "MONSTER_039_1", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_2", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_3", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "WaterB2nd" },
			{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "WaterB2nd" },

			-- shape: [1-1-1]
			{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "WaterBBasic" },
			{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "WaterBBasic" },
			{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireC2nd" },
			{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireC2nd" },

			-- shape: [1-1-0]
			-- FireA is a 3 stage that got changed to a two stage here - working
			-- as expected - i.e. not necessarily preservation max stage
			{ id = "MONSTER_025_1", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_2", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_3", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_038_1", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_038_2", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },

			-- shape: [1-0-0]
			{ id = "MONSTER_092_1", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_092_2", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_080", name = "FireDBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_018_1", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_018_2", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_094", name = "WaterDBasic", stage = "BASIC", prevEvoName = "" },
		}
	),

	caseFor(
		"by_stage_and_max_stage",
		{
			source = "ROM",
			approach = "MINIMIZE_REPEATS",
			grouping = "BY_STAGE_AND_MAX_STAGE",
			withinType = false,
			evoLinePool = defaultEvoLinePool,
		},
		romCards,
		romCards,
		{
			-- shape: [1-1-1]
			-- Mixed types as expected
			{ id = "MONSTER_003_1", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_003_2", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },

			-- shape: [1-1-1]
			{ id = "MONSTER_025_1", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_2", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_3", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_026_1", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_026_2", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },
			{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },

			-- shape: [1-3-2]
			{ id = "MONSTER_039_1", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_2", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_3", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_038_1", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_038_2", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },

			-- shape: [1-1-0]
			{ id = "MONSTER_092_1", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_092_2", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "FireCBasic" },
			{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "FireCBasic" },

			-- shape: [1-0-0]
			{ id = "MONSTER_080", name = "FireDBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_018_1", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_018_2", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			-- Was a basic/stage 2 but we did not use all them with different evo lines, so
			-- its now just a basic/basic (this is because we have a 1-3-2 so we have one more
			-- stage1/stage2 and stage2/stage2 used for the basics meaing one will be left over)
			{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_094", name = "WaterDBasic", stage = "BASIC", prevEvoName = "" },
		}
	),

	caseFor(
		"within_type_all_together",
		{
			source = "ROM",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
			withinType = true,
			evoLinePool = defaultEvoLinePool,
		},
		romCards,
		romCards,
		{
			-- shape: [1-3-2]
			-- Each branch is its own type - so "mixing" here of fire and water is expected -
			-- each branch does keep its type
			-- Water C is a 3 stage evo but is switched to a basic here showing its working as
			-- expected
			{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_2", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_2", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_039_1", name = "WaterABasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_039_2", name = "WaterABasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_039_3", name = "WaterABasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_094", name = "WaterDBasic", stage = "STAGE_2", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_018_1", name = "FireEBasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_018_2", name = "FireEBasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			-- shape: [1-1-1]
			{ id = "MONSTER_092_1", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_092_2", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_080", name = "FireDBasic", stage = "STAGE_1", prevEvoName = "FireCBasic" },
			{ id = "MONSTER_026_1", name = "FireB2nd", stage = "STAGE_2", prevEvoName = "FireDBasic" },
			{ id = "MONSTER_026_2", name = "FireB2nd", stage = "STAGE_2", prevEvoName = "FireDBasic" },
			-- shape: [1-3-0]
			-- Each branch is its own type - so "mixing" here of fire and water is expected -
			-- each branch does keep its type
			{ id = "MONSTER_025_1", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_2", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_3", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			-- shape: [1-1-0]
			{ id = "MONSTER_003_1", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_003_2", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			-- shape: [1-1-0]
			{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "STAGE_1", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "STAGE_1", prevEvoName = "WaterA2nd" },
			-- shape: [1-0-0]
			{ id = "MONSTER_038_1", name = "FireA2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_038_2", name = "FireA2nd", stage = "BASIC", prevEvoName = "" },
			-- shape: [1-0-0]
			{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
		}
	),

	caseFor(
		"within_type_by_stage",
		{
			source = "ROM",
			approach = "MINIMIZE_REPEATS",
			grouping = "BY_STAGE",
			withinType = true,
			evoLinePool = defaultEvoLinePool,
		},
		romCards,
		romCards,
		{
			-- shape: [1-3-2]
			-- Each branch is its own type - so "mixing" here of fire and water is expected -
			-- each branch does keep its type
			-- Water B is a 2 stage evo but is switched to a 3 stage here showing its working as
			-- expected
			{ id = "MONSTER_003_1", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_003_2", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterB2nd" },
			{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterB2nd" },
			{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			-- shape: [1-1-1]
			{ id = "MONSTER_080", name = "FireDBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_026_1", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireDBasic" },
			{ id = "MONSTER_026_2", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireDBasic" },
			{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },
			{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },
			-- shape: [1-1-1]
			{ id = "MONSTER_018_1", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_018_2", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_038_1", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "FireEBasic" },
			{ id = "MONSTER_038_2", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "FireEBasic" },
			{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },
			-- shape: [1-1-0]
			{ id = "MONSTER_092_1", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_092_2", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "FireCBasic" },
			{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "FireCBasic" },
			-- shape: [1-0-0]
			{ id = "MONSTER_094", name = "WaterDBasic", stage = "BASIC", prevEvoName = "" },
			-- shape: [1-0-0]
			{ id = "MONSTER_025_1", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_2", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_3", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			-- shape: [1-0-0]
			{ id = "MONSTER_039_1", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_2", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_3", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			-- shape: [1-0-0]
			{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			-- shape: [1-0-0]
			{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
		}
	),

	caseFor(
		"within_type_by_stage_and_max_stage",
		{
			source = "ROM",
			approach = "MINIMIZE_REPEATS",
			grouping = "BY_STAGE_AND_MAX_STAGE",
			withinType = true,
			evoLinePool = defaultEvoLinePool,
		},
		romCards,
		romCards,
		{
			-- shape: [1-3-2]
			-- Each branch is its own type - so "mixing" here of fire and water is expected -
			-- each branch does keep its type
			{ id = "MONSTER_003_1", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_003_2", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			-- shape: [1-1-1]
			{ id = "MONSTER_025_1", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_2", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_3", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_038_1", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_038_2", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },
			-- shape: [1-1-1]
			{ id = "MONSTER_092_1", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_092_2", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_026_1", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireCBasic" },
			{ id = "MONSTER_026_2", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireCBasic" },
			{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },
			{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },
			-- shape: [1-1-0]
			{ id = "MONSTER_018_1", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_018_2", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "FireEBasic" },
			{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "FireEBasic" },
			-- shape: [1-0-0]
			{ id = "MONSTER_080", name = "FireDBasic", stage = "BASIC", prevEvoName = "" },
			-- shape: [1-0-0]
			{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			-- shape: [1-0-0]
			{ id = "MONSTER_039_1", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_2", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_3", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			-- shape: [1-0-0]
			{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			-- shape: [1-0-0]
			{ id = "MONSTER_094", name = "WaterDBasic", stage = "BASIC", prevEvoName = "" },
		}
	),

	caseFor(
		"from_current_all_together",
		{
			source = "CURRENT",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
			withinType = false,
			evoLinePool = defaultEvoLinePool,
		},
		romCards,
		currentCards,
		{
			-- shape: [1-1-1]
			{ id = "MONSTER_081_2", name = "FightABranchB", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_003_1", name = "GrassA3rd", stage = "STAGE_1", prevEvoName = "FightABranchB" },
			{ id = "MONSTER_001", name = "GrassABasic", stage = "STAGE_2", prevEvoName = "GrassA3rd" },

			-- shape: [1-1-0]
			{ id = "MONSTER_004", name = "GrassBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_080", name = "FightABasic", stage = "STAGE_1", prevEvoName = "GrassBBasic" },

			-- shape: [1-3-0]
			{ id = "MONSTER_007", name = "GrassCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_081_1", name = "FightABranchA", stage = "STAGE_1", prevEvoName = "GrassCBasic" },
			{ id = "MONSTER_012", name = "FightBBasic", stage = "STAGE_1", prevEvoName = "GrassCBasic" },
			{ id = "MONSTER_002", name = "GrassA2nd", stage = "STAGE_1", prevEvoName = "GrassCBasic" },

			-- shape: [1-0-0]
			{ id = "MONSTER_005", name = "GrassB2nd", stage = "BASIC", prevEvoName = "" },
		}
	),
}
