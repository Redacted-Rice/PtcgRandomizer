local common_field_defs = require("modules.util.common_field_defs")
local pool_utils = require("modules.util.pool_utils")

local module
module = {
	id = "retreat_cost_cards_together_and_stage",
	name = "Randomize Retreat Cost (From Cards, Together or By Stage)",
	description = "Randomizes retreat cost using existing card values either from one shared pool or keyed by"
					.. " evolution stage",
	groups = { "Monsters", "Retreat Cost" },
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
	execute = function(context, args)
		return module.randomizeRetreatCost(context, args)
	end,
}

function module.randomizeRetreatCost(context, args)
	local sourceCards = pool_utils.sourceCards(context, args.source)
	local targets = context.modified:getRandomizableMonsterCards()
	local options = pool_utils.poolOptions(args.approach)

	if args.grouping == "BY_STAGE" then
		pool_utils.buildGroupedPool(sourceCards, "stage", "retreatCost", args.duplicates):
				useToRandomize(targets, "stage", "retreatCost", options)
	else
		pool_utils.buildValuePool(sourceCards, "retreatCost", args.duplicates):useToRandomize(
				targets, "retreatCost", options)
	end
end

return module
