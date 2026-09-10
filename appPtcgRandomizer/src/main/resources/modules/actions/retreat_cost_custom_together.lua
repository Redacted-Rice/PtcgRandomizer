local common_field_defs = require("modules.util.common_field_defs")
local custom_pool_utils = require("modules.util.custom_pool_utils")
local pool_utils = require("modules.util.pool_utils")

local module
module = {
	id = "retreat_cost_custom_together",
	name = "Randomize Retreat Cost (Custom, All Together)",
	description = "Randomizes retreat cost using custom values from one shared pool",
	groups = { "Monsters", "Retreat Cost" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		common_field_defs.ARG_DEF_RANDOMIZATION_APPROACH,
		{
			name = "retreatPool",
			displayName = "Retreat Cost Pool",
			description = "Shared weighted retreat cost values used for every card. Repeat a value in the list to make it more likely",
			definition = {
				type = "list",
				elementDefinition = common_field_defs.ELEMENT_DEF_RETREAT_COST_LIST,
			},
			default = { 0, 1, 1, 1, 1, 1, 2, 2, 2, 3 },
		},
	},
	execute = function(context, args)
		return module.randomizeRetreatCost(context, args)
	end,
}

function module.randomizeRetreatCost(context, args)
	local targets = context.modified:getRandomizableMonsterCards()
	local options = pool_utils.poolOptions(args.approach)
	custom_pool_utils.retreat.listPool(args.retreatPool):useToRandomize(targets, "retreatCost",
			options)
end

return module
