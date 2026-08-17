local hp_custom_utils = require("modules.util.hp_custom_utils")
local pool_utils = require("modules.util.pool_utils")

local module
module = {
	id = "randomize_hp_custom_by_stage",
	name = "Randomize HP with Custom Values (By Stage)",
	description = "Randomizes HP using custom pools keyed by card stage",
	groups = { "Monsters", "HP" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		hp_custom_utils.approachArg(),
		{
			name = "hpPools",
			displayName = "HP Pools by Stage",
			description = "Weighted HP values for each card's evolution stage. When randomizing it will take a value from the pool that matches the current card's evolution stage",
			definition = {
				type = "table",
				keyDefinition = hp_custom_utils.evoStageKeyDef(),
				valueDefinition = {
					type = "list",
					elementDefinition = hp_custom_utils.HP_LIST_ELEMENT,
				},
			},
			default = {
				BASIC = { 30, 40, 40, 50, 50, 60, 70, 80, 90, 100, 110, 120 },
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
	hp_custom_utils.buildStagePoolGroup(context, args.hpPools, targets):useToRandomize(targets,
		"stage", "setHp", options)
end

return module
