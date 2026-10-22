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
			-- One type per branch
			-- Failry even spread of branches with leftovers creating multiples of the smallers
			{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_092_1", name = "FireCBasic", stage = "STAGE_1", prevEvoName = "WaterA3rd" },
			{ id = "MONSTER_092_2", name = "FireCBasic", stage = "STAGE_1", prevEvoName = "WaterA3rd" },
			{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_2", prevEvoName = "FireCBasic" },
			{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_2", prevEvoName = "FireCBasic" },
			{ id = "MONSTER_038_1", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "WaterA3rd" },
			{ id = "MONSTER_038_2", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "WaterA3rd" },
			{ id = "MONSTER_018_1", name = "FireEBasic", stage = "STAGE_2", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_018_2", name = "FireEBasic", stage = "STAGE_2", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_003_1", name = "FireABasic", stage = "STAGE_1", prevEvoName = "WaterA3rd" },
			{ id = "MONSTER_003_2", name = "FireABasic", stage = "STAGE_1", prevEvoName = "WaterA3rd" },

			-- shape: [1-3-0]
			{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_094", name = "WaterDBasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_039_1", name = "WaterABasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_039_2", name = "WaterABasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_039_3", name = "WaterABasic", stage = "STAGE_1", prevEvoName = "WaterC3rd" },

			-- shape: [1-1-1]
			-- One type for the line
			{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "STAGE_1", prevEvoName = "WaterB2nd" },
			{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "STAGE_1", prevEvoName = "WaterB2nd" },
			{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_2", prevEvoName = "WaterBBasic" },
			{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_2", prevEvoName = "WaterBBasic" },

			-- shape: [1-1-0]
			-- One type for the line
			{ id = "MONSTER_025_1", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_2", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_3", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_080", name = "FireDBasic", stage = "STAGE_1", prevEvoName = "FireBBasic" },

			-- shape: [1-1-0]
			-- One type for the line
			{ id = "MONSTER_026_1", name = "FireB2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_026_2", name = "FireB2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_1", prevEvoName = "FireB2nd" },
			{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_1", prevEvoName = "FireB2nd" },

			-- shape: [1-0-0]
			{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
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
			-- Each keeps its stage

			-- shape: [1-3-2]
			-- Each branch keeps its own type
			{ id = "MONSTER_094", name = "WaterDBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterDBasic" },
			{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterDBasic" },
			{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_038_1", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "WaterDBasic" },
			{ id = "MONSTER_038_2", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "WaterDBasic" },
			{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "WaterDBasic" },
			{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "WaterDBasic" },

			-- shape: [1-1-1]
			{ id = "MONSTER_080", name = "FireDBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_026_1", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireDBasic" },
			{ id = "MONSTER_026_2", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireDBasic" },
			{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },
			{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },

			-- shape: [1-1-1]
			{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterBBasic" },
			{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterBBasic" },
			{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },

			-- shape: [1-1-0]
			{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },

			-- shape: [1-0-0]
			{ id = "MONSTER_003_1", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_003_2", name = "FireABasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_025_1", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_2", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_3", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_092_1", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_092_2", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_018_1", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_018_2", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_039_1", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_2", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_3", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
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
			{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_026_1", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_026_2", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },
			{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },
			{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },

			-- shape: [1-1-1]
			{ id = "MONSTER_025_1", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_2", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_3", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_038_1", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_038_2", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },

			-- shape: [1-1-1]
			{ id = "MONSTER_039_1", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_2", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_3", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },

			-- shape: [1-1-0]
			{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterBBasic" },
			{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterBBasic" },

			-- Fire A & C we coerced into basic/basic due to card availability and evo lines
			-- that were selected
			-- shape: [1-0-0]
			{ id = "MONSTER_003_1", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_003_2", name = "FireABasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_092_1", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_092_2", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_080", name = "FireDBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_018_1", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_018_2", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_094", name = "WaterDBasic", stage = "BASIC", prevEvoName = "" },
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
			{ id = "MONSTER_018_1", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_018_2", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_1", prevEvoName = "FireEBasic" },
			{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_1", prevEvoName = "FireEBasic" },
			{ id = "MONSTER_080", name = "FireDBasic", stage = "STAGE_2", prevEvoName = "FireB3rd" },
			{ id = "MONSTER_038_1", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "FireEBasic" },
			{ id = "MONSTER_038_2", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "FireEBasic" },
			{ id = "MONSTER_003_1", name = "FireABasic", stage = "STAGE_2", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_003_2", name = "FireABasic", stage = "STAGE_2", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "FireEBasic" },
			{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "FireEBasic" },

			-- shape: [1-3-0]
			{ id = "MONSTER_039_1", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_2", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_3", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_092_1", name = "FireCBasic", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_092_2", name = "FireCBasic", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },
			{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterABasic" },

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
			{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "STAGE_1", prevEvoName = "WaterC3rd" },
			{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "STAGE_2", prevEvoName = "WaterA3rd" },
			{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "STAGE_2", prevEvoName = "WaterA3rd" },
			{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "STAGE_2", prevEvoName = "WaterA3rd" }

			-- shape: [1-0-0]
			{ id = "MONSTER_059_1", name = "FireA3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_059_2", name = "FireA3rd", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "BASIC", prevEvoName = "" },

			-- shape: [1-0-0]
			{ id = "MONSTER_094", name = "WaterDBasic", stage = "BASIC", prevEvoName = "" },
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
			{ id = "MONSTER_081_2", name = "FightABranchB", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_001", name = "GrassABasic", stage = "STAGE_1", prevEvoName = "FightABranchB" },
			{ id = "MONSTER_005", name = "GrassB2nd", stage = "STAGE_1", prevEvoName = "FightABranchB" },
			{ id = "MONSTER_081_1", name = "FightABranchA", stage = "STAGE_1", prevEvoName = "FightABranchB" },

			{ id = "MONSTER_002", name = "GrassA2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_003_1", name = "GrassA3rd", stage = "STAGE_1", prevEvoName = "GrassA2nd" },
			{ id = "MONSTER_007", name = "GrassCBasic", stage = "STAGE_2", prevEvoName = "GrassA3rd" },

			{ id = "MONSTER_080", name = "FightABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_012", name = "FightBBasic", stage = "BASIC", prevEvoName = "" },

			{ id = "MONSTER_004", name = "GrassBBasic", stage = "BASIC", prevEvoName = "" },
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

	-- Cards stay within their subtype
	--		Fire - Water/None (A & B), Water/Grass (C, D & E)
	--		Water - Lighning/None (A & B), Grass/None (C & D)
	caseFor(
		"within_subtype_all_together",
		{
			source = "ROM",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
			withinType = "Within Subtype",
			evoLinePool = defaultEvoLinePool,
			branchingBasesColorless = unbiasedPolicyArgs.branchingBasesColorless,
			branchRootConsistency = unbiasedPolicyArgs.branchRootConsistency,
			interBranchTypes = unbiasedPolicyArgs.interBranchTypes,
		},
		romCards,
		romCards,
		{
			-- Fire C, D & E together in a non-braching
			{ id = "MONSTER_018_1", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_018_2", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_080", name = "FireDBasic", stage = "STAGE_1", prevEvoName = "FireEBasic" },
			{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_2", prevEvoName = "FireDBasic" },
			{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_2", prevEvoName = "FireDBasic" },

			{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterA3rd" },
			{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterA3rd" },

			-- 1-3-2 chain - each branch is consistent
			{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			-- Fire A&B line
			{ id = "MONSTER_025_1", name = "FireBBasic", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_025_2", name = "FireBBasic", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_025_3", name = "FireBBasic", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireBBasic" },
			-- Fire A&B line
			{ id = "MONSTER_003_1", name = "FireABasic", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_003_2", name = "FireABasic", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireABasic" },
			{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireABasic" },
			-- Water A&B Line
			{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "STAGE_1", prevEvoName = "WaterCBasic" },

			-- 1-3 chain - no relation between the branches
			{ id = "MONSTER_094", name = "WaterDBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_092_1", name = "FireCBasic", stage = "STAGE_1", prevEvoName = "WaterDBasic" },
			{ id = "MONSTER_092_2", name = "FireCBasic", stage = "STAGE_1", prevEvoName = "WaterDBasic" },
			{ id = "MONSTER_038_1", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "WaterDBasic" },
			{ id = "MONSTER_038_2", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "WaterDBasic" },
			{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterDBasic" },
			{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterDBasic" },

			{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "BASIC", prevEvoName = "" },

			{ id = "MONSTER_026_1", name = "FireB2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_026_2", name = "FireB2nd", stage = "BASIC", prevEvoName = "" },

			-- 1-1 line water A & B line
			{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_1", name = "WaterABasic", stage = "STAGE_1", prevEvoName = "WaterB2nd" },
			{ id = "MONSTER_039_2", name = "WaterABasic", stage = "STAGE_1", prevEvoName = "WaterB2nd" },
			{ id = "MONSTER_039_3", name = "WaterABasic", stage = "STAGE_1", prevEvoName = "WaterB2nd" },
		}
	),

	caseFor(
		"within_subtype_by_stage_and_max_stage",
		{
			source = "ROM",
			approach = "MINIMIZE_REPEATS",
			grouping = "BY_STAGE_AND_MAX_STAGE",
			withinType = "Within Subtype",
			evoLinePool = defaultEvoLinePool,
			branchingBasesColorless = unbiasedPolicyArgs.branchingBasesColorless,
			branchRootConsistency = unbiasedPolicyArgs.branchRootConsistency,
			interBranchTypes = unbiasedPolicyArgs.interBranchTypes,
		},
		romCards,
		romCards,
		{
			-- Fire A&B mixed
			{ id = "MONSTER_003_1", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_003_2", name = "FireABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_026_1", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_026_2", name = "FireB2nd", stage = "STAGE_1", prevEvoName = "FireABasic" },
			{ id = "MONSTER_081_1", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },
			{ id = "MONSTER_081_2", name = "FireB3rd", stage = "STAGE_2", prevEvoName = "FireB2nd" },
			-- Fire A &B mixed again
			{ id = "MONSTER_025_1", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_2", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_025_3", name = "FireBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_038_1", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_038_2", name = "FireA2nd", stage = "STAGE_1", prevEvoName = "FireBBasic" },
			{ id = "MONSTER_059_1", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },
			{ id = "MONSTER_059_2", name = "FireA3rd", stage = "STAGE_2", prevEvoName = "FireA2nd" },

			{ id = "MONSTER_092_1", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_092_2", name = "FireCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_093_1", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "FireCBasic" },
			{ id = "MONSTER_093_2", name = "FireC2nd", stage = "STAGE_1", prevEvoName = "FireCBasic" },

			{ id = "MONSTER_080", name = "FireDBasic", stage = "BASIC", prevEvoName = "" },

			{ id = "MONSTER_018_1", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_018_2", name = "FireEBasic", stage = "BASIC", prevEvoName = "" },

			-- Water 1-3-2 chain
			{ id = "MONSTER_151_1", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_2", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_151_3", name = "WaterCBasic", stage = "BASIC", prevEvoName = "" },
			-- Water A & B line
			{ id = "MONSTER_052_1", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_052_2", name = "WaterA2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_149_1", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			{ id = "MONSTER_149_2", name = "WaterA3rd", stage = "STAGE_2", prevEvoName = "WaterA2nd" },
			-- Water C & D line
			{ id = "MONSTER_105_1", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_105_2", name = "WaterC2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_079_1", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },
			{ id = "MONSTER_079_2", name = "WaterC3rd", stage = "STAGE_2", prevEvoName = "WaterC2nd" },
			-- Water A & B line
			{ id = "MONSTER_145_1", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },
			{ id = "MONSTER_145_2", name = "WaterB2nd", stage = "STAGE_1", prevEvoName = "WaterCBasic" },

			{ id = "MONSTER_039_1", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_2", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_039_3", name = "WaterABasic", stage = "BASIC", prevEvoName = "" },

			{ id = "MONSTER_135_1", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },
			{ id = "MONSTER_135_2", name = "WaterBBasic", stage = "BASIC", prevEvoName = "" },

			{ id = "MONSTER_094", name = "WaterDBasic", stage = "BASIC", prevEvoName = "" },
		}
	),

}
