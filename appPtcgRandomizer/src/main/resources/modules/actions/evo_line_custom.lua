-- Randomizes evolution lines using weighted custom evo lines ignoring card types
local common_field_defs = require("modules.util.common_field_defs")
local evo_line_randomize_utils = require("modules.util.evo_line_randomize_utils")

local module
module = {
	id = "evo_line_custom",
	name = "Randomize Evolution Lines (Custom, All Together)",
	description = "Randomizes evolution lines with custom defined evo line data. Grouping controls"
			.. " whether evolutions, stages, or neither are preserved. Need to re-run set evo line"
			.. " metadata if needed after this",
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
		common_field_defs.ARG_DEF_EVO_LINE_POOL,
	},
	execute = function(context, args)
		evo_line_randomize_utils.init(context)
		return evo_line_randomize_utils.randomizeCustomEvoLines(context, args, nil)
	end,
}

return module
