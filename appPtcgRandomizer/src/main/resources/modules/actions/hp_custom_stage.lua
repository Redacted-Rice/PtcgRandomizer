local common_field_defs = require("modules.util.common_field_defs")
local custom_pool_utils = require("modules.util.custom_pool_utils")
local pool_utils = require("modules.util.pool_utils")

local module
module = {
	id = "hp_custom_stage",
	name = "Randomize HP (Custom, By Stage)",
	description = "Randomizes HP using custom values keyed by card stage",
	groups = { "Monsters", "HP" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		common_field_defs.ARG_DEF_RANDOMIZATION_APPROACH,
		{
			name = "hpPools",
			displayName = "HP Pools by Stage",
			description = "Weighted HP values for each card's evolution stage. When randomizing it will take a value"
							.. " from the pool that matches the current card's evolution stage",
			definition = {
				type = "table",
				keyDefinition = common_field_defs.KEY_DEF_EVO_STAGE,
				valueDefinition = {
					type = "list",
					elementDefinition = common_field_defs.TYPE_DEF_HP_WEIGHTED,
				},
			},
			default = {
				BASIC = {
					{ weight = 1, value = 30 },
					{ weight = 2, value = 40 },
					{ weight = 2, value = 50 },
					{ weight = 1, value = 60 },
					{ weight = 1, value = 70 },
					{ weight = 1, value = 80 },
					{ weight = 1, value = 90 },
					{ weight = 1, value = 100 },
					{ weight = 1, value = 120 },
				},
				STAGE_1 = {
					{ weight = 1, value = 50 },
					{ weight = 2, value = 60 },
					{ weight = 2, value = 70 },
					{ weight = 1, value = 80 },
					{ weight = 1, value = 90 },
					{ weight = 1, value = 100 },
				},
				STAGE_2 = {
					{ weight = 1, value = 80 },
					{ weight = 2, value = 90 },
					{ weight = 2, value = 100 },
					{ weight = 1, value = 110 },
					{ weight = 1, value = 120 },
				},
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
	custom_pool_utils.hp.buildStagePoolGroup(context, args.hpPools, targets):useToRandomize(
			targets, "stage", "hp", options)
end

return module
