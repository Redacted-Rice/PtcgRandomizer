-- Preserve seed used in testing across rename
local SEED_OFFSET_ADJUST = 4436 - 5339
local seed = 42 + SEED_OFFSET_ADJUST

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

local function copyCards(cards)
	local out = {}
	for i, card in ipairs(cards) do
		local row = {}
		for k, v in pairs(card) do
			row[k] = v
		end
		out[i] = row
	end
	return out
end

local policyBranchEvoLinePool = {
	{ weight = 1, evoLine = { BASIC = 1, STAGE_1 = 2, STAGE_2 = 0 } },
	{ weight = 1, evoLine = { BASIC = 1, STAGE_1 = 2, STAGE_2 = 4 } },
	{ weight = 1, evoLine = { BASIC = 1, STAGE_1 = 3, STAGE_2 = 2 } },
}

local function buildPolicyBranchCards(type1, type2, type3, type4)
	return {
		{ id = "MONSTER_101_1", name = "Card_1", type = type1, stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_101_2", name = "Card_1", type = type1, stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_102", name = "Card_2", type = type2, stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_103", name = "Card_3", type = type3, stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_104", name = "Card_4", type = type4, stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_105_1", name = "Card_5", type = type1, stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_105_2", name = "Card_5", type = type1, stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_106", name = "Card_6", type = type2, stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_107", name = "Card_7", type = type3, stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_108", name = "Card_8", type = type4, stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_109", name = "Card_9", type = type1, stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_110", name = "Card_10", type = type2, stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_121", name = "Card_11", type = type3, stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_122", name = "Card_11", type = type3, stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_129", name = "Card_12", type = type4, stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_132", name = "Card_12", type = type4, stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_130", name = "Card_13", type = type1, stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_131", name = "Card_14", type = type2, stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_138", name = "Card_15", type = type3, stage = "BASIC", prevEvoName = "" },
		{ id = "MONSTER_139", name = "Card_16", type = type4, stage = "BASIC", prevEvoName = "" },
	}
end

local policyBranchCards = buildPolicyBranchCards(
	"MONSTER_FIRE",
	"MONSTER_WATER",
	"MONSTER_COLORLESS",
	"MONSTER_GRASS"
)
local policyBranchFireWaterCards = buildPolicyBranchCards(
	"MONSTER_FIRE",
	"MONSTER_WATER",
	"MONSTER_FIRE",
	"MONSTER_WATER"
)

return {
	caseFor(
		"inter_branch_match",
		{
			source = "ROM",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
			evoLinePool = policyBranchEvoLinePool,
			branchingBasesColorless = unbiasedPolicyArgs.branchingBasesColorless,
			branchRootConsistency = unbiasedPolicyArgs.branchRootConsistency,
			interBranchTypes = {
				["Unbiased"] = 0,
				["Match"] = 1,
				["Different"] = 0,
			},
		},
		-- Inter branch match uses FIRE and WATER only to showcase better
		copyCards(policyBranchFireWaterCards),
		copyCards(policyBranchFireWaterCards),
		{
			-- shape: [1-3-2]
			-- All branches water
			{ id = "MONSTER_106", name = "Card_6", stage = "BASIC", prevEvoName = "", type = "MONSTER_WATER" },
			{ id = "MONSTER_129", name = "Card_12", stage = "STAGE_1", prevEvoName = "Card_6", type = "MONSTER_WATER" },
			{ id = "MONSTER_132", name = "Card_12", stage = "STAGE_1", prevEvoName = "Card_6", type = "MONSTER_WATER" },
			{ id = "MONSTER_139", name = "Card_16", stage = "STAGE_1", prevEvoName = "Card_6", type = "MONSTER_WATER" },
			{ id = "MONSTER_102", name = "Card_2", stage = "STAGE_1", prevEvoName = "Card_6", type = "MONSTER_WATER" },
			{ id = "MONSTER_110", name = "Card_10", stage = "STAGE_2", prevEvoName = "Card_12", type = "MONSTER_WATER" },
			{ id = "MONSTER_131", name = "Card_14", stage = "STAGE_2", prevEvoName = "Card_2", type = "MONSTER_WATER" },

			-- shape: [1-2-4]
			-- Stage 1's share the same type, Stage 2's of Card_13 share fire, stage 2's of Card_11 are different
			-- but that's because we ran out of fire names in the pool
			{ id = "MONSTER_107", name = "Card_7", stage = "BASIC", prevEvoName = "", type = "MONSTER_FIRE" },
			{ id = "MONSTER_121", name = "Card_11", stage = "STAGE_1", prevEvoName = "Card_7", type = "MONSTER_FIRE" },
			{ id = "MONSTER_122", name = "Card_11", stage = "STAGE_1", prevEvoName = "Card_7", type = "MONSTER_FIRE" },
			{ id = "MONSTER_130", name = "Card_13", stage = "STAGE_1", prevEvoName = "Card_7", type = "MONSTER_FIRE" },
			{ id = "MONSTER_101_1", name = "Card_1", stage = "STAGE_2", prevEvoName = "Card_13", type = "MONSTER_FIRE" },
			{ id = "MONSTER_101_2", name = "Card_1", stage = "STAGE_2", prevEvoName = "Card_13", type = "MONSTER_FIRE" },
			{ id = "MONSTER_138", name = "Card_15", stage = "STAGE_2", prevEvoName = "Card_11", type = "MONSTER_FIRE" },
			{ id = "MONSTER_108", name = "Card_8", stage = "STAGE_2", prevEvoName = "Card_11", type = "MONSTER_WATER" },
			{ id = "MONSTER_109", name = "Card_9", stage = "STAGE_2", prevEvoName = "Card_13", type = "MONSTER_FIRE" },

			-- shape: [1-2-0]
			-- stage 1 branches are fire
			{ id = "MONSTER_104", name = "Card_4", stage = "BASIC", prevEvoName = "", type = "MONSTER_WATER" },
			{ id = "MONSTER_103", name = "Card_3", stage = "STAGE_1", prevEvoName = "Card_4", type = "MONSTER_FIRE" },
			{ id = "MONSTER_105_1", name = "Card_5", stage = "STAGE_1", prevEvoName = "Card_4", type = "MONSTER_FIRE" },
			{ id = "MONSTER_105_2", name = "Card_5", stage = "STAGE_1", prevEvoName = "Card_4", type = "MONSTER_FIRE" },
		},
		1 + SEED_OFFSET_ADJUST
	),
	caseFor(
		"inter_branch_different",
		{
			source = "ROM",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
			evoLinePool = policyBranchEvoLinePool,
			branchingBasesColorless = unbiasedPolicyArgs.branchingBasesColorless,
			branchRootConsistency = unbiasedPolicyArgs.branchRootConsistency,
			interBranchTypes = {
				["Unbiased"] = 0,
				["Match"] = 0,
				["Different"] = 1,
			},
		},
		copyCards(policyBranchCards),
		copyCards(policyBranchCards),
		{
			-- shape: [1-3-2]
			-- each branch is a different type
			{ id = "MONSTER_107", name = "Card_7", stage = "BASIC", prevEvoName = "", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_110", name = "Card_10", stage = "STAGE_1", prevEvoName = "Card_7", type = "MONSTER_WATER" },
			{ id = "MONSTER_131", name = "Card_14", stage = "STAGE_2", prevEvoName = "Card_10", type = "MONSTER_WATER" },
			{ id = "MONSTER_139", name = "Card_16", stage = "STAGE_1", prevEvoName = "Card_7", type = "MONSTER_GRASS" },
			{ id = "MONSTER_130", name = "Card_13", stage = "STAGE_1", prevEvoName = "Card_7", type = "MONSTER_FIRE" },
			{ id = "MONSTER_101_1", name = "Card_1", stage = "STAGE_2", prevEvoName = "Card_13", type = "MONSTER_FIRE" },
			{ id = "MONSTER_101_2", name = "Card_1", stage = "STAGE_2", prevEvoName = "Card_13", type = "MONSTER_FIRE" },

			-- shape: [1-2-4]
			-- Stage 1s are different, each separate stage 1's stage 2's are different
			{ id = "MONSTER_121", name = "Card_11", stage = "BASIC", prevEvoName = "", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_122", name = "Card_11", stage = "BASIC", prevEvoName = "", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_109", name = "Card_9", stage = "STAGE_1", prevEvoName = "Card_11", type = "MONSTER_FIRE" },
			{ id = "MONSTER_106", name = "Card_6", stage = "STAGE_2", prevEvoName = "Card_9", type = "MONSTER_WATER" },
			{ id = "MONSTER_108", name = "Card_8", stage = "STAGE_2", prevEvoName = "Card_9", type = "MONSTER_GRASS" },
			{ id = "MONSTER_104", name = "Card_4", stage = "STAGE_1", prevEvoName = "Card_11", type = "MONSTER_GRASS" },
			{ id = "MONSTER_105_1", name = "Card_5", stage = "STAGE_2", prevEvoName = "Card_4", type = "MONSTER_FIRE" },
			{ id = "MONSTER_105_2", name = "Card_5", stage = "STAGE_2", prevEvoName = "Card_4", type = "MONSTER_FIRE" },
			{ id = "MONSTER_102", name = "Card_2", stage = "STAGE_2", prevEvoName = "Card_4", type = "MONSTER_WATER" },

			-- shape: [1-2-0]
			-- Each line is different
			{ id = "MONSTER_138", name = "Card_15", stage = "BASIC", prevEvoName = "", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_103", name = "Card_3", stage = "STAGE_1", prevEvoName = "Card_15", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_129", name = "Card_12", stage = "STAGE_1", prevEvoName = "Card_15", type = "MONSTER_GRASS" },
			{ id = "MONSTER_132", name = "Card_12", stage = "STAGE_1", prevEvoName = "Card_15", type = "MONSTER_GRASS" },
		},
		1 + SEED_OFFSET_ADJUST
	),
	caseFor(
		"branch_root_match",
		{
			source = "ROM",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
			evoLinePool = policyBranchEvoLinePool,
			branchingBasesColorless = unbiasedPolicyArgs.branchingBasesColorless,
			branchRootConsistency = {
				["Unbiased"] = 0,
				["Match Branch Root"] = 1,
				["Avoid Branch Root"] = 0,
			},
			interBranchTypes = unbiasedPolicyArgs.interBranchTypes,
		},
		copyCards(policyBranchCards),
		copyCards(policyBranchCards),
		{
			-- shape: [1-3-2]
			-- One branch matches root (water)
			{ id = "MONSTER_110", name = "Card_10", stage = "BASIC", prevEvoName = "", type = "MONSTER_WATER" },
			{ id = "MONSTER_101_1", name = "Card_1", stage = "STAGE_1", prevEvoName = "Card_10", type = "MONSTER_FIRE" },
			{ id = "MONSTER_101_2", name = "Card_1", stage = "STAGE_1", prevEvoName = "Card_10", type = "MONSTER_FIRE" },
			{ id = "MONSTER_130", name = "Card_13", stage = "STAGE_2", prevEvoName = "Card_1", type = "MONSTER_FIRE" },
			{ id = "MONSTER_102", name = "Card_2", stage = "STAGE_1", prevEvoName = "Card_10", type = "MONSTER_WATER" },
			{ id = "MONSTER_131", name = "Card_14", stage = "STAGE_2", prevEvoName = "Card_2", type = "MONSTER_WATER" },
			{ id = "MONSTER_105_1", name = "Card_5", stage = "STAGE_1", prevEvoName = "Card_10", type = "MONSTER_FIRE" },
			{ id = "MONSTER_105_2", name = "Card_5", stage = "STAGE_1", prevEvoName = "Card_10", type = "MONSTER_FIRE" },

			-- shape: [1-2-4]
			-- F We ran out of fires so we couldn't match the basic node but each of the stage 1 nodes have a match
			{ id = "MONSTER_109", name = "Card_9", stage = "BASIC", prevEvoName = "", type = "MONSTER_FIRE" },
			{ id = "MONSTER_106", name = "Card_6", stage = "STAGE_1", prevEvoName = "Card_9", type = "MONSTER_WATER" },
			{ id = "MONSTER_139", name = "Card_16", stage = "STAGE_2", prevEvoName = "Card_6", type = "MONSTER_GRASS" },
			{ id = "MONSTER_108", name = "Card_8", stage = "STAGE_2", prevEvoName = "Card_6", type = "MONSTER_GRASS" },
			{ id = "MONSTER_104", name = "Card_4", stage = "STAGE_1", prevEvoName = "Card_9", type = "MONSTER_GRASS" },
			{ id = "MONSTER_103", name = "Card_3", stage = "STAGE_2", prevEvoName = "Card_4", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_129", name = "Card_12", stage = "STAGE_2", prevEvoName = "Card_4", type = "MONSTER_GRASS" },
			{ id = "MONSTER_132", name = "Card_12", stage = "STAGE_2", prevEvoName = "Card_4", type = "MONSTER_GRASS" },

			-- shape: [1-2-0]
			-- All branches happened to match
			{ id = "MONSTER_138", name = "Card_15", stage = "BASIC", prevEvoName = "", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_121", name = "Card_11", stage = "STAGE_1", prevEvoName = "Card_15", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_122", name = "Card_11", stage = "STAGE_1", prevEvoName = "Card_15", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_107", name = "Card_7", stage = "STAGE_1", prevEvoName = "Card_15", type = "MONSTER_COLORLESS" },
		},
		3 + SEED_OFFSET_ADJUST
	),
	caseFor(
		"branch_root_avoid",
		{
			source = "ROM",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
			evoLinePool = policyBranchEvoLinePool,
			branchingBasesColorless = unbiasedPolicyArgs.branchingBasesColorless,
			branchRootConsistency = {
				["Unbiased"] = 0,
				["Match Branch Root"] = 0,
				["Avoid Branch Root"] = 1,
			},
			interBranchTypes = unbiasedPolicyArgs.interBranchTypes,
		},
		copyCards(policyBranchCards),
		copyCards(policyBranchCards),
		{
			-- shape: [1-3-2]
			-- Branch node doesn't match evo lines
			{ id = "MONSTER_107", name = "Card_7", stage = "BASIC", prevEvoName = "", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_131", name = "Card_14", stage = "STAGE_1", prevEvoName = "Card_7", type = "MONSTER_WATER" },
			{ id = "MONSTER_110", name = "Card_10", stage = "STAGE_2", prevEvoName = "Card_14", type = "MONSTER_WATER" },
			{ id = "MONSTER_139", name = "Card_16", stage = "STAGE_1", prevEvoName = "Card_7", type = "MONSTER_GRASS" },
			{ id = "MONSTER_104", name = "Card_4", stage = "STAGE_2", prevEvoName = "Card_16", type = "MONSTER_GRASS" },
			{ id = "MONSTER_109", name = "Card_9", stage = "STAGE_1", prevEvoName = "Card_7", type = "MONSTER_FIRE" },

			-- shape: [1-2-4]
			-- Again all don't match
			{ id = "MONSTER_105_1", name = "Card_5", stage = "BASIC", prevEvoName = "", type = "MONSTER_FIRE" },
			{ id = "MONSTER_105_2", name = "Card_5", stage = "BASIC", prevEvoName = "", type = "MONSTER_FIRE" },
			{ id = "MONSTER_108", name = "Card_8", stage = "STAGE_1", prevEvoName = "Card_5", type = "MONSTER_GRASS" },
			{ id = "MONSTER_121", name = "Card_11", stage = "STAGE_2", prevEvoName = "Card_8", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_122", name = "Card_11", stage = "STAGE_2", prevEvoName = "Card_8", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_130", name = "Card_13", stage = "STAGE_2", prevEvoName = "Card_8", type = "MONSTER_FIRE" },
			{ id = "MONSTER_103", name = "Card_3", stage = "STAGE_1", prevEvoName = "Card_5", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_101_1", name = "Card_1", stage = "STAGE_2", prevEvoName = "Card_3", type = "MONSTER_FIRE" },
			{ id = "MONSTER_101_2", name = "Card_1", stage = "STAGE_2", prevEvoName = "Card_3", type = "MONSTER_FIRE" },
			{ id = "MONSTER_106", name = "Card_6", stage = "STAGE_2", prevEvoName = "Card_3", type = "MONSTER_WATER" },

			-- shape: [1-2-0]
			-- This must be a lucky seed - these all don't match too - we happened to pick such that all could fulfil
			-- the conditions
			{ id = "MONSTER_138", name = "Card_15", stage = "BASIC", prevEvoName = "", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_129", name = "Card_12", stage = "STAGE_1", prevEvoName = "Card_15", type = "MONSTER_GRASS" },
			{ id = "MONSTER_132", name = "Card_12", stage = "STAGE_1", prevEvoName = "Card_15", type = "MONSTER_GRASS" },
			{ id = "MONSTER_102", name = "Card_2", stage = "STAGE_1", prevEvoName = "Card_15", type = "MONSTER_WATER" },
		},
		1 + SEED_OFFSET_ADJUST
	),
	caseFor(
		"branch_bases_colorless",
		{
			source = "ROM",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
			evoLinePool = policyBranchEvoLinePool,
			branchingBasesColorless = {
				["Colorless"] = 1,
				["Any Type"] = 0,
			},
			branchRootConsistency = unbiasedPolicyArgs.branchRootConsistency,
			interBranchTypes = unbiasedPolicyArgs.interBranchTypes,
		},
		copyCards(policyBranchCards),
		copyCards(policyBranchCards),
		{
			-- ALl use colorless as base
			-- shape: [1-3-2]
			{ id = "MONSTER_121", name = "Card_11", stage = "BASIC", prevEvoName = "", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_122", name = "Card_11", stage = "BASIC", prevEvoName = "", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_110", name = "Card_10", stage = "STAGE_1", prevEvoName = "Card_11", type = "MONSTER_WATER" },
			{ id = "MONSTER_106", name = "Card_6", stage = "STAGE_2", prevEvoName = "Card_10", type = "MONSTER_WATER" },
			{ id = "MONSTER_104", name = "Card_4", stage = "STAGE_1", prevEvoName = "Card_11", type = "MONSTER_GRASS" },
			{ id = "MONSTER_129", name = "Card_12", stage = "STAGE_2", prevEvoName = "Card_4", type = "MONSTER_GRASS" },
			{ id = "MONSTER_132", name = "Card_12", stage = "STAGE_2", prevEvoName = "Card_4", type = "MONSTER_GRASS" },
			{ id = "MONSTER_131", name = "Card_14", stage = "STAGE_1", prevEvoName = "Card_11", type = "MONSTER_WATER" },

			-- shape: [1-2-4]
			{ id = "MONSTER_103", name = "Card_3", stage = "BASIC", prevEvoName = "", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_101_1", name = "Card_1", stage = "STAGE_1", prevEvoName = "Card_3", type = "MONSTER_FIRE" },
			{ id = "MONSTER_101_2", name = "Card_1", stage = "STAGE_1", prevEvoName = "Card_3", type = "MONSTER_FIRE" },
			{ id = "MONSTER_130", name = "Card_13", stage = "STAGE_2", prevEvoName = "Card_1", type = "MONSTER_FIRE" },
			{ id = "MONSTER_102", name = "Card_2", stage = "STAGE_2", prevEvoName = "Card_1", type = "MONSTER_WATER" },
			{ id = "MONSTER_105_1", name = "Card_5", stage = "STAGE_1", prevEvoName = "Card_3", type = "MONSTER_FIRE" },
			{ id = "MONSTER_105_2", name = "Card_5", stage = "STAGE_1", prevEvoName = "Card_3", type = "MONSTER_FIRE" },
			{ id = "MONSTER_139", name = "Card_16", stage = "STAGE_2", prevEvoName = "Card_5", type = "MONSTER_GRASS" },
			{ id = "MONSTER_108", name = "Card_8", stage = "STAGE_2", prevEvoName = "Card_5", type = "MONSTER_GRASS" },

			-- shape: [1-2-0]
			{ id = "MONSTER_138", name = "Card_15", stage = "BASIC", prevEvoName = "", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_109", name = "Card_9", stage = "STAGE_1", prevEvoName = "Card_15", type = "MONSTER_FIRE" },
			{ id = "MONSTER_107", name = "Card_7", stage = "STAGE_1", prevEvoName = "Card_15", type = "MONSTER_COLORLESS" },
		},
		45 + SEED_OFFSET_ADJUST
	),

}
