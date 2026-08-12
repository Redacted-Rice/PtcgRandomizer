local randomizer = require("randomizer")
local pool = require("modules.util.pool")

local module
module = {
	id = "randomize_num_moves",
	name = "Randomize Num Moves",
	description = "Randomizes the number of moves (attacks or powers) per card",
	groups = { "Monsters", "Support", "Moves", "Attacks", "Powers" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	provides = {
		{ name = "numMoves", type = "integer" },
	},
	needs = {
		{ name = "evoLineMaxStage", type = "EvolutionStage" },
	},
	arguments = pool.standardArgs({
		{
			name = "grouping",
			definition = {
				type = "enum",
				constraint = "StageGrouping",
			},
			default = "ALL_TOGETHER",
		},
	}),
	execute = function(context, args)
		return module.randomizeNumMoves(context, args)
	end,
}

function module.randomizeNumMoves(context, args)
	randomizer.changedetector.pushMoveChangeDisplay({ "name" })

	local sourceCards = pool.sourceCards(context, args.source)
	local targets = context.modified:getRandomizableMonsterCards()
	local options = pool.poolOptions(args.approach)

	if args.grouping == "ALL_TOGETHER" then
		pool.buildValuePool(sourceCards, "getNumMoves", args.duplicates):useToRandomize(targets,
			"setNumMoves", options)
	elseif args.grouping == "BY_STAGE" then
		pool.buildGroupedPool(sourceCards, "stage", "getNumMoves", args.duplicates):
			useToRandomize(targets, "stage", "setNumMoves", options)
	else
		pool.buildGroupedPool(sourceCards, pool.stageAndMaxStageKey, "getNumMoves",
			args.duplicates):useToRandomize(targets, pool.stageAndMaxStageKey, "setNumMoves",
			options)
	end
end

return module
