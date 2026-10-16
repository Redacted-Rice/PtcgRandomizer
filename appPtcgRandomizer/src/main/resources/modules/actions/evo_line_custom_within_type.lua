-- Custom weighted evo lines with per branch energy type pooling and branch aware type rules.
-- withinType here means each branch picks one energy type for all its slots
-- This is slighlty different than other usage of withinType but I think at
-- a high level is a similar concept so it makes sense to "reuse" the term
local common_field_defs = require("modules.util.common_field_defs")
local evo_line_randomize_utils = require("modules.util.evo_line_randomize_utils")

local module
module = {
	id = "evo_line_custom_within_type",
	name = "Randomize Evolution Lines (Custom, Within Type)",
	description = "Like custom evolution line randomization, but each branch picks one energy type for"
			.. " all its slots. Branching bases and branch paths can be biased toward Colorless, the"
			.. " basic's type, or each other via weighted choices",
	groups = { "Monsters", "Evolutions" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	needs = {
		{ name = "evoLineId", type = "integer" },
		{ name = "evoLineMaxStage", type = "EvolutionStage" },
	},
	arguments = {
		common_field_defs.ARG_DEF_SOURCE,
		common_field_defs.ARG_DEF_RANDOMIZATION_APPROACH,
		common_field_defs.ARG_DEF_EVO_LINE_STAGE_GROUPING,
		common_field_defs.ARG_DEF_EVO_LINE_BRANCHING_BASES_COLORLESS,
		common_field_defs.ARG_DEF_EVO_LINE_BRANCH_ROOT_CONSISTENCY,
		common_field_defs.ARG_DEF_EVO_LINE_INTER_BRANCH_TYPES,
		common_field_defs.ARG_DEF_EVO_LINE_POOL,
	},
	execute = function(context, args)
		return module.randomizeEvoLinesWithinType(context, args)
	end,
}

function module.randomizeEvoLinesWithinType(context, args)
	evo_line_randomize_utils.init(context)
	local choicePools = evo_line_randomize_utils.buildTypePolicyPools(
			args.branchingBasesColorless,
			args.branchRootConsistency,
			args.interBranchTypes,
			args.approach)
	return evo_line_randomize_utils.randomizeCustomEvoLines(context, args, choicePools)
end

return module
