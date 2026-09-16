local common_field_defs = require("modules.util.common_field_defs")
local custom_pool_utils = require("modules.util.custom_pool_utils")
local pool_utils = require("modules.util.pool_utils")

local module
module = {
	id = "retreat_cost_custom_stage",
	name = "Randomize Retreat Cost (Custom, By Stage)",
	description = "Randomizes retreat cost using custom values keyed by card stage",
	groups = { "Monsters", "Retreat Cost" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		common_field_defs.ARG_DEF_RANDOMIZATION_APPROACH,
		{
			name = "retreatPools",
			displayName = "Retreat Cost Pools by Stage",
			description = "Weighted retreat cost values for each card's evolution stage. When randomizing it will take"
							.. " a value from the pool that matches the current card's evolution stage",
			definition = {
				type = "table",
				keyDefinition = common_field_defs.KEY_DEF_EVO_STAGE,
				valueDefinition = {
					type = "list",
					elementDefinition = common_field_defs.ELEMENT_DEF_RETREAT_COST_LIST,
				},
			},
			default = {
				BASIC = { 0, 0, 1, 1, 1, 1, 2, 3 },
				STAGE_1 = { 0, 1, 1, 1, 2, 2, 2, 3 },
				STAGE_2 = { 1, 2, 2, 2, 3, 3, 3 },
			},
		},
	},
	execute = function(context, args)
		return module.randomizeRetreatCost(context, args)
	end,
}

function module.randomizeRetreatCost(context, args)
	local targets = context.modified:getRandomizableMonsterCards()
	local options = pool_utils.poolOptions(args.approach)
	custom_pool_utils.retreat.buildStagePoolGroup(context, args.retreatPools, targets):useToRandomize(
			targets, "stage", "retreatCost", options)
end

return module
