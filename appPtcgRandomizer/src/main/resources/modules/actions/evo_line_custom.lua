-- Randomizes evolution lines using weighted custom evo lines ignoring card types
local common_field_defs = require("modules.util.common_field_defs")
local evo_line_randomize_utils = require("modules.util.evo_line_randomize_utils")
local logger = require("randomizer.logger")

local MAX_ASSIGN_ATTEMPTS = 10

local module
module = {
	id = "evo_line_custom",
	name = "Randomize Evolution Lines (Custom, All Together)",
	description = "Randomizes evolution lines with custom defined evo line data. Grouping controls"
			.. " whether evolutions, stages, or neither are preserved. Need to re-run set evo line"
			.. " metadata if needed after this",
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
		common_field_defs.ARG_DEF_RANDOMIZATION_APPROACH,
		common_field_defs.ARG_DEF_EVO_LINE_STAGE_GROUPING,
		common_field_defs.ARG_DEF_EVO_LINE_POOL,
		common_field_defs.ARG_DEF_EXCLUDE_TRAINERS_FROM_EVO_LINES,
	},
	execute = function(context, args)
		return module.randomizeCustomEvoLines(context, args)
	end,
}

function module.randomizeCustomEvoLines(context, args)
	evo_line_randomize_utils.init(context)
	for attempt = 1, MAX_ASSIGN_ATTEMPTS do
		local lastPass = attempt == MAX_ASSIGN_ATTEMPTS
		if evo_line_randomize_utils.randomizeCustomEvoLines(context, args, nil, lastPass) then
			return true
		end
		if not lastPass then
			logger.info(module.id .. " assignment attempt " .. attempt .. " did not succeed, retrying ("
					.. (attempt + 1) .. " of " .. MAX_ASSIGN_ATTEMPTS .. ")")
		end
	end
	logger.warn("'" .. module.name .. "' could not assign all names after " .. MAX_ASSIGN_ATTEMPTS
			.. " attempts for this seed. Please retry with more permissive options or a different seed")
	return false
end

return module
