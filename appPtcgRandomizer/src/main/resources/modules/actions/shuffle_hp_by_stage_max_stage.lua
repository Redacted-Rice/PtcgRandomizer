local common_field_defs = require("modules.util.common_field_defs")
local pool_utils = require("modules.util.pool_utils")

local module
module = {
	id = "hp_cards_stage_max_stage",
	name = "Randomize HP (From Cards, By Stage + Max Stage)",
	description = "Randomizes HP using existing card values grouped by evolution line max stage and card stage",
	groups = { "Monsters", "HP" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	needs = {
		{ name = "evoLineMaxStage", type = "EvolutionStage" },
	},
	arguments = {
		common_field_defs.ARG_DEF_SOURCE,
		common_field_defs.ARG_DEF_DUPLICATES,
		common_field_defs.ARG_DEF_RANDOMIZATION_APPROACH,
	},
	execute = function(context, args)
		return module.randomizeHp(context, args)
	end,
}

function module.randomizeHp(context, args)
	local sourceCards = pool_utils.sourceCards(context, args.source)
	local targets = context.modified:getRandomizableMonsterCards()
	local options = pool_utils.poolOptions(args.approach)
	pool_utils.buildGroupedPool(sourceCards, pool_utils.stageAndMaxStageKey,
			"hp", args.duplicates):useToRandomize(targets,
				pool_utils.stageAndMaxStageKey, "hp", options)
end

return module
