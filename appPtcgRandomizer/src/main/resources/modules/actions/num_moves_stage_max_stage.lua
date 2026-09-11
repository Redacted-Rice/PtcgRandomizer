local common_field_defs = require("modules.util.common_field_defs")
local pool_utils = require("modules.util.pool_utils")

local module
module = {
	id = "num_moves_stage_max_stage",
	name = "Randomize Num Moves (By Stage + Max Stage)",
	description = "Randomizes the number of moves per card using pools grouped by evolution line max stage and card stage",
	groups = { "Monsters", "Support", "Moves", "Attacks", "Powers" },
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
		return module.randomizeNumMoves(context, args)
	end,
}

function module.randomizeNumMoves(context, args)
	local sourceCards = pool_utils.sourceCards(context, args.source)
	local targets = context.modified:getRandomizableMonsterCards()
	local options = pool_utils.poolOptions(args.approach)
	pool_utils.buildGroupedPool(sourceCards, pool_utils.stageAndMaxStageKey, "getNumMoves",
		args.duplicates):useToRandomize(targets, pool_utils.stageAndMaxStageKey, "setNumMoves",
		options)
end

return module
