-- Enforces non-decreasing retreat cost up each evolution line by stage.
-- Run after retreat cost randomization. Needs evoLineId on each card.
local fix_evo_line_utils = require("modules.util.fix_evo_line_utils")

local fixUtils = fix_evo_line_utils.create("retreatCost")

local module
module = {
	id = "retreat_cost_fix_evo_line",
	name = "Make Retreat Cost Consistent for Evo Lines",
	description = "For each evolution line, ensures retreat cost is non-decreasing by stage by either making higher"
					.. " stages match lower stages or swapping values between stages",
	groups = { "Monsters", "Retreat Cost", "Evolutions", "Support", "Consistency" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	needs = {
		{ name = "evoLineId", type = "integer" },
	},
	seeded = false,
	arguments = {
		fix_evo_line_utils.modeArg("retreat cost", "retreat costs"),
	},
	execute = function(context, args)
		return fixUtils.fixEvoLines(context, args)
	end,
}

return module
