-- Canonical enum value strings for module arguments and runtime comparisons

local enums_def = {}

-------------------- Java backed enum values --------------------

enums_def.RANDOMIZATION_APPROACH_FULLY_RANDOM = "FULLY_RANDOM"
enums_def.RANDOMIZATION_APPROACH_MINIMIZE_REPEATS = "MINIMIZE_REPEATS"

enums_def.DATA_SOURCE_ROM = "ROM"
enums_def.DATA_SOURCE_CURRENT = "CURRENT"

enums_def.DUPLICATE_HANDLING_REMOVE = "REMOVE_DUPLICATES"
enums_def.DUPLICATE_HANDLING_KEEP = "KEEP_DUPLICATES"

enums_def.STAGE_GROUPING_ALL_TOGETHER = "ALL_TOGETHER"
enums_def.STAGE_GROUPING_BY_STAGE = "BY_STAGE"
enums_def.STAGE_GROUPING_BY_STAGE_AND_MAX_STAGE = "BY_STAGE_AND_MAX_STAGE"

enums_def.MOVE_KIND_ALL_MOVES = "ALL_MOVES"
enums_def.MOVE_KIND_ATTACKS = "ATTACKS"
enums_def.MOVE_KIND_POWERS = "POWERS"

enums_def.EVO_STAGE_BASIC = "BASIC"
enums_def.EVO_STAGE_STAGE_1 = "STAGE_1"
enums_def.EVO_STAGE_STAGE_2 = "STAGE_2"
enums_def.EVO_STAGE_STAGE_2_WITHOUT_STAGE_1 = "STAGE_2_WITHOUT_STAGE_1"

enums_def.ENERGY_TYPE_FIRE = "FIRE"
enums_def.ENERGY_TYPE_GRASS = "GRASS"
enums_def.ENERGY_TYPE_LIGHTNING = "LIGHTNING"
enums_def.ENERGY_TYPE_WATER = "WATER"
enums_def.ENERGY_TYPE_FIGHTING = "FIGHTING"
enums_def.ENERGY_TYPE_PSYCHIC = "PSYCHIC"
enums_def.ENERGY_TYPE_COLORLESS = "COLORLESS"
enums_def.ENERGY_TYPE_UNUSED = "UNUSED_TYPE"

enums_def.ENERGY_TYPE_KEYS = {
	enums_def.ENERGY_TYPE_FIRE,
	enums_def.ENERGY_TYPE_GRASS,
	enums_def.ENERGY_TYPE_LIGHTNING,
	enums_def.ENERGY_TYPE_WATER,
	enums_def.ENERGY_TYPE_FIGHTING,
	enums_def.ENERGY_TYPE_PSYCHIC,
	enums_def.ENERGY_TYPE_COLORLESS,
}

-------------------- Lua string enum values --------------------

enums_def.TYPE_POOL_ALL_TOGETHER = "All Together"
enums_def.TYPE_POOL_WITHIN_TYPE = "Within Type"
enums_def.TYPE_POOL_WITHIN_SUBTYPE = "Within Subtype"

enums_def.BRANCHING_BASE_COLORLESS = "Colorless"
enums_def.BRANCHING_BASE_ANY_TYPE = "Any Type"

enums_def.BRANCH_ROOT_UNBIASED = "Unbiased"
enums_def.BRANCH_ROOT_MATCH = "Match Branch Root"
enums_def.BRANCH_ROOT_AVOID = "Avoid Branch Root"

enums_def.INTER_BRANCH_UNBIASED = "Unbiased"
enums_def.INTER_BRANCH_MATCH = "Match"
enums_def.INTER_BRANCH_DIFFER = "Different"

enums_def.FIX_EVO_LINE_MODE_MATCH_PREVIOUS = "Match Previous"
enums_def.FIX_EVO_LINE_MODE_REDISTRIBUTE = "Redistribute"

enums_def.BRANCH_HANDLING_ALL_TOGETHER = "All Together"
enums_def.BRANCH_HANDLING_INDIVIDUAL = "Individual Branches"
enums_def.BRANCH_HANDLING_SEPARATE = "Separate Branches"

return enums_def