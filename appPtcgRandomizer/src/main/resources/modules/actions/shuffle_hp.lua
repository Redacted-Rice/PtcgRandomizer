local pool_utils = require("modules.util.pool_utils")

local module
module = {
	-- Keep shuffle_hp id so older presets still resolve (was stage grouped before args existed)
	id = "shuffle_hp",
	name = "Randomize HP using Existing Values",
	description = "Randomizes HP using existing card values, either from one shared pool or grouped by evolution stage",
	groups = { "Monsters", "HP" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = pool_utils.standardArgs({
		pool_utils.groupingArg("BY_STAGE"),
	}),
	execute = function(context, args)
		return module.randomizeHp(context, args)
	end,
}

function module.randomizeHp(context, args)
	local sourceCards = pool_utils.sourceCards(context, args.source)
	local targets = context.modified:getRandomizableMonsterCards()
	local options = pool_utils.poolOptions(args.approach)

	if args.grouping == "BY_STAGE" then
		pool_utils.buildGroupedPool(sourceCards, "stage", "hp", args.duplicates):useToRandomize(
			targets, "stage", "hp", options)
	else
		pool_utils.buildValuePool(sourceCards, "hp", args.duplicates):useToRandomize(targets,
			"hp", options)
	end
end

return module
