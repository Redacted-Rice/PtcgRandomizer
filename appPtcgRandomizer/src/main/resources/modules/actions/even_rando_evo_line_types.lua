local randomizer = require("randomizer")

local module
module = {
	id = "even_rando_evo_line_types",
	name = "Randomize Evo Line Types",
	description = "Randomizes the energy type for each card in each evolution line to the same type",
	groups = { "Monsters", "Energy Type", "Evolutions" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	needs = {
		{ name = "evoLineId", type = "integer" },
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
	},
	execute = function(context, args)
		return module.randomizeEvoLineTypes(context, args)
	end,
}

-- Maps approach to URC pool options. Minimize repeats always regenerates when empty
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

function module.buildTypePool(context, args)
	local sourceCards = module.sourceCards(context, args.source)

	if args.duplicates == "KEEP_DUPLICATES" then
		-- Keep source multiplicity so common types stay more common
		return randomizer.list(sourceCards):select("type")
	end

	-- One of each type so every type has equal weight in the pool
	return randomizer.groupBy(sourceCards, "type"):map(function(_, cards)
		return cards:get(1).type
	end)
end

function module.randomizeEvoLineTypes(context, args)
	local monsterCards = context.modified:getRandomizableMonsterCards()
	local typePool = module.buildTypePool(context, args)

	-- Get one card from each evo line to set the type of
	local byEvoLine = randomizer.groupBy(monsterCards, "evoLineId")
	local representatives = byEvoLine:map(function(_, line)
		return line:get(1)
	end)

	typePool:useToRandomize(representatives, "type", module.poolOptions(args.approach))

	-- Copy each line's type from its first card. Since byEvoLine is unchanged, and
	-- representatives were those same first-card objects we can safely use the first
	-- card again as it will still be the representative card
	byEvoLine:each(function(_, line)
		local cardType = line:get(1).type
		line:each(function(card)
			card.type = cardType
		end)
	end)
end

return module
