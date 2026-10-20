local card_sets = require("support.card_sets")

-- Preserve seed used in testing across rename
local SEED_OFFSET_ADJUST = 4436 - 5339
local seed = 42 + SEED_OFFSET_ADJUST
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

-- Default to unbiased policies for simplicity in most tests
local unbiasedPolicyArgs = {
	branchingBasesColorless = {
		["Colorless"] = 0,
		["Any Type"] = 1,
	},
	branchRootConsistency = {
		["Unbiased"] = 1,
		["Match Branch Root"] = 0,
		["Avoid Branch Root"] = 0,
	},
	interBranchTypes = {
		["Unbiased"] = 1,
		["Match"] = 0,
		["Different"] = 0,
	},
}

local function caseFor(name, args, original, modified, expect, caseSeed)
	return {
		name = name,
		module = "evo_line_custom_within_type",
		seed = caseSeed or seed,
		args = args,
		original = original,
		modified = modified or original,
		expect = expect,
	}
end

return {
	caseFor(
		"all_together",
		{
			source = "ROM",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
			evoLinePool = defaultEvoLinePool,
			branchingBasesColorless = unbiasedPolicyArgs.branchingBasesColorless,
			branchRootConsistency = unbiasedPolicyArgs.branchRootConsistency,
			interBranchTypes = unbiasedPolicyArgs.interBranchTypes,
		},
		romCards,
		romCards,
		{
			-- shape: [1-3-2]
			-- one type per branch as expected
			-- FireDBasic is a basic now as a second stage also working as expected
			{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_080", name = "FireDBasic", stage = "STAGE_1", prevEvoName = "WaterBBasic" },
			{ id = "MONSTER_018_1", name = "FireEBasic", stage = "STAGE_2", prevEvoName = "FireDBasic" },
			{ id = "MONSTER_018_2", name = "FireEBasic", stage = "STAGE_2", prevEvoName = "FireDBasic" },
			{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "STAGE_1", prevEvoName = "WaterBBasic" },
			{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "STAGE_1", prevEvoName = "WaterBBasic" },
			{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "STAGE_1", prevEvoName = "WaterBBasic" },
			{ id = "MONSTER_094", name = "WaterDBasic", stage = "STAGE_2", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_092_1", name = "FireCBasic", stage = "STAGE_1", prevEvoName = "WaterBBasic" },
			{ id = "MONSTER_092_2", name = "FireCBasic", stage = "STAGE_1", prevEvoName = "WaterBBasic" },

			-- shape: [1-3-0]
			{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_026_1", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_026_2", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_1", prevEvoName = "WaterC3rd" },

			-- shape: [1-1-1]
			-- fire basic through stage 2 stays on one fire line
			{ id = "MONSTER_038_1", name = "FireA2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_038_2", name = "FireA2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_1", name = "FireBBasic", stage = "STAGE_1", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_025_2", name = "FireBBasic", stage = "STAGE_1", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_025_3", name = "FireBBasic", stage = "STAGE_1", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_2", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_2", prevEvoName = "FireBBasic" },

			-- shape: [1-1-1]
			-- Two 1-1-1 is fine as we exhausted the pool and had to restart it
			{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterA3rd" },
			{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterA3rd" },
			{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },

			-- shape: [1-1-0]
			{ id = "MONSTER_081_1", name = "FireB3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_081_2", name = "FireB3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_003_1", name = "FireABasic", stage = "STAGE_1", prevEvoName = "FireB3rd" },
			{ id = "MONSTER_003_2", name = "FireABasic", stage = "STAGE_1", prevEvoName = "FireB3rd" },

			-- shape: [1-0-0]
			{ id = "MONSTER_039_1", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_2", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_3", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
		}
	),

	caseFor(
		"by_stage",
		{
			source = "ROM",
			approach = "MINIMIZE_REPEATS",
			grouping = "BY_STAGE",
			evoLinePool = defaultEvoLinePool,
			branchingBasesColorless = unbiasedPolicyArgs.branchingBasesColorless,
			branchRootConsistency = unbiasedPolicyArgs.branchRootConsistency,
			interBranchTypes = unbiasedPolicyArgs.interBranchTypes,
		},
		romCards,
		romCards,
		{
			-- shape: [1-3-2]
			-- Each stage stayed the same. FireA2nd was a 3 stage but now is a 2 stage showing its working as expected
			-- Again each line has its own consistent type
			{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterB2nd" },
			{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterB2nd" },
			{ id = "MONSTER_038_1", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_038_2", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },

			-- shape: [1-1-1]
			{ id = "MONSTER_080", name = "FireDBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_026_1", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireDBasic" },
			{ id = "MONSTER_026_2", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireDBasic" },
			{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },
			{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },

			-- shape: [1-1-1]
			-- shapes are constrained by the stages so we expect repeats here
			{ id = "MONSTER_092_1", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_092_2", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "FireCBasic" },
			{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "FireCBasic" },
			{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireC2nd" },
			{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireC2nd" },

			-- shape: [1-1-0]
			{ id = "MONSTER_039_1", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_2", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_3", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },

			-- shape: [1-0-0]
			{ id = "MONSTER_003_1", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_003_2", name = "FireABasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_018_1", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_018_2", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_025_1", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_2", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_3", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_094", name = "WaterDBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
		}
	),

	caseFor(
		"by_stage_and_max_stage",
		{
			source = "ROM",
			approach = "MINIMIZE_REPEATS",
			grouping = "BY_STAGE_AND_MAX_STAGE",
			evoLinePool = defaultEvoLinePool,
			branchingBasesColorless = unbiasedPolicyArgs.branchingBasesColorless,
			branchRootConsistency = unbiasedPolicyArgs.branchRootConsistency,
			interBranchTypes = unbiasedPolicyArgs.interBranchTypes,
		},
		romCards,
		romCards,
		{
			-- shape: [1-3-2]
			-- All cards stayed wiht the same stage an max eco as expected
			{ id = "MONSTER_039_1", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_2", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_3", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },

			-- shape: [1-1-1]
			{ id = "MONSTER_003_1", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_003_2", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_026_1", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_026_2", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },
			{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },

			-- shape: [1-1-1]
			{ id = "MONSTER_025_1", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_2", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_3", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_038_1", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_038_2", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },

			-- shape: [1-1-0]
			{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterBBasic" },
			{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterBBasic" },

			-- shape: [1-0-0]
			{ id = "MONSTER_018_1", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_018_2", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_080", name = "FireDBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_092_1", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_092_2", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_094", name = "WaterDBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
		}
	),

	caseFor(
		"fully_random",
		{
			source = "ROM",
			approach = "FULLY_RANDOM",
			grouping = "ALL_TOGETHER",
			evoLinePool = defaultEvoLinePool,
			branchingBasesColorless = unbiasedPolicyArgs.branchingBasesColorless,
			branchRootConsistency = unbiasedPolicyArgs.branchRootConsistency,
			interBranchTypes = unbiasedPolicyArgs.interBranchTypes,
		},
		romCards,
		romCards,
		{
			-- shape: [1-3-2]
			{ id = "MONSTER_059_1", name = "FireA3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_059_2", name = "FireA3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "FireA3rd" },
			{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "FireA3rd" },
			{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "STAGE_2", prevEvoName = "WaterB2nd" },
			{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "STAGE_2", prevEvoName = "WaterB2nd" },
			{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "FireA3rd" },
			{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "FireA3rd" },
			{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_1", prevEvoName = "FireA3rd" },
			{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_1", prevEvoName = "FireA3rd" },

			-- shape: [1-3-0]
			{ id = "MONSTER_039_1", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_2", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_3", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_018_1", name = "FireEBasic", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_018_2", name = "FireEBasic", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_038_1", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_038_2", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },

			-- This shape was choosen 2 times while [1-1-0] was not used at all
			-- shape: [1-1-1]
			{ id = "MONSTER_026_1", name = "FireB2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_026_2", name = "FireB2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_1", name = "FireBBasic", stage = "STAGE_1", prevEvoName = "FireB2nd" },
			{ id = "MONSTER_025_2", name = "FireBBasic", stage = "STAGE_1", prevEvoName = "FireB2nd" },
			{ id = "MONSTER_025_3", name = "FireBBasic", stage = "STAGE_1", prevEvoName = "FireB2nd" },
			{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_2", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_2", prevEvoName = "FireBBasic" },

			-- shape: [1-1-1]
			{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_094", name = "WaterDBasic", stage = "STAGE_2", prevEvoName = "WaterC3rd" },

			-- This shape was choosen 3 times while [1-1-0] was not used at all
			-- shape: [1-0-0]
			{ id = "MONSTER_003_1", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_003_2", name = "FireABasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_080", name = "FireDBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_092_1", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_092_2", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },

		},
		31 + SEED_OFFSET_ADJUST
	),

	caseFor(
		"from_current",
		{
			source = "CURRENT",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
			evoLinePool = defaultEvoLinePool,
			branchingBasesColorless = unbiasedPolicyArgs.branchingBasesColorless,
			branchRootConsistency = unbiasedPolicyArgs.branchRootConsistency,
			interBranchTypes = unbiasedPolicyArgs.interBranchTypes,
		},
		romCards,
		currentCards,
		{
			-- Uses the current cards (fighting and grass) instead of rom cards (fire and water)
			{ id = "MONSTER_081_2", name = "FightABranchB", stage = "BASIC", prevEvoName = "", type = "MONSTER_FIGHTING" },
			{ id = "MONSTER_001", name = "GrassABasic", stage = "STAGE_1", prevEvoName = "FightABranchB", type = "MONSTER_GRASS" },
			{ id = "MONSTER_005", name = "GrassB2nd", stage = "STAGE_1", prevEvoName = "FightABranchB", type = "MONSTER_GRASS" },
			{ id = "MONSTER_081_1", name = "FightABranchA", stage = "STAGE_1", prevEvoName = "FightABranchB", type = "MONSTER_FIGHTING" },

			{ id = "MONSTER_002", name = "GrassA2nd", stage = "BASIC", prevEvoName = "", type = "MONSTER_GRASS" },
			{ id = "MONSTER_003_1", name = "GrassA3rd", stage = "STAGE_1", prevEvoName = "GrassA2nd", type = "MONSTER_GRASS" },
			{ id = "MONSTER_007", name = "GrassCBasic", stage = "STAGE_2", prevEvoName = "GrassA3rd", type = "MONSTER_GRASS" },

			{ id = "MONSTER_080", name = "FightABasic", stage = "BASIC", prevEvoName = "", type = "MONSTER_FIGHTING" },
			{ id = "MONSTER_012", name = "FightBBasic", stage = "BASIC", prevEvoName = "", type = "MONSTER_FIGHTING" },

			{ id = "MONSTER_004", name = "GrassBBasic", stage = "BASIC", prevEvoName = "", type = "MONSTER_GRASS" },
		}
	),

	caseFor(
		"exclude_trainer_proxies_isolates_as_basics",
		{
			source = "ROM",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
			excludeTrainerProxies = true,
			evoLinePool = {
				{ weight = 1, evoLine = { BASIC = 1, STAGE_1 = 1, STAGE_2 = 1 } },
				{ weight = 1, evoLine = { BASIC = 1, STAGE_1 = 0, STAGE_2 = 0 } },
			},
			branchingBasesColorless = unbiasedPolicyArgs.branchingBasesColorless,
			branchRootConsistency = unbiasedPolicyArgs.branchRootConsistency,
			interBranchTypes = unbiasedPolicyArgs.interBranchTypes,
		},
		card_sets.EVO_BRANCH_PROXY_3_TO_2,
		nil,
		{
			-- Proxy becomes a basic, others can form lines
			{ id = "MONSTER_148", name = "Proxy3Basic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_141", name = "Proxy3S1B", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_140", name = "Proxy3S1A", stage = "STAGE_1", prevEvoName = "Proxy3S1B" },
			{ id = "MONSTER_143", name = "Proxy3S2A", stage = "STAGE_2", prevEvoName = "Proxy3S1A" },
			{ id = "MONSTER_142", name = "Proxy3S1C", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_147", name = "Proxy3S2B", stage = "BASIC", prevEvoName = "" },
		}
	),


}
