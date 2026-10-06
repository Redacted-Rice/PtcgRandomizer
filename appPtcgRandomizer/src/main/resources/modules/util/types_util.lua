-- Shared type randomization helpers for evo line type modules.
local common_field_defs = require("modules.util.common_field_defs")
local pool_utils = require("modules.util.pool_utils")
local randomizer = require("randomizer")

local types_util = {}


----------------------- Bulding Pools -----------------------------


function types_util.buildEvoLineTypePoolFromCards(context, args)
	local sourceCards = pool_utils.sourceCards(context, args.source)
	-- One type per evo line to keep the evo line numbers the same for each type
	-- This won't exactly capture branching evos but its the best we can do per
	-- evo line
	local byEvoLine = randomizer.groupBy(sourceCards, "evoLineId")
	local types = byEvoLine:mapToList(function(_, line)
		return line:get(1).type
	end)

	if args.duplicates == "KEEP_DUPLICATES" then
		-- Keep source multiplicity so common types stay more common
		return types
	end
	-- One of each type so every type has equal weight in the pool
	return types:removeDuplicates()
end

function types_util.buildTypePoolFromWeights(context, typeWeights)
	local energyType = context.EnergyType
	local entries = {}
	-- Convert energy types to card types
	for _, typeKey in ipairs(common_field_defs.ENERGY_TYPE_KEYS) do
		local weight = typeWeights[typeKey]
		if weight ~= nil and weight > 0 then
			table.insert(entries, { type = energyType[typeKey]:convertToCardType(), weight = weight })
		end
	end
	if #entries == 0 then
		error("typeWeights must include at least one entry with a positive weight")
	end

	return randomizer.List.backedBy(entries):flatMapNTimes("weight", function(entry)
		return entry.type
	end)
end


----------------------- Randomizations -----------------------------


function types_util.randomizeEvoLineTypes(context, typePool, poolOptions)
	local monsterCards = context.modified:getRandomizableMonsterCards()
	local byEvoLine = randomizer.groupBy(monsterCards, "evoLineId")

	-- Get one card from each evo line to set the type of
	local representatives = byEvoLine:mapToList(function(_, line)
		return line:get(1)
	end)

	-- Randomize one entry from each line
	typePool:useToRandomize(representatives, "type", poolOptions)

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

return types_util
