local common_field_defs = require("modules.util.common_field_defs")
local pool_utils = require("modules.util.pool_utils")
local types_util = require("modules.util.types_util")

-- Keeps each evo line on the same type drawn from a custom weighted type pool.
local module
module = {
	id = "types_custom_evo_lines",
	name = "Randomize Evo Line Types (Custom)",
	description = "Randomizes the energy type for each card in each evolution line to the same type"
			.. " using custom type weights",
	groups = { "Monsters", "Energy Type", "Evolutions" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	needs = {
		{ name = "evoLineId", type = "integer" },
	},
	arguments = {
		common_field_defs.ARG_DEF_RANDOMIZATION_APPROACH,
		common_field_defs.ARG_DEF_TYPE_WEIGHTS,
	},
	execute = function(context, args)
		return module.randomizeEvoLineTypes(context, args)
	end,
}

function module.randomizeEvoLineTypes(context, args)
	types_util.randomizeEvoLineTypes(context,
			types_util.buildTypePoolFromWeights(context, args.typeWeights),
			pool_utils.poolOptions(args.approach))
end

return module
