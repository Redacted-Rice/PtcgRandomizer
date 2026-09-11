local common_field_defs = require("modules.util.common_field_defs")
local pool_utils = require("modules.util.pool_utils")

local module
module = {
	id = "num_moves_together_and_stage",
	name = "Randomize Num Moves",
	description = "Randomizes the number of moves per card, either from one shared pool or grouped by evolution stage",
	groups = { "Monsters", "Support", "Moves", "Attacks", "Powers" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		common_field_defs.ARG_DEF_SOURCE,
		common_field_defs.ARG_DEF_DUPLICATES,
		common_field_defs.ARG_DEF_RANDOMIZATION_APPROACH,
		common_field_defs.ARG_DEF_STAGE_GROUPING_ALL_TOGETHER,
	},
	execute = function(context, args)
		return module.randomizeNumMoves(context, args)
	end,
}

function module.randomizeNumMoves(context, args)
	local sourceCards = pool_utils.sourceCards(context, args.source)
	local targets = context.modified:getRandomizableMonsterCards()
	local options = pool_utils.poolOptions(args.approach)

	if args.grouping == "BY_STAGE" then
		pool_utils.buildGroupedPool(sourceCards, "stage", "getNumMoves", args.duplicates):
			useToRandomize(targets, "stage", "setNumMoves", options)
	else
		pool_utils.buildValuePool(sourceCards, "getNumMoves", args.duplicates):useToRandomize(
			targets, "setNumMoves", options)
	end
end

return module
