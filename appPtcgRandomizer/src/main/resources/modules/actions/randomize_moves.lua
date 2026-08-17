local randomizer = require("randomizer")
local pool_utils = require("modules.util.pool_utils")
local move_utils = require("modules.util.move_utils")

local module
module = {
	id = "randomize_moves",
	name = "Randomize Existing Moves",
	description = "Randomizes existing attacks and/or powers, either from one shared pool or grouped by evolution stage. Keeps the same number of moves per card",
	groups = { "Monsters", "Moves", "Attacks", "Powers" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = pool_utils.standardArgs({
		move_utils.moveKindArg(),
		move_utils.withinTypeArg(),
		pool_utils.groupingArg("ALL_TOGETHER"),
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
	local options = pool_utils.poolOptions(args.approach)

	local setter = function(target, move)
		target:getSourceCard():setMove(move, target:getSourceMoveIndex())
	end

	if args.grouping == "BY_STAGE" or args.withinType then
		local key = move_utils.groupKey(args)
		move_utils.buildGroupedPool(context, args, key):useToRandomize(moveTargets, key, setter,
			options)
	else
		move_utils.buildPool(context, args):useToRandomize(moveTargets, setter, options)
	end
end

return module
