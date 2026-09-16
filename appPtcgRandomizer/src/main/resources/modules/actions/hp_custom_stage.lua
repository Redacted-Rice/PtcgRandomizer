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
					elementDefinition = common_field_defs.ELEMENT_DEF_HP_LIST,
				},
			},
			default = {
				BASIC = { 30, 40, 40, 50, 50, 60, 70, 80, 90, 100, 120 },
				STAGE_1 = { 50, 60, 60, 70, 70, 80, 90, 100 },
				STAGE_2 = { 80, 90, 90, 100, 100, 110, 120 },
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
