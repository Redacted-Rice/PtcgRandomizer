-- Randomizes evo lines from per-line slot lists: dedupe names, shuffle pools, apply stage then prevEvo.
-- BY_STAGE_AND_MAX_STAGE keeps slot shape and shuffles names within each maxStage/stage pool.
-- BY_STAGE keeps slot shape but shuffles names within each stage pool.
-- ALL_TOGETHER keeps slot shape but ignores stage when picking names.
local common_field_defs = require("modules.util.common_field_defs")
local evo_line_randomize_utils = require("modules.util.evo_line_randomize_utils")
local randomizer = require("randomizer")

local module
module = {
	id = "evo_line_cards",
	name = "Randomize Evolution Lines (From Cards)",
	description = "Randomizes evolution lines from card data. Grouping controls whether evolutions, stages,"
		.. " or neither are preserved. Need to re run set evo line metadata if needed after this",
	groups = { "Monsters", "Evolutions" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	needs = {
		{ name = "evoLineId", type = "integer" },
		{ name = "evoLineMaxStage", type = "EvolutionStage" },
	},
	arguments = {
		common_field_defs.ARG_DEF_SOURCE,
		common_field_defs.ARG_DEF_WITHIN_TYPE,
		{
			name = "grouping",
			displayName = "Evo Stage Grouping",
			description = "By Stage And Max Stage keeps names and shuffles who they evolve from."
				.. " By Stage keeps line shape and stage but shuffles names within each stage pool."
				.. " All Together keeps line shape but ignores stage when picking names.",
			definition = {
				type = "enum",
				constraint = "StageGrouping",
			},
			default = "BY_STAGE_AND_MAX_STAGE",
		},
	},
	execute = function(context, args)
		return module.randomizeEvoLines(context, args)
	end,
}

function module.sourceCards(context, source)
	if source == "CURRENT" then
		return context.modified:getRandomizableMonsterCardsWithProxies()
	end
	return context.original:getRandomizableMonsterCardsWithProxies()
end

-- Creates a list of each cards' data (type, stage, maxStage, prevEvoIdx) in the line
function module.extractLineData(sourceLine)
	local nameToIdx = {}
	local entries = {}

	-- first sort by stage. This just makes it so we can know the prev evo should have been processed
	-- making the logic a bit simpler
	local sortedByStage = sourceLine:sort(function(a, b)
		return a.stage:getValue() < b.stage:getValue()
	end)

	-- group by name then for each group of names create and add one line entry data
	sortedByStage:groupBy("name"):each(function(name, cardsOfName)
		local card = cardsOfName:get(1)
		local prevEvoIdx = nil
		local prevName = card.prevEvoName:toString()
		if prevName ~= "" then
			prevEvoIdx = nameToIdx[prevName]
		end

		table.insert(entries, {
			type = card.type,
			stage = card.stage,
			maxStage = card.evoLineMaxStage,
			prevEvoIdx = prevEvoIdx,
		})
		-- Store the index by name for quick lookup of prev evo
		nameToIdx[name] = #entries
	end)

	return randomizer.list(entries)
end

-- Extract the evo line data from the cards
function module.extractAllEvoLineData(sourceCards)
	-- For each evo line, map it to the evo line data
	return randomizer.groupBy(sourceCards, "evoLineId"):map(function(evoLineId, sourceLine)
		return {
			-- We don't need original evo line id but keep it for debugging
			evoLineId = evoLineId,
			entries = module.extractLineData(sourceLine),
		}
	end)
end

function module.randomizeEvoLines(context, args)
	local sourceCards = randomizer.list(module.sourceCards(context, args.source))
	local targets = randomizer.list(context.modified:getRandomizableMonsterCardsWithProxies())
	local evoLineData = module.extractAllEvoLineData(sourceCards)

	evo_line_randomize_utils.randomize(context, args, sourceCards, evoLineData, targets)
end

return module
