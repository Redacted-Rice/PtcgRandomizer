local randomizer = require("randomizer")
local pool = require("modules.util.pool")

local module
module = {
	id = "randomize_moves",
	name = "Randomize Existing Moves",
	description = "Randomizes all moves (attacks and powers). Keeps the same number of moves per card but number of attacks and powers may change.",
	groups = { "Monsters", "Moves", "Attacks", "Powers" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = pool.standardArgs({
		{
			name = "withinType",
			displayName = "Within Type",
			definition = {
				type = "boolean",
			},
			default = false,
		},
	}),
	execute = function(context, args)
		return module.randomizeMoves(context, args)
	end,
}

-- One move per name so the pool is not weighted by how often a move appears
function module.uniqueMoves(moveList)
	return randomizer.groupBy(moveList, function(move)
		return move.name:toString()
	end):map(function(_, moves)
		return moves:get(1)
	end)
end

function module.buildMovePool(context, args)
	local movePool = randomizer.list(pool.sourceData(context, args.source):getRandomizableMoves(
		true, false))
	if args.duplicates == "REMOVE_DUPLICATES" then
		return module.uniqueMoves(movePool)
	end
	return movePool
end

function module.randomizeMoves(context, args)
	randomizer.changedetector.pushMoveChangeDisplay({ "name", "energyCost", "damage" })

	-- Targets always come from modified. Pool uses the chosen source set
	local moveTargets = randomizer.list(context.modified:getRandomizableMoves(false, false))
	local movePool = module.buildMovePool(context, args)
	local options = pool.poolOptions(args.approach)

	local setter = function(target, move)
		target:getSourceCard():setMove(move, target:getSourceMoveIndex())
	end

	if args.withinType then
		movePool:groupBy("getSourceCard:type"):useToRandomize(moveTargets, "getSourceCard:type",
			setter, options)
	else
		movePool:useToRandomize(moveTargets, setter, options)
	end
end

return module
