local randomizer = require("randomizer")
local pool_utils = require("modules.util.pool_utils")

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
	arguments = pool_utils.standardArgs(),
	execute = function(context, args)
		return module.randomizeEvoLineTypes(context, args)
	end,
}

function module.buildTypePool(context, args)
	local sourceCards = pool_utils.sourceCards(context, args.source)
	local types = randomizer.list(sourceCards):select("type")

	if args.duplicates == "KEEP_DUPLICATES" then
		-- Keep source multiplicity so common types stay more common
		return types
	end

	-- One of each type so every type has equal weight in the pool
	return types:removeDuplicates()
end

function module.randomizeEvoLineTypes(context, args)
	local monsterCards = context.modified:getRandomizableMonsterCards()
	local typePool = module.buildTypePool(context, args)

	-- Get one card from each evo line to set the type of
	local byEvoLine = randomizer.groupBy(monsterCards, "evoLineId")
	local representatives = byEvoLine:map(function(_, line)
		return line:get(1)
	end)

	typePool:useToRandomize(representatives, "type", pool_utils.poolOptions(args.approach))

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
