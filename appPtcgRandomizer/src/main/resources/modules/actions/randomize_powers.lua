local randomizer = require("randomizer")

local module
module = {
	id = "randomize_powers",
	name = "Randomize Existing Powers",
	description = "Randomizes powers only (not attacks). Keeps the same number of powers per card.",
	groups = { "Monsters", "Moves", "Powers" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		{
			name = "source",
			definition = {
				type = "enum",
				constraint = "CardDataSource",
			},
			default = "ORIGINAL",
		},
		{
			name = "duplicates",
			definition = {
				type = "enum",
				constraint = "DuplicateHandling",
			},
			default = "KEEP_DUPLICATES",
		},
		{
			name = "approach",
			definition = {
				type = "enum",
				constraint = "RandomizationApproach",
			},
			default = "MINIMIZE_REPEATS",
		},
		{
			name = "withinType",
			definition = {
				type = "boolean",
			},
			default = false,
		},
	},
	execute = function(context, args)
		return module.randomizePokePowers(context, args)
	end,
}

function module.poolOptions(approach)
	if approach == "MINIMIZE_REPEATS" then
		return { consumable = true, regenerate = true }
	end
	return { consumable = false }
end

function module.sourceData(context, source)
	if source == "MODIFIED" then
		return context.modified
	end
	return context.original
end

function module.uniqueMoves(moveList)
	return randomizer.groupBy(moveList, function(move)
		return move.name:toString()
	end):map(function(_, moves)
		return moves:get(1)
	end)
end

function module.buildPowerPool(context, args)
	local pool = randomizer.list(module.sourceData(context, args.source):getRandomizableMoves(true,
		false)):filter("isPokePower")
	if args.duplicates == "REMOVE_DUPLICATES" then
		return module.uniqueMoves(pool)
	end
	return pool
end

function module.randomizePokePowers(context, args)
	randomizer.changedetector.pushMoveChangeDisplay({ "name" })

	local powerTargets = randomizer.list(context.modified:getRandomizableMoves(false, false)):
		filter("isPokePower")
	local powerPool = module.buildPowerPool(context, args)
	local options = module.poolOptions(args.approach)

	local setter = function(target, move)
		target:getSourceCard():setMove(move, target:getSourceMoveIndex())
	end

	if args.withinType then
		powerPool:groupBy("getSourceCard:type"):useToRandomize(powerTargets, "getSourceCard:type",
			setter, options)
	else
		powerPool:useToRandomize(powerTargets, setter, options)
	end
end

return module
