-- Shared argument and type defs
-- Require as modules.util.common_field_defs
local enums_def = require("modules.util.enums_def")
local common_field_defs = {}

-- TODO: Rename remaining KEY_DEF to TYPE_DEF
-- TODO: Look for opportunities to consolidate similar ones
common_field_defs.TYPE_DEF_WEIGHT = {
	type = "int",
	constraint = { type = "range", min = 1, max = 100 },
}

-- Allows 0 so types can be excluded from a weighted pool
common_field_defs.TYPE_DEF_ZEROABLE_WEIGHT = {
	type = "int",
	constraint = { type = "range", min = 0, max = 100 },
}

common_field_defs.TYPE_DEF_HP = {
	type = "int",
	constraint = { type = "discrete_range", min = 10, max = 120, step = 10 },
}

common_field_defs.TYPE_DEF_HP_WEIGHTED = {
	type = "tuple",
	fields = {
		{ name = "weight", definition = common_field_defs.TYPE_DEF_WEIGHT },
		{ name = "value", definition = common_field_defs.TYPE_DEF_HP },
	},
}

-- ROM range is 0-3 but the UI allows up to 10 (todo later: actually determine value) before text runs over lines
common_field_defs.TYPE_DEF_RETREAT_COST = {
	type = "int",
	constraint = { type = "discrete_range", min = 0, max = 10, step = 1 },
}

common_field_defs.TYPE_DEF_RETREAT_COST_WEIGHTED = {
	type = "tuple",
	fields = {
		{ name = "weight", definition = common_field_defs.TYPE_DEF_WEIGHT },
		{ name = "value", definition = common_field_defs.TYPE_DEF_RETREAT_COST },
	},
}

common_field_defs.ARG_DEF_RANDOMIZATION_APPROACH = {
	name = "approach",
	displayName = "Randomization Approach",
	description = "How values are drawn from the pool. Fully random can result in repeated values or disproportional"
			.. " values. Minimize Repeats consumes values and refills when empty guaranteeing an even distribution",
	definition = {
		type = "enum",
		constraint = "RandomizationApproach",
	},
	default = enums_def.RANDOMIZATION_APPROACH_MINIMIZE_REPEATS,
}

common_field_defs.ARG_DEF_SOURCE = {
	name = "source",
	displayName = "Pool Source",
	description = "Which card set supplies values for the randomization pool",
	definition = {
		type = "enum",
		constraint = "DataSource",
	},
	default = enums_def.DATA_SOURCE_ROM,
}

common_field_defs.ARG_DEF_DUPLICATES = {
	name = "duplicates",
	displayName = "Duplicate Handling",
	description = "Whether duplicate pool values are kept (weighted by frequency) or reduced to one of each",
	definition = {
		type = "enum",
		constraint = "DuplicateHandling",
	},
	default = enums_def.DUPLICATE_HANDLING_KEEP,
}

common_field_defs.ARG_DEF_NO_DUPLICATE_MOVE_NAMES = {
	name = "noDuplicateMoveNames",
	displayName = "Prevent Duplicate Move Names per Card",
	description = "When enabled, a monster card will not receive the same move name on two different slots",
	definition = {
		type = "boolean",
	},
	default = true,
}

-- default ALL_TOGETHER. use ARG_DEF_STAGE_GROUPING_BY_STAGE when BY_STAGE is the module default
common_field_defs.ARG_DEF_STAGE_GROUPING_ALL_TOGETHER = {
	name = "grouping",
	displayName = "Evo Stage Grouping",
	description = "How source values are grouped into pools. 'All Together' uses a single pool. 'By Stage' groups by"
			.. " the card's evolution stage.",
	definition = {
		type = "enum",
		constraint = "StageGrouping",
		exclude = { enums_def.STAGE_GROUPING_BY_STAGE_AND_MAX_STAGE },
	},
	default = enums_def.STAGE_GROUPING_ALL_TOGETHER,
}

common_field_defs.ARG_DEF_STAGE_GROUPING_BY_STAGE = {
	name = common_field_defs.ARG_DEF_STAGE_GROUPING_ALL_TOGETHER.name,
	displayName = common_field_defs.ARG_DEF_STAGE_GROUPING_ALL_TOGETHER.displayName,
	description = common_field_defs.ARG_DEF_STAGE_GROUPING_ALL_TOGETHER.description,
	definition = common_field_defs.ARG_DEF_STAGE_GROUPING_ALL_TOGETHER.definition,
	default = enums_def.STAGE_GROUPING_BY_STAGE,
}

common_field_defs.ARG_DEF_EVO_LINE_STAGE_GROUPING = {
	name = "grouping",
	displayName = "Evo Stage Grouping",
	description = "By Stage And Max Stage keeps names and shuffles who they evolve from."
			.. " By Stage keeps line shape and stage but shuffles cards within each stage."
			.. " All Together keeps line shape but ignores stage when picking and changes stage"
			.. " appropriately.",
	definition = {
		type = "enum",
		constraint = "StageGrouping",
	},
	default = enums_def.STAGE_GROUPING_BY_STAGE_AND_MAX_STAGE,
}

common_field_defs.ARG_DEF_MOVE_KIND = {
	name = "moveKind",
	displayName = "Moves To Randomize",
	description = "'All Moves' randomizes attacks and powers together. 'Attacks' and 'Powers' keep each kind on its"
			.. " own slots",
	definition = {
		type = "enum",
		constraint = "MoveKind",
	},
	default = enums_def.MOVE_KIND_ALL_MOVES,
}

common_field_defs.ARG_DEF_WITHIN_TYPE = {
	name = "withinType",
	displayName = "Type Pool Scope",
	description = "'All Together' pools every card together."
			.. " 'Within Type' pools by cards' energy types."
			.. " 'Within Subtype' pools combining each card's energy type with its weakness and resistance.",
	definition = {
		type = "string",
		constraint = {
			type = "enum",
			values = {
				enums_def.TYPE_POOL_ALL_TOGETHER,
				enums_def.TYPE_POOL_WITHIN_TYPE,
				enums_def.TYPE_POOL_WITHIN_SUBTYPE,
			},
		},
	},
	default = enums_def.TYPE_POOL_ALL_TOGETHER,
}

-- Custom within-type evo lines always pool by type. Subtype adds weakness/resistance to the bucket.
common_field_defs.ARG_DEF_WITHIN_TYPE_NO_ALL_TOGETHER = {
	name = "withinType",
	displayName = "Type Pool Scope",
	description = "'Within Type' pools by cards' energy types."
			.. " 'Within Subtype' pools combining each card's energy type with its weakness and resistance.",
	definition = {
		type = "string",
		constraint = {
			type = "enum",
			values = {
				enums_def.TYPE_POOL_WITHIN_TYPE,
				enums_def.TYPE_POOL_WITHIN_SUBTYPE,
			},
		},
	},
	default = enums_def.TYPE_POOL_WITHIN_TYPE,
}

common_field_defs.ARG_DEF_EXCLUDE_TRAINERS_FROM_EVO_LINES = {
	name = "excludeTrainerProxies",
	displayName = "Exclude Trainers from Evo Lines",
	description = "When enabled, playable trainer will not be included in any evo line but will always be BASIC only"
			.. " cards. If not set, they are included and any of them can become the first stage of an evo line.",
	definition = {
		type = "boolean",
	},
	default = false,
}

common_field_defs.KEY_DEF_EVO_STAGE = {
	type = "enum",
	constraint = "EvoStage",
	exclude = { enums_def.EVO_STAGE_STAGE_2_WITHOUT_STAGE_1 },
}

common_field_defs.KEY_DEF_EVO_LINE_STAGES = {
	type = "enum",
	constraint = "NumEvoStages",
	exclude = { enums_def.EVO_STAGE_STAGE_2_WITHOUT_STAGE_1 },
}

common_field_defs.ARG_DEF_TYPE_WEIGHTS = {
	name = "typeWeights",
	displayName = "Type Weights",
	description = "Weights for each monster energy type in the randomization pool",
	definition = {
		type = "table",
		keyDefinition = {
			type = "enum",
			constraint = "EnergyType",
			exclude = { enums_def.ENERGY_TYPE_UNUSED },
		},
		valueDefinition = common_field_defs.TYPE_DEF_ZEROABLE_WEIGHT,
		fixedKeys = enums_def.ENERGY_TYPE_KEYS,
	},
	-- even spread except colorless a bit higher. not 2:3 so minimize reuse has room to vary
	default = {
		[enums_def.ENERGY_TYPE_FIRE] = 4,
		[enums_def.ENERGY_TYPE_GRASS] = 4,
		[enums_def.ENERGY_TYPE_LIGHTNING] = 4,
		[enums_def.ENERGY_TYPE_WATER] = 4,
		[enums_def.ENERGY_TYPE_FIGHTING] = 4,
		[enums_def.ENERGY_TYPE_PSYCHIC] = 4,
		[enums_def.ENERGY_TYPE_COLORLESS] = 6,
	},
}

common_field_defs.ARG_DEF_EVO_LINE_BRANCHING_BASES_COLORLESS = {
	name = "branchingBasesColorless",
	displayName = "Branching Base Type",
	description = "When a line branches, whether shared cards before the branch are forced to Colorless"
			.. " or can be any energy type",
	definition = {
		type = "table",
		keyDefinition = {
			type = "string",
			constraint = {
				type = "enum",
				values = {
					enums_def.BRANCHING_BASE_COLORLESS,
					enums_def.BRANCHING_BASE_ANY_TYPE,
				},
			},
		},
		valueDefinition = common_field_defs.TYPE_DEF_ZEROABLE_WEIGHT,
		fixedKeys = {
			enums_def.BRANCHING_BASE_COLORLESS,
			enums_def.BRANCHING_BASE_ANY_TYPE,
		},
	},
	default = {
		[enums_def.BRANCHING_BASE_COLORLESS] = 3,
		[enums_def.BRANCHING_BASE_ANY_TYPE] = 1,
	},
}


common_field_defs.ARG_DEF_EVO_LINE_BRANCH_ROOT_CONSISTENCY = {
	name = "branchRootConsistency",
	displayName = "Branch Root Type Consistency",
	description = "Whether at least one branch should match the basic's energy type or not. `Unbiased` means it does"
			.. " not consider this when selecting types, `Match Branch Root` means that at least one branch will"
			.. " match the branch root line, `Avoid Branch Root` means that all branches will"
			.. " try to be a different type than the branch root",
	definition = {
		type = "table",
		keyDefinition = {
			type = "string",
			constraint = {
				type = "enum",
				values = {
					enums_def.BRANCH_ROOT_UNBIASED,
					enums_def.BRANCH_ROOT_MATCH,
					enums_def.BRANCH_ROOT_AVOID,
				},
			},
		},
		valueDefinition = common_field_defs.TYPE_DEF_ZEROABLE_WEIGHT,
		fixedKeys = {
			enums_def.BRANCH_ROOT_UNBIASED,
			enums_def.BRANCH_ROOT_MATCH,
			enums_def.BRANCH_ROOT_AVOID,
		},
	},
	default = {
		[enums_def.BRANCH_ROOT_UNBIASED] = 2,
		[enums_def.BRANCH_ROOT_MATCH] = 7,
		[enums_def.BRANCH_ROOT_AVOID] = 1,
	},
}


common_field_defs.ARG_DEF_EVO_LINE_INTER_BRANCH_TYPES = {
	name = "interBranchTypes",
	displayName = "Inter Branch Types",
	description = "When selecting types for a  (after the first one), will it choose the same type as a branch or "
			.. " different type."
			.. " `Unbiased` means that it does not consider other branches when selecting types."
			.. " `Match` means it will choose the same type as another branch."
			.. " `Different` means it will choose a type not currently used by another branch.",
	definition = {
		type = "table",
		keyDefinition = {
			type = "string",
			constraint = {
				type = "enum",
				values = {
					enums_def.INTER_BRANCH_UNBIASED,
					enums_def.INTER_BRANCH_MATCH,
					enums_def.INTER_BRANCH_DIFFER,
				},
			},
		},
		valueDefinition = common_field_defs.TYPE_DEF_ZEROABLE_WEIGHT,
		fixedKeys = {
			enums_def.INTER_BRANCH_UNBIASED,
			enums_def.INTER_BRANCH_MATCH,
			enums_def.INTER_BRANCH_DIFFER,
		},
	},
	default = {
		[enums_def.INTER_BRANCH_UNBIASED] = 2,
		[enums_def.INTER_BRANCH_MATCH] = 1,
		[enums_def.INTER_BRANCH_DIFFER] = 7,
	},
}


-- Definition for a single evo line definition containing the counts
-- for each stage. All stages are displayed even if 0 and BASIC is fixed
-- to 1 (vanilla limitation that we can't converge evos but only divergent
-- ones due to single preEvo name. Display it anyways for clarity)
common_field_defs.TYPE_DEF_CUSTOM_EVO_LINE_COUNTS = {
	type = "table",
	keyDefinition = common_field_defs.KEY_DEF_EVO_STAGE,
	valueDefinition = {
		type = "int",
		constraint = { type = "range", min = 0, max = 100 },
	},
	fixedKeys = {
		enums_def.EVO_STAGE_BASIC,
		enums_def.EVO_STAGE_STAGE_1,
		enums_def.EVO_STAGE_STAGE_2,
	},
	fixedValues = { [enums_def.EVO_STAGE_BASIC] = 1 },
}

-- Single weighted evo line entry
common_field_defs.TYPE_DEF_CUSTOM_WEIGHTED_EVO_LINE = {
	type = "tuple",
	fields = {
		{
			name = "weight",
			definition = {
				type = "int",
				constraint = { type = "range", min = 1, max = 100 },
			},
		},
		{ name = "evoLine", definition = common_field_defs.TYPE_DEF_CUSTOM_EVO_LINE_COUNTS },
	},
}

common_field_defs.ARG_DEF_EVO_LINE_POOL = {
	name = "evoLinePool",
	displayName = "Evoution Lines Pool",
	description = "Weighted evolution lines. Each evo line lists how many cards appear at STAGE_1 and"
			.. " STAGE_2 (BASIC is always 1). STAGE_1 slots evolve from the basic. STAGE_2 slots are"
			.. " distributed as evenly as possible between STAGE_1 slots. \n"
			.. " For reference when building lines, there are 155 different named monster cards - 80 Basics"
			.. " (27 Basic/Basic, 26 Basic/Stage 1, 17 Basic/Stage 2), 57 Stage 1 (39 Stage 1/Stage 1,"
			.. " 18 Stage 1/Stage 2), and 18 Stage 2 cards.\n"
			.. " Note that if not using ALL_TOGETHER grouping, it will automatically adjust lines based on"
			.. " what is in the pools so the exact numbers may not match the weights depending on how many"
			.. " of each stage are left.",
	definition = {
		type = "list",
		elementDefinition = common_field_defs.TYPE_DEF_CUSTOM_WEIGHTED_EVO_LINE,
	},
	-- Semi arbitrary but fairly similar to the rom values but with some more
	-- branches just for funsies
	default = {
		{
			-- Similar for basic onlies - the 3 stage and 2 stage will
			-- still leave some that would evo out so account for them
			-- here too
			-- 80 - (11 + 1 + 1 + 1)(14) - (30 + 1 + 3)(34) = 32
			weight = 32,
			evoLine = {
				[enums_def.EVO_STAGE_BASIC] = 1,
				[enums_def.EVO_STAGE_STAGE_1] = 0,
				[enums_def.EVO_STAGE_STAGE_2] = 0,
			},
		},
		{
			-- first stage will prefer intermediates for 3 chain evos,
			-- but there will be some leftover for 2 chain evos to
			-- account for as well
			-- 57 - (11 + 1 + 1 + 1)(14) - 3 - (2 * 4)(8) = 32
			weight = 34,
			evoLine = {
				[enums_def.EVO_STAGE_BASIC] = 1,
				[enums_def.EVO_STAGE_STAGE_1] = 1,
				[enums_def.EVO_STAGE_STAGE_2] = 0,
			},
		},
		{
			-- 2 stage will fill nicely
			-- 18 - 2 - 2 -3 = 11
			weight = 11,
			evoLine = {
				[enums_def.EVO_STAGE_BASIC] = 1,
				[enums_def.EVO_STAGE_STAGE_1] = 1,
				[enums_def.EVO_STAGE_STAGE_2] = 1,
			},
		},
		{
			-- Energy type stone based evo line
			weight = 1,
			evoLine = {
				[enums_def.EVO_STAGE_BASIC] = 1,
				[enums_def.EVO_STAGE_STAGE_1] = 3,
				[enums_def.EVO_STAGE_STAGE_2] = 0,
			},
		},
		{
			-- Fossil evo line
			weight = 1,
			evoLine = {
				[enums_def.EVO_STAGE_BASIC] = 1,
				[enums_def.EVO_STAGE_STAGE_1] = 3,
				[enums_def.EVO_STAGE_STAGE_2] = 2,
			},
		},
		{
			-- Just one for fun
			weight = 1,
			evoLine = {
				[enums_def.EVO_STAGE_BASIC] = 1,
				[enums_def.EVO_STAGE_STAGE_1] = 1,
				[enums_def.EVO_STAGE_STAGE_2] = 2,
			},
		},
		{
			-- Getting a bit crazy with this one
			weight = 1,
			evoLine = {
				[enums_def.EVO_STAGE_BASIC] = 1,
				[enums_def.EVO_STAGE_STAGE_1] = 2,
				[enums_def.EVO_STAGE_STAGE_2] = 3,
			},
		},
		{
			-- Just to make things a bit more interesting for others too
			weight = 4,
			evoLine = {
				[enums_def.EVO_STAGE_BASIC] = 1,
				[enums_def.EVO_STAGE_STAGE_1] = 2,
				[enums_def.EVO_STAGE_STAGE_2] = 0,
			},
		},
	},
}


return common_field_defs
