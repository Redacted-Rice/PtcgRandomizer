-- Shared argument field defs for HP, retreat cost, pool, and move modules.
-- Require as modules.util.common_field_defs
local common_field_defs = {}

common_field_defs.ELEMENT_DEF_HP_LIST = {
	type = "int",
	constraint = { type = "discrete_range", min = 10, max = 120, step = 10 },
}

-- ROM range is 0-3 but the UI allows up to 10 (todo later: actually determine value) before text runs over lines
common_field_defs.ELEMENT_DEF_RETREAT_COST_LIST = {
	type = "int",
	constraint = { type = "discrete_range", min = 0, max = 10, step = 1 },
}

common_field_defs.ARG_DEF_RANDOMIZATION_APPROACH = {
	name = "approach",
	displayName = "Randomization Approach",
	description = "How values are drawn from the pool. Minimize Repeats consumes values and refills when empty",
	definition = {
		type = "enum",
		constraint = "RandomizationApproach",
	},
	default = "MINIMIZE_REPEATS",
}

common_field_defs.ARG_DEF_SOURCE = {
	name = "source",
	displayName = "Pool Source",
	description = "Which card set supplies values for the randomization pool",
	definition = {
		type = "enum",
		constraint = "DataSource",
	},
	default = "ROM",
}

common_field_defs.ARG_DEF_DUPLICATES = {
	name = "duplicates",
	displayName = "Duplicate Handling",
	description = "Whether duplicate pool values are kept (weighted by frequency) or reduced to one of each",
	definition = {
		type = "enum",
		constraint = "DuplicateHandling",
	},
	default = "KEEP_DUPLICATES",
}

-- default ALL_TOGETHER. use ARG_DEF_STAGE_GROUPING_BY_STAGE when BY_STAGE is the module default
common_field_defs.ARG_DEF_STAGE_GROUPING_ALL_TOGETHER = {
	name = "grouping",
	displayName = "Evo Stage Grouping",
	description = "How source values are grouped into pools. 'All Together' uses a single pool. 'By Stage' groups by the card's evolution stage.",
	definition = {
		type = "enum",
		constraint = "StageGrouping",
		exclude = { "BY_STAGE_AND_MAX_STAGE" },
	},
	default = "ALL_TOGETHER",
}

common_field_defs.ARG_DEF_STAGE_GROUPING_BY_STAGE = {
	name = "grouping",
	displayName = "Evo Stage Grouping",
	description = "How source values are grouped into pools. 'All Together' uses a single pool. 'By Stage' groups by the card's evolution stage.",
	definition = {
		type = "enum",
		constraint = "StageGrouping",
		exclude = { "BY_STAGE_AND_MAX_STAGE" },
	},
	default = "BY_STAGE",
}

common_field_defs.ARG_DEF_MOVE_KIND = {
	name = "moveKind",
	displayName = "Moves To Randomize",
	description = "'All Moves' randomizes attacks and powers together. 'Attacks' and 'Powers' keep each kind on its own slots",
	definition = {
		type = "enum",
		constraint = "MoveKind",
	},
	default = "ALL_MOVES",
}

common_field_defs.ARG_DEF_WITHIN_TYPE = {
	name = "withinType",
	displayName = "Within Energy Type",
	description = "When enabled moves are pooled by Energy type so, for example, a fire type card will only get moves from Fire type cards",
	definition = {
		type = "boolean",
	},
	default = false,
}

common_field_defs.KEY_DEF_EVO_STAGE = {
	type = "enum",
	constraint = "EvoStage",
}

common_field_defs.KEY_DEF_EVO_LINE_STAGES = {
	type = "enum",
	constraint = "EvoLineStages",
}

return common_field_defs
