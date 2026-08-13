local hp_custom = require("modules.util.hp_custom")

local module
module = {
	id = "randomize_hp_custom_all_together",
	name = "Randomize HP with Custom Values (All Together)",
	description = "Randomizes HP from one shared custom pool for every card",
	groups = { "Monsters", "HP" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		hp_custom.approachArg(),
		{
			name = "hpPool",
			displayName = "HP Pool",
			description = "Shared weighted HP values used for every card. Repeat a value in the list to make it more likely",
			definition = {
				type = "list",
				elementDefinition = hp_custom.HP_LIST_ELEMENT,
			},
			default = {
				30, 30, 40, 40, 40, 50, 50, 50, 60, 60, 60, 70, 70, 70, 80, 80, 90, 90, 100, 100,
				110, 120,
			},
		},
	},
	execute = function(context, args)
		return module.randomizeHp(context, args)
	end,
}

function module.randomizeHp(context, args)
	hp_custom.randomize(context, args, hp_custom.listPool(args.hpPool))
end

return module
