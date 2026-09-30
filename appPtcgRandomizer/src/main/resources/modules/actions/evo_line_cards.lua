-- Randomizes evo lines from per-line slot lists: dedupe names, shuffle pools, apply stage then prevEvo.
-- BY_STAGE_AND_MAX_STAGE keeps slot shape and shuffles names within each maxStage/stage pool.
-- BY_STAGE keeps slot shape but shuffles names within each stage pool.
-- ALL_TOGETHER keeps slot shape but ignores stage when picking names.
local common_field_defs = require("modules.util.common_field_defs")
local evo_line_randomize_utils = require("modules.util.evo_line_randomize_utils")
local randomizer = require("randomizer")
local logger = require("randomizer.logger")

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
		common_field_defs.ARG_DEF_EVO_LINE_STAGE_GROUPING,
	},
	execute = function(context, args)
		return module.randomizeEvoLines(context, args)
	end,
}

function module.randomizeEvoLines(context, args)
	evo_line_randomize_utils.init(context)

	local sourceCards = randomizer.list(evo_line_randomize_utils.sourceCards(context, args.source))
	local targets = randomizer.list(context.modified:getRandomizableMonsterCardsWithProxies())
	local evoLineData = evo_line_randomize_utils.extractAllEvoLineData(sourceCards)

	logger.debug("evo_line_cards source=" .. tostring(args.source) .. " withinType="
			.. tostring(args.withinType) .. " grouping=" .. tostring(args.grouping)
			.. " cards=" .. sourceCards:size())

	local namePools = evo_line_randomize_utils.buildNamePools(sourceCards, args.grouping, args.withinType)
	local toModifyByName = randomizer.groupBy(targets, "name")

	evo_line_randomize_utils.assignEvoLineData(evoLineData, namePools, args.grouping, args.withinType,
			toModifyByName)
	evo_line_randomize_utils.finalize(context, args, targets, toModifyByName)
end

return module
