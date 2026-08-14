local randomizer = require("randomizer")
local pool_utils = require("modules.util.pool_utils")
local move_utils = require("modules.util.move_utils")

local module
module = {
	id = "randomize_moves_by_stage_max_stage",
	name = "Randomize Existing Moves (By Stage and Max Stage)",
	description = "Randomizes existing attacks and/or powers using pools grouped by evolution line max stage and card stage. Keeps the same number of moves per card.",
	groups = { "Monsters", "Moves", "Attacks", "Powers" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	needs = {
		{ name = "evoLineMaxStage", type = "EvolutionStage" },
	},
	arguments = pool_utils.standardArgs({
		move_utils.moveKindArg(),
		move_utils.withinTypeArg(),
	}),
	execute = function(context, args)
		return module.randomizeMoves(context, args)
	end,
}

function module.randomizeMoves(context, args)
	if args.moveKind == "POWERS" then
		randomizer.changedetector.pushMoveChangeDisplay({ "name" })
	else
		randomizer.changedetector.pushMoveChangeDisplay({ "name", "energyCost", "damage" })
	end

	local moveTargets = move_utils.targets(context, args)
	local movePool = move_utils.buildPool(context, args)
	local options = pool_utils.poolOptions(args.approach)
	local key = move_utils.stageAndMaxStageGroupKey(args)

	local setter = function(target, move)
		target:getSourceCard():setMove(move, target:getSourceMoveIndex())
	end

	movePool:groupBy(key):useToRandomize(moveTargets, key, setter, options)
end

return module
