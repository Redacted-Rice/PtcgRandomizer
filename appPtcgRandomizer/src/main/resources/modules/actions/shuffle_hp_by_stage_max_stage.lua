local pool_utils = require("modules.util.pool_utils")

local module
module = {
	id = "shuffle_hp_by_stage_max_stage",
	name = "Randomize HP using Existing Values (By Stage and Max Stage)",
	description = "Randomizes HP using pools grouped by evolution line max stage and card stage",
	groups = { "Monsters", "HP" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	needs = {
		{ name = "evoLineMaxStage", type = "EvolutionStage" },
	},
	arguments = pool_utils.standardArgs(),
	execute = function(context, args)
		return module.randomizeHp(context, args)
	end,
}

function module.randomizeHp(context, args)
	local sourceCards = pool_utils.sourceCards(context, args.source)
	local targets = context.modified:getRandomizableMonsterCards()
	local options = pool_utils.poolOptions(args.approach)
	pool_utils.buildGroupedPool(sourceCards, pool_utils.stageAndMaxStageKey, "hp",
		args.duplicates):useToRandomize(targets, pool_utils.stageAndMaxStageKey, "hp", options)
end

return module
