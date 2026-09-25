local common_field_defs = require("modules.util.common_field_defs")
local custom_pool_utils = require("modules.util.custom_pool_utils")
local pool_utils = require("modules.util.pool_utils")

local module
module = {
	id = "hp_custom_together",
	name = "Randomize HP (Custom, All Together)",
	description = "Randomizes HP using custom values from one shared pool",
	groups = { "Monsters", "HP" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		common_field_defs.ARG_DEF_RANDOMIZATION_APPROACH,
		{
			name = "hpPool",
			displayName = "HP Pool",
			description = "Shared weighted HP values used for every card",
			definition = {
				type = "list",
				elementDefinition = common_field_defs.TYPE_DEF_HP_WEIGHTED,
			},
			default = {
				{ weight = 1, value = 30 },
				{ weight = 2, value = 40 },
				{ weight = 3, value = 50 },
				{ weight = 3, value = 60 },
				{ weight = 3, value = 70 },
				{ weight = 2, value = 80 },
				{ weight = 2, value = 90 },
				{ weight = 2, value = 100 },
				{ weight = 1, value = 110 },
				{ weight = 1, value = 120 },
			},
		},
	},
	execute = function(context, args)
		return module.randomizeHp(context, args)
	end,
}

function module.randomizeHp(context, args)
	local targets = context.modified:getRandomizableMonsterCards()
	local options = pool_utils.poolOptions(args.approach)
	custom_pool_utils.hp.expandWeightedList(args.hpPool):useToRandomize(targets, "hp", options)
end

return module
