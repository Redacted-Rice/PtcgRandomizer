local randomizer = require("randomizer")

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
			name = "grouping",
			definition = {
				type = "enum",
				constraint = "StageGrouping",
			},
			default = "ALL_TOGETHER",
		},
		{
			name = "approach",
			definition = {
				type = "enum",
				constraint = "RandomizationApproach",
			},
			default = "MINIMIZE_REPEATS",
		},
	},
	execute = function(context, args)
		return module.randomizeNumMoves(context, args)
	end,
}

function module.poolOptions(approach)
	if approach == "MINIMIZE_REPEATS" then
		return { consumable = true, regenerate = true }
	end
	return { consumable = false }
end

function module.sourceCards(context, source)
	if source == "MODIFIED" then
		return context.modified:getRandomizableMonsterCards()
	end
	return context.original:getRandomizableMonsterCards()
end

function module.stageAndMaxStageKey(mc)
	return mc.evoLineMaxStage:getValue() * 10 + mc.stage:getValue()
end

function module.uniqueValues(list)
	return randomizer.groupBy(list, function(value)
		return value
	end):map(function(value, _)
		return value
	end)
end

function module.buildValuePool(sourceCards, valueGetter, duplicates)
	local values = randomizer.list(sourceCards):select(valueGetter)
	if duplicates == "REMOVE_DUPLICATES" then
		return module.uniqueValues(values)
	end
	return values
end

function module.buildGroupedPool(sourceCards, groupKey, valueGetter, duplicates)
	local grouped = randomizer.groupFromField(sourceCards, groupKey, valueGetter)
	if duplicates == "KEEP_DUPLICATES" then
		return grouped
	end

	local selected = {}
	local keyOrder = {}
	grouped:each(function(key, list)
		selected[key] = module.uniqueValues(list)
		table.insert(keyOrder, key)
	end)
	return randomizer.group(selected, keyOrder)
end

function module.randomizeNumMoves(context, args)
	randomizer.changedetector.pushMoveChangeDisplay({ "name" })

	local sourceCards = module.sourceCards(context, args.source)
	local targets = context.modified:getRandomizableMonsterCards()
	local options = module.poolOptions(args.approach)

	if args.grouping == "ALL_TOGETHER" then
		module.buildValuePool(sourceCards, "getNumMoves", args.duplicates):useToRandomize(targets,
			"setNumMoves", options)
	elseif args.grouping == "BY_STAGE" then
		module.buildGroupedPool(sourceCards, "stage", "getNumMoves", args.duplicates):
			useToRandomize(targets, "stage", "setNumMoves", options)
	else
		module.buildGroupedPool(sourceCards, module.stageAndMaxStageKey, "getNumMoves",
			args.duplicates):useToRandomize(targets, module.stageAndMaxStageKey, "setNumMoves",
			options)
	end
end

return module
