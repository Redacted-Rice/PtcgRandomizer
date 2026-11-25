local common_field_defs = require("modules.util.common_field_defs")
local pool_utils = require("modules.util.pool_utils")

local module
module = {
	id = "hp_cards_together_and_stage",
	name = "Randomize HP (From Cards, Together or By Stage)",
	description = "Randomizes HP using existing card values either from one shared pool or keyed by evolution stage",
	groups = { "Monsters", "HP" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		common_field_defs.ARG_DEF_SOURCE,
		common_field_defs.ARG_DEF_DUPLICATES,
		common_field_defs.ARG_DEF_RANDOMIZATION_APPROACH,
		common_field_defs.ARG_DEF_STAGE_GROUPING_BY_STAGE,
	},
	execute = function(args)
		return module.randomizeHp(args)
	end,
}

function module.randomizeHp(args)
	local sourceCards = pool_utils.sourceCards(args.source)
	local targets = context.modified.cards:getRandomizableMonsterCards()
	local options = pool_utils.poolOptionsForCardSource(args)

	if args.grouping == "BY_STAGE" then
		pool_utils.buildGroupedPool(sourceCards, "stage", "hp", args.duplicates):useToRandomize(
				targets, "stage", "hp", options)
	else
		pool_utils.buildValuePool(sourceCards, "hp", args.duplicates):useToRandomize(targets,
				"hp", options)
	end
end

return module
