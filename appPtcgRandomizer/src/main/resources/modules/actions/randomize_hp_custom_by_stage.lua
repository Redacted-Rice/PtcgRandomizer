local hp_custom = require("modules.util.hp_custom")

local module
module = {
	id = "randomize_hp_custom_by_stage",
	name = "Randomize HP with Custom Values (By Stage)",
	description = "Randomizes HP using custom pools keyed by card stage. Repeat values in a list to weight them",
	groups = { "Monsters", "HP" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		hp_custom.approachArg(),
		{
			name = "hpPools",
			displayName = "HP Pools by Card's Evo Stage",
			definition = {
				type = "table",
				keyDefinition = hp_custom.evoStageKeyDef(),
				valueDefinition = {
					type = "list",
					elementDefinition = hp_custom.HP_LIST_ELEMENT,
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
	hp_custom.randomize(context, args,
		hp_custom.buildStagePoolGroup(context, args.hpPools), "stage")
end

return module
