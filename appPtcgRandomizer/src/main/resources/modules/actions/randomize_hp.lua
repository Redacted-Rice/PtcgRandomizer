local pool = require("modules.util.pool")

local module
module = {
	id = "shuffle_hp",
	name = "Randomize HP using Existing Values",
	description = "Randomizes HP values of all Monster cards using existing values from the cards.",
	groups = { "Monsters", "HP" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
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
			default = "BY_STAGE_AND_MAX_STAGE",
		},
	}),
	execute = function(context, args)
		return module.randomizeHp(context, args)
	end,
}

function module.randomizeHp(context, args)
	local sourceCards = pool.sourceCards(context, args.source)
	local targets = context.modified:getRandomizableMonsterCards()
	local options = pool.poolOptions(args.approach)

	if args.grouping == "ALL_TOGETHER" then
		pool.buildValuePool(sourceCards, "getHp", args.duplicates):useToRandomize(targets, "setHp",
			options)
	elseif args.grouping == "BY_STAGE" then
		pool.buildGroupedPool(sourceCards, "stage", "getHp", args.duplicates):useToRandomize(
			targets, "stage", "setHp", options)
	else
		pool.buildGroupedPool(sourceCards, pool.stageAndMaxStageKey, "getHp", args.duplicates):
			useToRandomize(targets, pool.stageAndMaxStageKey, "setHp", options)
	end
end

return module
