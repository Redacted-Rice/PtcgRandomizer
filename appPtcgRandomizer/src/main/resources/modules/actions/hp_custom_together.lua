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
			description = "Shared weighted HP values used for every card. Repeat a value in the list to make it more likely",
			definition = {
				type = "list",
				elementDefinition = common_field_defs.ELEMENT_DEF_HP_LIST,
			},
			default = {
				30, 40, 40, 50, 50, 50, 60, 60, 60, 70, 70, 70, 80, 80, 90, 90, 100, 100,
				110, 120,
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
	custom_pool_utils.hp.listPool(args.hpPool):useToRandomize(targets, "hp", options)
end

return module
