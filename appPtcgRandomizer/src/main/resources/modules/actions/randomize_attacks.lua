local randomizer = require("randomizer")
local pool = require("modules.util.pool")

local module
module = {
	id = "randomize_attacks",
	name = "Randomize Existing Attacks",
	description = "Randomizes attacks only (not powers). Keeps the same number of attacks per card.",
	groups = { "Monsters", "Moves", "Attacks" },
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
		return module.randomizeAttacks(context, args)
	end,
}

function module.uniqueMoves(moveList)
	return randomizer.groupBy(moveList, function(move)
		return move.name:toString()
	end):map(function(_, moves)
		return moves:get(1)
	end)
end

function module.buildAttackPool(context, args)
	local attackPool = randomizer.list(pool.sourceData(context, args.source):getRandomizableMoves(
		true, false)):filter("isAttack")
	if args.duplicates == "REMOVE_DUPLICATES" then
		return module.uniqueMoves(attackPool)
	end
	return attackPool
end

function module.randomizeAttacks(context, args)
	randomizer.changedetector.pushMoveChangeDisplay({ "name", "energyCost", "damage" })

	local attackTargets = randomizer.list(context.modified:getRandomizableMoves(false, false)):
		filter("isAttack")
	local attackPool = module.buildAttackPool(context, args)
	local options = pool.poolOptions(args.approach)

	local setter = function(target, move)
		target:getSourceCard():setMove(move, target:getSourceMoveIndex())
	end

	if args.withinType then
		attackPool:groupBy("getSourceCard:type"):useToRandomize(attackTargets, "getSourceCard:type",
			setter, options)
	else
		attackPool:useToRandomize(attackTargets, setter, options)
	end
end

return module
