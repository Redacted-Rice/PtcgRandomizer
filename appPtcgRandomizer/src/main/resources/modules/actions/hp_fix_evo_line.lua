-- Enforces non-decreasing HP up each evolution line by stage.
-- Run after HP randomization. Needs evoLineId and evoBranchIds on each card.
local enums_def = require("modules.util.enums_def")
local fix_evo_line_utils = require("modules.util.fix_evo_line_utils")

local fixUtils = fix_evo_line_utils.create("hp")

local module
module = {
	id = "hp_fix_evo_line",
	name = "Make HP Consistent for Evo Lines",
	description = "For each evolution line, ensures HP is non-decreasing by stage by either making"
			.. " higher stages match lower stages or swapping values between stages with"
			.. " controllable behavior for branching lines",
	groups = { "Monsters", "HP", "Evolutions", "Support", "Consistency" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	needs = {
		{ name = "evoLineId", type = "integer" },
		{ name = "evoBranchIds", type = "List<integer>" },
	},
	arguments = {
		fix_evo_line_utils.modeArg("HP", "HPs"),
		fix_evo_line_utils.branchHandlingArg(enums_def.BRANCH_HANDLING_ALL_TOGETHER,
				{ enums_def.BRANCH_HANDLING_SEPARATE }),
	},
	execute = function(args)
		return fixUtils.fixEvoLines(args)
	end,
}

return module
